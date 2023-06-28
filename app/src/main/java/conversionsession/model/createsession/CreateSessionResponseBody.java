package conversionsession.model.createsession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponseBody {
    private String sessionId;
    private Map<String, String> fileNameToId;
}
