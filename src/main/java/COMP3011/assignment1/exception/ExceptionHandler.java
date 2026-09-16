package COMP3011.assignment1.exception;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
@RestControllerAdvice
public class ExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(ExceptionHandler.class);
    @org.springframework.web.bind.annotation.ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        logger.warn(
                "Bad request to {}: {}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        createError(
                                400,
                                "Bad Request",
                                exception.getMessage(),
                                request.getRequestURI()
                        )
                );
    }
    @org.springframework.web.bind.annotation.ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {

        logger.error(
                "Server error while processing {}: {}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        createError(
                                500,
                                "Internal Server Error",
                                "An unexpected server error occurred.",
                                request.getRequestURI()
                        )
                );
    }
    private Map<String, Object> createError(
            int status,
            String error,
            String message,
            String path) {

        Map<String, Object> response =
                new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        response.put("path", path);

        return response;
    }
}

