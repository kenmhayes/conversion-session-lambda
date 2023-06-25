package conversionsession.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import conversionsession.model.ConversionRequest;
import conversionsession.model.Session;
import dagger.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Component(modules = { DynamoDBModule.class, CoreModule.class })
public interface HandlerComponent {
    DynamoDbTable<Session> sessionDynamoDbTable();
    DynamoDbTable<ConversionRequest> conversionRequestDynamoDbTable();
    ObjectMapper objectMapper();

    static HandlerComponent create() {
        return DaggerHandlerComponent.create();
    }
}
