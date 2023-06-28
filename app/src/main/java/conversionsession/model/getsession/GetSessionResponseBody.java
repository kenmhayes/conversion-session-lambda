package conversionsession.model.getsession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetSessionResponseBody implements Serializable {
    private long expirationTime;
    private List<ConversionOutput> conversions;
}
