package conversionsession.model.createsession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionInput implements Serializable  {
    private String fileName;
    private String originalFileFormat;
    private String convertedFileFormat;
}
