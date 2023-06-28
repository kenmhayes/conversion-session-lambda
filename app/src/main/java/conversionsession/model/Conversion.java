package conversionsession.model;

import conversionsession.Constants;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.io.Serializable;

@AllArgsConstructor
@Builder
@Data
@DynamoDbBean
@NoArgsConstructor
public class Conversion implements Serializable {
    @Getter(onMethod_=@DynamoDbPartitionKey)
    private String id;
    @Getter(onMethod_=@DynamoDbSecondaryPartitionKey(indexNames = Constants.SESSION_INDEX_NAME))
    private String sessionId;
    private String fileName;
    private String originalFileFormat;
    private String convertedFileFormat;
    private String conversionStatus;
    private long expirationTime;
}
