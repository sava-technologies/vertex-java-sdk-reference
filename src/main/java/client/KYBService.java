package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KYBService {

  private final Connection nc;
  private final String token;
  private final ObjectMapper objectMapper;

  private final List<String> subjectParams;

  public KYBService(Connection nc, String token, List<String> subjectParams) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    this.nc = nc;
    this.token = token;
    this.objectMapper = new ObjectMapper();

    this.subjectParams = subjectParams;
  }

  public CompletableFuture<GetKYBResponse> get(GetKYBRequest req) {
    String ep = "svc.kyb.*.get";

    StringBuilder epBuilder = new StringBuilder("svc.kyb.*.get");
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
                  return this.objectMapper.readValue(msg.getData(), GetKYBResponse.class);
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

  public CompletableFuture<SubmitDocumentsResponse> submit(SubmitDocumentsRequest req) {
    String ep = "svc.kyb.*.submit";

    StringBuilder epBuilder = new StringBuilder("svc.kyb.*.submit");
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
                  return this.objectMapper.readValue(msg.getData(), SubmitDocumentsResponse.class);
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

  public CompletableFuture<UpdateKYBResponse> update(UpdateKYBRequest req) {
    String ep = "svc.kyb.*.update";

    StringBuilder epBuilder = new StringBuilder("svc.kyb.*.update");
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
                  return this.objectMapper.readValue(msg.getData(), UpdateKYBResponse.class);
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

  public CompletableFuture<SendDirectorVerificationLinkResponse> send_verification_email(
      SendDirectorVerificationLinkRequest req) {
    String ep = "svc.kyb.*.send_verification_email";

    StringBuilder epBuilder = new StringBuilder("svc.kyb.*.send_verification_email");
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
                      msg.getData(), SendDirectorVerificationLinkResponse.class);
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

  public CompletableFuture<UpdateDirectorResponse> update_director(UpdateDirectorRequest req) {
    String ep = "svc.kyb.*.update_director";

    StringBuilder epBuilder = new StringBuilder("svc.kyb.*.update_director");
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
                  return this.objectMapper.readValue(msg.getData(), UpdateDirectorResponse.class);
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

  public record Director(
      String id,
      String name,
      boolean approved,
      boolean verified,
      List<Document> documents,
      boolean verification_sent,
      String id_type,
      Map<String, Object> metadata) {}

  public record Document(String key, String doc_type, String rejection_reason, boolean approved) {}

  public record GetKYBRequest(String entity_id) {}

  public record GetKYBResponse(
      String entity_id,
      List<Document> documents,
      List<Director> directors,
      Map<String, Object> metadata,
      String state,
      String rejection_reason) {}

  public record UUID() {}

  public record SubmitDocumentsRequest(
      String entity_id, List<String> documents, Map<String, List<String>> directors) {}

  public record SubmitDocumentsResponse() {}

  public record UpdateKYBRequest(String entity_id, Map<String, Object> metadata) {}

  public record UpdateKYBResponse(
      String entity_id,
      List<Document> documents,
      List<Director> directors,
      Map<String, Object> metadata) {}

  public record SendDirectorVerificationLinkRequest(
      String entity_id, String director_id, String email) {}

  public record SendDirectorVerificationLinkResponse() {}

  public record UpdateDirectorRequest(
      String entity_id,
      String director_id,
      String full_name,
      String email,
      Map<String, Object> metadata) {}

  public record UpdateDirectorResponse() {}
}
