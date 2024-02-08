package com.task.account.transaction.repository;

import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.domain.TransactionEntity;
import com.task.account.transaction.domain.TransactionState;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends CrudRepository<TransactionEntity, Long> {

  @Query("FROM TransactionEntity t JOIN FETCH t.customer WHERE t.id=:transactionId")
  Optional<TransactionEntity> findById(long transactionId);

  List<TransactionEntity> findByCustomerOrderByCreatedAtDesc(CustomerEntity customer, Limit limit);

  @Modifying
  @Query(value = "UPDATE transaction "
    + "SET state='ROLLING_BACK' "
    + "WHERE id=:transactionId "
    + "AND state='ACTIVE'",
    nativeQuery = true)
  @Transactional
  int markTransactionRollingBack(long transactionId);

  @Modifying
  @Query(value = "UPDATE transaction "
    + "SET state='ROLLED_BACK', rolled_back_by_transaction_id=:rolledBackByTransactionId "
    + "WHERE state='ROLLING_BACK' "
    + "AND id=:rollingBackTransactionId",
    nativeQuery = true)
  @Transactional
  int markTransactionRolledBack(long rollingBackTransactionId, long rolledBackByTransactionId);

  @Query(value = "INSERT INTO transaction(id, customer_tenant_id, service, amount, state, rolling_back_transaction_id, created_at) "
    + "SELECT nextval('transaction_id_seq'), customer_tenant_id, service, -1 * amount, 'ACTIVE', id, :createdAt "
    + "FROM transaction "
    + "WHERE id=:transactionId AND state='ROLLING_BACK' "
    + "RETURNING id",
    nativeQuery = true)
  @Transactional
  Optional<Integer> rollbackTransaction(long transactionId, Instant createdAt);
}
