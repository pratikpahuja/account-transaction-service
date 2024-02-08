package com.task.account.transaction.api;

import com.task.account.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.stream.Stream;

import static java.lang.StringTemplate.STR;
import static java.math.BigDecimal.valueOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @Autowired MockMvc mvc;
  @MockBean TransactionService transactionService;
  @MockBean Clock clock;

  @ParameterizedTest
  @MethodSource("invalidRequestForPostTransaction")
  void postTransactionInvalidRequest(String requestBody) throws Exception {
    performPostTransaction(requestBody)
      .andExpect(status().isBadRequest());
  }

  @Test
  void postTransactionValidRequest() throws Exception {
    var customerStr = customer(10L, "www.test.com");
    performPostTransaction(postTransactionRequestBody("test-service", valueOf(23.23), "PAY_IN", customerStr))
      .andExpect(status().isOk());
  }

  private ResultActions performPostTransaction(String requestBody) throws Exception {
    return mvc.perform(post("/api/v1/transactions")
      .contentType(MediaType.APPLICATION_JSON)
      .content(requestBody));
  }

  public static Stream<Arguments> invalidRequestForPostTransaction() {
    var customerStr = customer(10L, "www.test.com");

    return Stream.of(
      Arguments.of(postTransactionRequestBody(null, valueOf(23.23), "PAY_IN", customerStr)), //service null
      Arguments.of(postTransactionRequestBody("", valueOf(23.23), "PAY_IN", customerStr)),   //service empty
      Arguments.of(postTransactionRequestBody("test-service", null, "PAY_IN", customerStr)), //amount null
      Arguments.of(postTransactionRequestBody("test-service", valueOf(23.23), null, customerStr)),   //type null
      Arguments.of(postTransactionRequestBody("test-service", valueOf(23.23), "invalid-type", customerStr)),  //type invalid
      Arguments.of(postTransactionRequestBody("test-service", valueOf(23.23), "PAY_OUT", null))  //null customer
    );
  }

  private static String postTransactionRequestBody(String service, BigDecimal amount, String type, String customer) {

    return STR."""
      {
        \{customer == null ? "" : STR."\"customer\": \{customer},"}
        \{service == null ? "" : STR."\"service\": \"\{service}\","}
        \{amount == null ? "" : STR."\"amount\": \"\{amount}\","}
        \{type == null ? "" : STR."\"type\": \"\{type}\""}
      }
      """;
  }

  private static String customer(Long customerId, String tenant) {
    return STR."""
      {
        \{customerId == null ? "" : STR."\"customerId\": \"\{customerId}\","}
        \{tenant == null ? "" : STR."\"tenant\": \"\{tenant}\""}
      }
      """;
  }
}