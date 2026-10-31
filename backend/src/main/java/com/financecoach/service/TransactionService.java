package com.financecoach.service;

import com.financecoach.dto.TransactionDto;
import com.financecoach.dto.TransactionRequest;
import com.financecoach.entity.Transaction;
import com.financecoach.entity.TransactionType;
import com.financecoach.entity.User;
import com.financecoach.exception.BadRequestException;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.TransactionRepository;
import com.financecoach.repository.UserRepository;
import com.financecoach.util.CsvStatementParser;
import com.financecoach.util.ParsedStatementRow;
import com.financecoach.util.PdfStatementParser;
import com.financecoach.util.SmsStatementParser;
import com.financecoach.util.StatementImportHelper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final UserRepository users;
    private final CategorizationService categorizationService;

    public TransactionService(TransactionRepository repository,
                              UserRepository users,
                              CategorizationService categorizationService) {
        this.repository = repository;
        this.users = users;
        this.categorizationService = categorizationService;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> list(Long userId, String query, TransactionType type, String category, LocalDate from, LocalDate to) {
        Sort sort = Sort.by(Sort.Direction.DESC, "transactionDate").and(Sort.by(Sort.Direction.DESC, "id"));
        List<Transaction> all;

        if (from != null && to != null) {
            all = repository.findByUserIdAndTransactionDateBetween(userId, from, to, sort);
        } else if (query != null && !query.isBlank()) {
            all = repository.searchByDescription(userId, query, sort);
        } else {
            all = repository.findByUserId(userId, sort);
        }

        return all.stream()
            .filter(t -> type == null || t.getType() == type)
            .filter(t -> category == null || category.isBlank() || category.equalsIgnoreCase("ALL") || t.getCategory().equalsIgnoreCase(category))
            .filter(t -> from == null || !t.getTransactionDate().isBefore(from))
            .filter(t -> to == null || !t.getTransactionDate().isAfter(to))
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public TransactionDto create(Long userId, TransactionRequest r) {
        User u = user(userId);

        // Deterministic auto-categorization if category is empty or generic "Other"
        String category = r.getCategory();
        if (category == null || category.isBlank() || category.equalsIgnoreCase("Other")) {
            category = categorizationService.categorize(userId, r.getDescription(), r.getMerchant());
        }

        String merchant = r.getMerchant();
        if (merchant == null || merchant.isBlank()) {
            merchant = CsvStatementParser.extractMerchant(r.getDescription());
        }

        String hash = StatementImportHelper.computeImportHash(
            userId, r.getTransactionDate(), r.getAmount(), r.getDescription()
        );

        Transaction t = new Transaction(
            u,
            r.getDescription().trim(),
            r.getAmount(),
            r.getTransactionDate(),
            r.getType(),
            category,
            merchant,
            r.getPaymentMethod(),
            r.isRecurring()
        );
        t.setImportHash(hash);

        return toDto(repository.save(t));
    }

    @Transactional
    public TransactionDto update(Long userId, Long id, TransactionRequest r) {
        Transaction t = owned(userId, id);
        String oldCategory = t.getCategory();
        String newCategory = r.getCategory() != null ? r.getCategory().trim() : "Other";

        // Requirement 7: When user corrects a category, save the correction as a categorization rule
        if (!newCategory.equalsIgnoreCase(oldCategory)) {
            String ruleKeyword = r.getMerchant();
            if (ruleKeyword == null || ruleKeyword.isBlank()) {
                ruleKeyword = CsvStatementParser.extractMerchant(r.getDescription());
            }
            if (ruleKeyword != null && !ruleKeyword.isBlank()) {
                categorizationService.learnUserCorrection(userId, ruleKeyword, newCategory);
            }
        }

        t.setDescription(r.getDescription().trim());
        t.setAmount(r.getAmount());
        t.setTransactionDate(r.getTransactionDate());
        t.setType(r.getType());
        t.setCategory(newCategory);
        t.setMerchant(r.getMerchant());
        t.setPaymentMethod(r.getPaymentMethod());
        t.setRecurring(r.isRecurring());

        return toDto(repository.save(t));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(owned(userId, id));
    }

    @Transactional(readOnly = true)
    public TransactionDto get(Long userId, Long id) {
        return toDto(owned(userId, id));
    }

    @Transactional
    public Map<String, Object> importStatement(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Import file is required");
        }

        User u = user(userId);
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();

        List<ParsedStatementRow> rows;
        try {
            if (name.endsWith(".pdf") || contentType.equals("application/pdf")) {
                rows = PdfStatementParser.parse(file.getInputStream());
            } else if (name.endsWith(".sms")) {
                rows = SmsStatementParser.parse(file.getInputStream());
            } else if (name.endsWith(".csv") || contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
                rows = CsvStatementParser.parse(file.getInputStream());
            } else if (name.endsWith(".txt")) {
                // For .txt files, try CSV format first; if it has no CSV headers, fall back to SMS statement parsing
                try {
                    rows = CsvStatementParser.parse(file.getInputStream());
                } catch (Exception ex) {
                    rows = SmsStatementParser.parse(file.getInputStream());
                }
            } else {
                throw new BadRequestException("Unsupported file format: " + file.getOriginalFilename() + ". Please upload a CSV, PDF, or SMS text file.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Unable to read statement file: " + e.getMessage());
        }

        return saveParsedRows(u, rows);
    }

    @Transactional
    public Map<String, Object> importSms(Long userId, String smsText) {
        if (smsText == null || smsText.isBlank()) {
            throw new BadRequestException("SMS text cannot be blank");
        }

        User u = user(userId);
        List<ParsedStatementRow> rows = SmsStatementParser.parseText(smsText);
        return saveParsedRows(u, rows);
    }

    private Map<String, Object> saveParsedRows(User u, List<ParsedStatementRow> rows) {
        int imported = 0;
        int duplicates = 0;
        List<Transaction> toSave = new ArrayList<>();

        for (ParsedStatementRow row : rows) {
            String hash = StatementImportHelper.computeImportHash(
                u.getId(), row.getDate(), row.getAmount(), row.getDescription()
            );

            // Duplicate prevention
            if (repository.existsByUserIdAndImportHash(u.getId(), hash)) {
                duplicates++;
                continue;
            }

            // Automatic rule-based categorization
            String category = categorizationService.categorize(u.getId(), row.getDescription(), row.getMerchant());

            Transaction t = new Transaction(
                u,
                row.getDescription(),
                row.getAmount(),
                row.getDate(),
                row.getType(),
                category,
                row.getMerchant(),
                row.getPaymentMethod(),
                false
            );
            t.setImportHash(hash);
            toSave.add(t);
            imported++;
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }

        return Map.of(
            "imported", imported,
            "skipped", duplicates,
            "totalParsed", rows.size()
        );
    }

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Transaction owned(Long userId, Long id) {
        return repository.findById(id)
            .filter(t -> t.getUser().getId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(
            t.getId(),
            t.getDescription(),
            t.getAmount(),
            t.getTransactionDate(),
            t.getType(),
            t.getCategory(),
            t.getMerchant(),
            t.getPaymentMethod(),
            t.isRecurring()
        );
    }
}
