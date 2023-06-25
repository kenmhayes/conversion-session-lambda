package conversionsession.model;

import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@AllArgsConstructor
@Builder
@Data
@DynamoDbBean
@NoArgsConstructor
public class Session implements Serializable {
    private String id;
    private String emailAddress;
    private long expirationTime;

    @DynamoDbPartitionKey
    public String getId() { return this.id; }
}
