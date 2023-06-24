package conversionsession.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class Response {
    private String statusCode;
    private Map<String, String> headers;
    private String body;
}
