package com.task.account.transaction.repository;

import com.task.account.transaction.domain.CustomerEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends org.springframework.data.repository.Repository<CustomerEntity, Long> {


  @Query("FROM CustomerEntity")
  List<CustomerEntity> findAll();

  @Query("FROM CustomerEntity c "
    + "JOIN c.tenant t "
    + "WHERE t.name=:tenant AND c.customerId=:customerId")
  Optional<CustomerEntity> findByIdAndTenant(long customerId, String tenant);

  @Modifying
  @Transactional
  @Query("UPDATE CustomerEntity c "
    + "SET accountBalance = accountBalance + :amountToAdd "
    + "WHERE id = :customerEntityId")
  int updateAccountBalance(long customerEntityId, BigDecimal amountToAdd);
}
