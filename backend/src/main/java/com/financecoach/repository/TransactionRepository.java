package com.financecoach.repository;

import com.financecoach.entity.Transaction;
import com.financecoach.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUserId(Long userId, Sort sort);

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Sort sort);

    boolean existsByUserIdAndImportHash(Long userId, String importHash);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(@Param("userId") Long userId,
                                                      @Param("type") TransactionType type,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type AND t.category = :category " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndCategoryAndDateBetween(@Param("userId") Long userId,
                                                                  @Param("type") TransactionType type,
                                                                  @Param("category") String category,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId,
                                        @Param("type") TransactionType type);

    @Query("SELECT t.category as category, SUM(t.amount) as totalAmount, COUNT(t.id) as count " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category " +
           "ORDER BY totalAmount DESC")
    List<Object[]> findCategorySpendingSummary(@Param("userId") Long userId,
                                               @Param("type") TransactionType type,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Transaction> searchByDescription(@Param("userId") Long userId, @Param("query") String query, Sort sort);
}
