package client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntityService {

  private final Connection nc;
  private final String token;
  private final ObjectMapper objectMapper;

  private final List<String> subjectParams;

  public EntityService(Connection nc, String token, List<String> subjectParams) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    this.nc = nc;
    this.token = token;
    this.objectMapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    this.subjectParams = subjectParams;
  }

  public CompletableFuture<EntityInfoResponse> info(EntityInfoRequest req) {
    String ep = "svc.entity.*.info";

    StringBuilder epBuilder = new StringBuilder("svc.entity.*.info");
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
                  return this.objectMapper.readValue(msg.getData(), EntityInfoResponse.class);
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

  public CompletableFuture<UpdateAddressResponse> update_address(UpdateAddressRequest req) {
    String ep = "svc.entity.*.update_address";

    StringBuilder epBuilder = new StringBuilder("svc.entity.*.update_address");
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
                  return this.objectMapper.readValue(msg.getData(), UpdateAddressResponse.class);
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

  public CompletableFuture<CreateEntityResponse> create(CreateEntityRequest req) {
    String ep = "svc.entity.*.create";

    StringBuilder epBuilder = new StringBuilder("svc.entity.*.create");
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
                  return this.objectMapper.readValue(msg.getData(), CreateEntityResponse.class);
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

  public CompletableFuture<ListEntityResponse> list_entities(ListEntityRequest req) {
    String ep = "svc.entity.*.list-entities";

    StringBuilder epBuilder = new StringBuilder("svc.entity.*.list-entities");
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
                  return this.objectMapper.readValue(msg.getData(), ListEntityResponse.class);
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

  public record EntityInfoRequest(String entity_id) {}

  public record EntityInfoResponse(
      String id,
      String date_created,
      String name,
      String trading_name,
      String entity_type,
      String email,
      boolean approved,
      String first_name,
      String last_name,
      String phone_number,
      String gender,
      String date_of_birth,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry_date,
      String city,
      String residency,
      String title,
      String permit_number) {}

  public record UUID() {}

  public record UpdateAddressRequest(
      String entity_id,
      String address_line_1,
      String address_line_2,
      String state,
      String postcode,
      String country,
      String city) {}

  public record UpdateAddressResponse() {}

  public record Address(
      String address_line_1, String address_line_2, String city, String state, String postcode) {}

  public record CreateEntityRequest(
      String name,
      String trading_name,
      String tax_number,
      String registration_number,
      String entity_type,
      String purpose,
      String email,
      String country,
      String first_name,
      String last_name,
      String phone_number,
      String gender,
      String date_of_birth,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry_date,
      String city,
      String residency,
      String title,
      String permit_number,
      Address address) {}

  public record CreateEntityResponse(String id, boolean user_ready, String user_id) {}

  public record Entity(
      String id,
      String name,
      String trading_name,
      String registration_number,
      String email,
      String created_at,
      String entity_type,
      String first_name,
      String last_name,
      String phone_number,
      String gender,
      String date_of_birth,
      String id_number,
      String id_type,
      String id_issue_date,
      String id_issue_expiry_date,
      String city,
      String residency,
      String title,
      String permit_number) {}

  public record ListEntityRequest() {}

  public record ListEntityResponse(List<Entity> entities) {}
}
