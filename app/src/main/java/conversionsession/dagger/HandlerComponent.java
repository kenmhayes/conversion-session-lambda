package conversionsession.dagger;

import conversionsession.model.Session;
import dagger.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Component(modules = DynamoDBModule.class)
public interface HandlerComponent {
    DynamoDbTable<Session> sessionDynamoDbTable();

    static HandlerComponent create() {
        return DaggerHandlerComponent.create();
    }
}
