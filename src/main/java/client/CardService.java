package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CardService {

  private final Connection nc;
  private final String token;
  private final ObjectMapper objectMapper;

  private final List<String> subjectParams;

  public CardService(Connection nc, String token, List<String> subjectParams) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    this.nc = nc;
    this.token = token;
    this.objectMapper = new ObjectMapper();

    this.subjectParams = subjectParams;
  }

  public CompletableFuture<RequestCardResponse> request(RequestCardRequest req) {
    String ep = "svc.card.*.request";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.request");
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
                  return this.objectMapper.readValue(msg.getData(), RequestCardResponse.class);
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

  public CompletableFuture<ListOrganisationCardRequestsResponse> list_requests(
      ListOrganisationCardRequestsRequest req) {
    String ep = "svc.card.*.list_requests";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.list_requests");
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
                      msg.getData(), ListOrganisationCardRequestsResponse.class);
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

  public CompletableFuture<RespondToCardRequestResponse> respond_to_request(
      RespondToCardRequestRequest req) {
    String ep = "svc.card.*.respond_to_request";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.respond_to_request");
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
                      msg.getData(), RespondToCardRequestResponse.class);
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

  public CompletableFuture<EditCardResponse> edit(EditCardRequest req) {
    String ep = "svc.card.*.edit";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.edit");
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
                  return this.objectMapper.readValue(msg.getData(), EditCardResponse.class);
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

  public CompletableFuture<ListOrganisationCardsResponse> list_cards(
      ListOrganisationCardsRequest req) {
    String ep = "svc.card.*.list_cards";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.list_cards");
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
                      msg.getData(), ListOrganisationCardsResponse.class);
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

  public CompletableFuture<GetCardDetailsResponse> get_details(GetCardDetailsRequest req) {
    String ep = "svc.card.*.get_details";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.get_details");
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
                  return this.objectMapper.readValue(msg.getData(), GetCardDetailsResponse.class);
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

  public CompletableFuture<UpdateCardStatusResponse> update_status(UpdateCardStatusRequest req) {
    String ep = "svc.card.*.update_status";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.update_status");
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
                  return this.objectMapper.readValue(msg.getData(), UpdateCardStatusResponse.class);
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

  public CompletableFuture<ActivateCardResponse> activate_card(ActivateCardRequest req) {
    String ep = "svc.card.*.activate_card";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.activate_card");
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
                  return this.objectMapper.readValue(msg.getData(), ActivateCardResponse.class);
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

  public CompletableFuture<SetPINResponse> set_pin(SetPINRequest req) {
    String ep = "svc.card.*.set_pin";

    StringBuilder epBuilder = new StringBuilder("svc.card.*.set_pin");
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
                  return this.objectMapper.readValue(msg.getData(), SetPINResponse.class);
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

  public record CardExtras(JsonTime auto_lock) {}

  public record CardFeatures(
      boolean domestic,
      boolean international,
      boolean e_commerce,
      boolean atm,
      boolean pos,
      boolean contactless) {}

  public record CardLimits(
      boolean transaction_enabled,
      int transaction,
      boolean daily_enabled,
      int daily,
      boolean monthly_enabled,
      int monthly,
      boolean yearly_enabled,
      int yearly) {}

  public record JsonTime() {}

  public record RequestCardRequest(
      String account_id,
      String user_id,
      String name,
      int type,
      int use_type,
      CardFeatures features,
      CardLimits limits,
      CardExtras extras) {}

  public record RequestCardResponse(String id) {}

  public record UUID() {}

  public record CardRequestView(
      String id,
      String name,
      int type,
      int use_type,
      String created_at,
      String requested_by,
      String assigned_to,
      CardFeatures features,
      CardLimits limits,
      CardExtras extras,
      String status) {}

  public record ListOrganisationCardRequestsRequest(String account_id, UUIDs user_ids) {}

  public record ListOrganisationCardRequestsResponse(String id, List<CardRequestView> requests) {}

  public record UUIDs() {}

  public record RespondToCardRequestRequest(
      String entity_id, String account_id, String initiator, String request_id, boolean approved) {}

  public record RespondToCardRequestResponse() {}

  public record EditCardRequest(
      String entity_id,
      String card_id,
      String name,
      CardFeatures features,
      CardLimits limits,
      CardExtras extras) {}

  public record EditCardResponse() {}

  public record CardView(
      String id,
      String name,
      int type,
      int use_type,
      String last_4,
      String date_created,
      String user_id,
      String org_id,
      CardFeatures features,
      CardLimits limits,
      CardExtras extras,
      String status) {}

  public record ListOrganisationCardsRequest(String entity_id, String account_id, UUIDs user_ids) {}

  public record ListOrganisationCardsResponse(String id, List<CardView> cards) {}

  public record GetCardDetailsRequest(String account_id, String initiator, String id) {}

  public record GetCardDetailsResponse(String card_number, String expiry_date, String cvv) {}

  public record UpdateCardStatusRequest(
      String account_id, String initiator, String id, String status) {}

  public record UpdateCardStatusResponse() {}

  public record ActivateCardRequest(
      String account_id, String initiator, String id, String last_4) {}

  public record ActivateCardResponse() {}

  public record SetPINRequest(String account_id, String initiator, String id, String pin) {}

  public record SetPINResponse() {}
}
