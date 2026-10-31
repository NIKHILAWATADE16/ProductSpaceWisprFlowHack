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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
 private final TransactionRepository repository; private final UserRepository users;
 public TransactionService(TransactionRepository repository, UserRepository users){this.repository=repository;this.users=users;}
 @Transactional(readOnly=true) public List<TransactionDto> list(Long userId,String query,TransactionType type,String category,LocalDate from,LocalDate to){
  Sort sort=Sort.by(Sort.Direction.DESC,"transactionDate"); List<Transaction> all;
  if(from!=null&&to!=null) all=repository.findByUserIdAndTransactionDateBetween(userId,from,to,sort); else if(query!=null&&!query.isBlank()) all=repository.searchByDescription(userId,query,sort); else all=repository.findByUserId(userId,sort);
  return all.stream().filter(t->type==null||t.getType()==type).filter(t->category==null||category.isBlank()||t.getCategory().equalsIgnoreCase(category)).filter(t->from==null||!t.getTransactionDate().isBefore(from)).filter(t->to==null||!t.getTransactionDate().isAfter(to)).map(this::toDto).toList();
 }
 @Transactional public TransactionDto create(Long userId,TransactionRequest r){User u=user(userId); return toDto(repository.save(new Transaction(u,r.getDescription(),r.getAmount(),r.getTransactionDate(),r.getType(),r.getCategory(),r.getMerchant(),r.getPaymentMethod(),r.isRecurring())));}
 @Transactional public TransactionDto update(Long userId,Long id,TransactionRequest r){Transaction t=owned(userId,id); t.setDescription(r.getDescription());t.setAmount(r.getAmount());t.setTransactionDate(r.getTransactionDate());t.setType(r.getType());t.setCategory(r.getCategory());t.setMerchant(r.getMerchant());t.setPaymentMethod(r.getPaymentMethod());t.setRecurring(r.isRecurring());return toDto(repository.save(t));}
 @Transactional public void delete(Long userId,Long id){repository.delete(owned(userId,id));}
 @Transactional public int importCsv(Long userId,MultipartFile file){if(file==null||file.isEmpty())throw new BadRequestException("Import file is required");String name=file.getOriginalFilename()==null?"":file.getOriginalFilename().toLowerCase();if(name.endsWith(".pdf")||"application/pdf".equalsIgnoreCase(file.getContentType()))throw new BadRequestException("PDF transaction import is not supported yet; upload a CSV file");if(!name.endsWith(".csv")&&!"text/csv".equalsIgnoreCase(file.getContentType()))throw new BadRequestException("Only CSV files are supported");int imported=0;try(Reader reader=new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8);CSVParser parser=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build().parse(reader)){for(CSVRecord row:parser){String description=val(row,"description","merchant","name");String amountText=val(row,"amount","value");String dateText=val(row,"date","transaction_date","transactionDate");if(description==null||amountText==null||dateText==null)throw new BadRequestException("CSV requires description, amount, and date columns");BigDecimal amount;LocalDate date;try{amount=new BigDecimal(amountText.replace(",",""));date=LocalDate.parse(dateText);}catch(NumberFormatException|DateTimeParseException ex){throw new BadRequestException("Invalid amount or ISO date in CSV row "+row.getRecordNumber());}TransactionType type=TransactionType.EXPENSE;String typeText=val(row,"type","transaction_type");if(typeText!=null&&typeText.equalsIgnoreCase("income"))type=TransactionType.INCOME;if(amount.signum()<0){amount=amount.abs();type=type==TransactionType.INCOME?TransactionType.EXPENSE:TransactionType.INCOME;}String hash=sha256(userId+"|"+description+"|"+amount+"|"+date+"|"+type);if(repository.existsByUserIdAndImportHash(userId,hash))continue;Transaction t=new Transaction(user(userId),description,amount,date,type,defaultValue(val(row,"category"),"Other"),val(row,"merchant"),val(row,"payment_method","paymentMethod"),false);t.setImportHash(hash);repository.save(t);imported++;}}catch(IOException ex){throw new BadRequestException("Unable to read CSV file");}return imported;}
 private String val(CSVRecord r,String... keys){for(String k:keys)if(r.isMapped(k)&&!r.get(k).isBlank())return r.get(k);return null;} private String defaultValue(String v,String d){return v==null||v.isBlank()?d:v;}
 private String sha256(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b));return out.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 private User user(Long id){return users.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));} private Transaction owned(Long userId,Long id){return repository.findById(id).filter(t->t.getUser().getId().equals(userId)).orElseThrow(()->new ResourceNotFoundException("Transaction not found"));}
 private TransactionDto toDto(Transaction t){return new TransactionDto(t.getId(),t.getDescription(),t.getAmount(),t.getTransactionDate(),t.getType(),t.getCategory(),t.getMerchant(),t.getPaymentMethod(),t.isRecurring());}
}
