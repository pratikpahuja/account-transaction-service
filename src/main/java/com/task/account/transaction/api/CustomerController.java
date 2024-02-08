package com.task.account.transaction.api;

import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerRepository repository;

  @GetMapping
  public List<CustomerEntity> getCustomers() {
    return repository.findAll();
  }

}
