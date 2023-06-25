package conversionsession.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@Builder
@Data
@DynamoDbBean
@NoArgsConstructor
public class ConversionRequest implements Serializable {
    private String id;
    private String sessionId;
    private String fileName;
    private String originalFileFormat;
    private String convertedFileFormat;
    private String conversionStatus;
    private long expirationTime;

    @DynamoDbPartitionKey
    public String getId() { return this.id; }
}
