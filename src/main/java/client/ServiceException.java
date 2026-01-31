package client;

import io.nats.client.Message;
import io.nats.service.ServiceMessage;
import java.util.Optional;

public class ServiceException extends RuntimeException {
  private final int code;

  public ServiceException(String message, int code) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static Optional<ServiceException> fromMessage(Message msg) {
    if (msg.hasHeaders()) {
      String serviceErrorCode = msg.getHeaders().getFirst(ServiceMessage.NATS_SERVICE_ERROR_CODE);
      if (serviceErrorCode != null && !serviceErrorCode.isEmpty()) {
        int code = 400;
        try {
          code = Integer.parseInt(serviceErrorCode);
        } catch (NumberFormatException e) {
          // Default to 400
        }
        String serviceError = msg.getHeaders().getFirst(ServiceMessage.NATS_SERVICE_ERROR);
        if (serviceError == null || serviceError.isEmpty()) {
          serviceError = serviceErrorCode;
        }
        return Optional.of(new ServiceException(serviceError, code));
      }
    }
    return Optional.empty();
  }
}
