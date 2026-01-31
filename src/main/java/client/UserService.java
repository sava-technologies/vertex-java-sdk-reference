package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService {

  private final Connection nc;
  private final String token;
  private final ObjectMapper objectMapper;

  private final List<String> subjectParams;

  public UserService(Connection nc, String token, List<String> subjectParams) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    this.nc = nc;
    this.token = token;
    this.objectMapper = new ObjectMapper();

    this.subjectParams = subjectParams;
  }

  public CompletableFuture<CreateUserResponse> create(CreateUserRequest req) {
    String ep = "svc.user.*.create";

    StringBuilder epBuilder = new StringBuilder("svc.user.*.create");
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
                  return this.objectMapper.readValue(msg.getData(), CreateUserResponse.class);
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

  public CompletableFuture<UpdateUserProfileResponse> update(UpdateUserProfileRequest req) {
    String ep = "svc.user.*.update";

    StringBuilder epBuilder = new StringBuilder("svc.user.*.update");
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
                      msg.getData(), UpdateUserProfileResponse.class);
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

  public CompletableFuture<ListUserResponse> list(ListUserRequest req) {
    String ep = "svc.user.*.list";

    StringBuilder epBuilder = new StringBuilder("svc.user.*.list");
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
                  return this.objectMapper.readValue(msg.getData(), ListUserResponse.class);
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

  public CompletableFuture<UploadKYCDocumentsResponse> upload_kyc_documents(
      UploadKYCDocumentsRequest req) {
    String ep = "svc.user.*.upload_kyc_documents";

    StringBuilder epBuilder = new StringBuilder("svc.user.*.upload_kyc_documents");
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
                      msg.getData(), UploadKYCDocumentsResponse.class);
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

  public record CreateUserRequest(
      String first_name,
      String last_name,
      String email,
      String entity_id,
      String gender,
      String date_of_birth,
      String country,
      String city,
      String residency,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry_date,
      String phone_number,
      String title,
      boolean verified,
      String permit_number) {}

  public record CreateUserResponse(String userId) {}

  public record UUID() {}

  public record UpdateUserProfileRequest(
      String user_id,
      String first_name,
      String last_name,
      String email,
      String phone_number,
      String gender,
      String date_of_birth,
      String birth_country,
      String birth_city,
      String residency,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry_date,
      String title,
      String permit_number) {}

  public record UpdateUserProfileResponse(String userId) {}

  public record ListUserRequest(String entity_id) {}

  public record ListUserResponse(List<User> users) {}

  public record User(
      String id,
      String first_name,
      String last_name,
      String email,
      String phone_number,
      String entity_id,
      String created_at,
      String updated_at,
      boolean verified,
      String verified_at,
      String gender,
      String date_of_birth,
      String country,
      String city,
      String residency,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry,
      String title,
      String date_registered,
      String permit_number,
      String kyc_status,
      String kyc_submitted_at,
      String kyc_reviewed_at) {}

  public record UploadKYCDocumentsRequest(
      String user_id, String id_document, String proof_of_residence) {}

  public record UploadKYCDocumentsResponse(boolean success, String message) {}
}
