package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AccountService {

  private final Connection nc;
  private final String token;
  private final ObjectMapper objectMapper;

  private final List<String> subjectParams;

  public AccountService(Connection nc, String token, List<String> subjectParams) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    this.nc = nc;
    this.token = token;
    this.objectMapper = new ObjectMapper();

    this.subjectParams = subjectParams;
  }

  public CompletableFuture<OpenAccountResponse> create(OpenAccountRequest req) {
    String ep = "svc.account.*.create";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.create");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(msg.getData(), OpenAccountResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<GetAccountDetailsResponse> get_details(GetAccountDetailsRequest req) {
    String ep = "svc.account.*.get_details";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.get_details");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(
                      msg.getData(), GetAccountDetailsResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<TransferEFTRTCResponse> transfer_eft_rtc(TransferEFTRTCRequest req) {
    String ep = "svc.account.*.transfer_eft_rtc";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.transfer_eft_rtc");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(msg.getData(), TransferEFTRTCResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<TransferEFTRTCResponse> transfer_internal(TransferInternalRequest req) {
    String ep = "svc.account.*.transfer_internal";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.transfer_internal");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(msg.getData(), TransferEFTRTCResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<GetTransactionsResponse> list(GetTransactionsRequest req) {
    String ep = "svc.account.*.list";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.list");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(msg.getData(), GetTransactionsResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<GetAccountsByEntityResponse> get_by_entity(
      GetAccountsByEntityRequest req) {
    String ep = "svc.account.*.get_by_entity";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.get_by_entity");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(
                      msg.getData(), GetAccountsByEntityResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<GetTransactionByIdResponse> get_transaction(
      GetTransactionByIdRequest req) {
    String ep = "svc.account.*.get_transaction";

    StringBuilder epBuilder = new StringBuilder("svc.account.*.get_transaction");
    for (String param : this.subjectParams) {
      int index = epBuilder.indexOf("*");
      if (index != -1) {
        epBuilder.replace(index, index + 1, param);
      }
    }
    ep = epBuilder.toString();

    Headers headers = new Headers();
    headers.add("token", this.token);

    try {
      byte[] payload = this.objectMapper.writeValueAsBytes(req);

      return this.nc
          .request(ep, headers, payload)
          .thenApply(
              msg -> {
                try {
                  ServiceException.fromMessage(msg)
                      .ifPresent(
                          e -> {
                            throw e;
                          });
                  return this.objectMapper.readValue(
                      msg.getData(), GetTransactionByIdResponse.class);
                } catch (ServiceException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RuntimeException("Serialization Error", e);
                }
              });
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  // Generated Types

  public static class Metadata extends java.util.HashMap<String, String> {}

  public record OpenAccountRequest(String entity_id) {}

  public record OpenAccountResponse(
      String account_id,
      String entity_id,
      String account_number,
      String country,
      String created_at,
      String account_type,
      Metadata metadata) {}

  public record UUID() {}

  public record GetAccountDetailsRequest(String account_id) {}

  public record GetAccountDetailsResponse(
      String account_id,
      String account_number,
      String balance,
      String available_balance,
      String account_type,
      Metadata metadata) {}

  public record TransferEFTRTCRequest(
      String account_id,
      String clientTxId,
      int amount,
      String ref,
      String own_ref,
      String account_number,
      String branch_code,
      String name,
      boolean pay_and_clear,
      String notification_email,
      String notification_name,
      String beneficiary_id,
      int beneficiary_version) {}

  public record TransferEFTRTCResponse(String tx_id, String amount) {}

  public record TransferInternalRequest(
      String account_id,
      String clientTxId,
      int amount,
      String ref,
      String own_ref,
      String account_number,
      String name,
      String notification_email,
      String notification_name,
      String beneficiary_id,
      int beneficiary_version) {}

  public record GetTransactionsRequest(String account_id, UUIDs filter_user_ids) {}

  public record GetTransactionsResponse(String account_id, List<Transaction> transactions) {}

  public record Transaction(
      String tx_id,
      String parent_tx_id,
      String date,
      String amount,
      String ref,
      String status,
      String tx_sha,
      String user_id,
      String added_by,
      String tx_type,
      String tx_payment_type,
      String fee,
      String recipient_name,
      String credit,
      String debit,
      String running_balance,
      boolean verified) {}

  public record UUIDs() {}

  public record AccountSummary(
      String account_id,
      String account_number,
      String created_at,
      String account_type,
      Metadata metadata) {}

  public record GetAccountsByEntityRequest(String entity_id) {}

  public record GetAccountsByEntityResponse(String entity_id, List<AccountSummary> accounts) {}

  public record GetTransactionByIdRequest(String tx_id) {}

  public record GetTransactionByIdResponse(Transaction transaction) {}
}
