package conversionsession.model;

import java.io.Serializable;
import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Session implements Serializable {
    private String id;
    private String emailAddress;

    private Instant expirationTime;

    public Session() {

    }

    @DynamoDbPartitionKey
    public String getId() { return this.id; }

    public void setId(String id) { this.id = id; }

    public String getEmailAddress() { return this.emailAddress; }

    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public Instant getExpirationTime() { return this.expirationTime; }

    public void setExpirationTime(Instant expirationTime) { this.expirationTime = expirationTime; }
}
