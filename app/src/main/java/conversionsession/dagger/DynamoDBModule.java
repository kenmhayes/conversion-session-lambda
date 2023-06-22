package conversionsession.dagger;

import conversionsession.model.Session;
import dagger.Module;
import dagger.Provides;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Module
public abstract class DynamoDBModule {

    @Provides
    public static DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder().build();
    }

    @Provides
    public static DynamoDbEnhancedClient getDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Provides
    public static TableSchema<Session> getSessionTableSchema() {
        return TableSchema.fromBean(Session.class);
    }

    @Provides
    public static DynamoDbTable<Session> getSessionTable(
            DynamoDbEnhancedClient dynamoDbEnhancedClient, TableSchema<Session> sessionTableSchema
    ) {
        return dynamoDbEnhancedClient.table(System.getenv().get("SessionTableName"), sessionTableSchema);
    }
}
