package com.task.account.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.task.account.transaction.domain.TransactionEntity;
import com.task.account.transaction.service.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
  , properties = {
  "spring.datasource.url=jdbc:postgresql://localhost/transaction_test_db",
  "spring.liquibase:.contexts=tests"
})
public class E2EIntegrationTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void performCompleteTransactionFlowWithRollback() throws JsonProcessingException {
    var transaction = performTransaction();

    //Fetch latest transaction.
    var transactions = performFetchTransactions();
    assertThat(transactions.get(0), is(transaction));

    var rollbackHttpStatus = performRollback(transaction.getId());
    assertThat(rollbackHttpStatus, is(HttpStatusCode.valueOf(204)));

    transactions = performFetchTransactions();
    assertThat(transactions.get(0).getAmount(), is(transaction.getAmount().negate()));
    assertThat(transactions.get(0).getRollingBackTransactionId(), is(transaction.getId()));
  }

  private List<TransactionEntity> performFetchTransactions() throws JsonProcessingException {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    var uri = UriComponentsBuilder.fromUriString("/api/v1/transactions")
      .queryParam("tenant", "tenant1")
      .queryParam("customerId", 110)
      .queryParam("count", 1)
      .build();

    var request = new RequestEntity(headers, HttpMethod.GET, uri.toUri());

    System.out.println(restTemplate.exchange(request, String.class).getBody());

    return restTemplate.exchange(request, new ParameterizedTypeReference<List<TransactionEntity>>() {}).getBody();
  }

  private HttpStatusCode performRollback(long transactionId) throws JsonProcessingException {
    var requestEntity = RequestEntity.post(STR."/api/v1/transactions/\{transactionId}/rollback")
      .contentType(MediaType.APPLICATION_JSON)
      .body(performRollbackRequest());

    var response = restTemplate
      .exchange(requestEntity, String.class);

    return response.getStatusCode();
  }

  private TransactionEntity performTransaction() throws JsonProcessingException {
    var amount = randomAmount();
    var type = randomTransactionType();

    var requestEntity = RequestEntity.post("/api/v1/transactions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(performTransactionRequest(BigDecimal.valueOf(amount), type));

    return restTemplate
      .exchange(requestEntity, TransactionEntity.class)
      .getBody();
  }

  String performTransactionRequest(BigDecimal amount, TransactionType type) {
    return STR."""
      {
        "service": "demo-service",
        "amount": \{amount},
        "type": "\{type}",
        "customer": {
          "customerId": 110,
          "tenant": "tenant1"
        }
      }
      """;
  }

  private String performRollbackRequest() {
    return """
      {
        "customer": {
          "customerId": 110,
          "tenant": "tenant1"
        }
      }
      """;
  }

  public static double randomAmount() {
    var df = new DecimalFormat("#.##");
    double amount = new Random().nextDouble(100D) + 1;
    return Double.parseDouble(df.format(amount));
  }

  public static TransactionType randomTransactionType() {
    return new Random().nextInt(2) == 0
      ? TransactionType.PAY_IN
      : TransactionType.PAY_OUT;
  }
}
