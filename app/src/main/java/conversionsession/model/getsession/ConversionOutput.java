package conversionsession.model.getsession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionOutput {
    private String id;
    private String fileName;
    private String conversionStatus;
    private String originalFileFormat;
    private String convertedFileFormat;
}
