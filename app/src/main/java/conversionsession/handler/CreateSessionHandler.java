package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.Session;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.UUID;

public class CreateSessionHandler implements RequestHandler<Map<String, Object>, Void> {
    private DynamoDbTable<Session> sessionDynamoDbTable;
    public CreateSessionHandler() {
        HandlerComponent handlerComponent = HandlerComponent.create();
        this.sessionDynamoDbTable = handlerComponent.sessionDynamoDbTable();
    }
    @Override
    public Void handleRequest(Map<String,Object> event, Context context)
    {
        LambdaLogger logger = context.getLogger();

        try {
            String sessionId = UUID.randomUUID().toString();
            // Get instant of 1 week from now
            Instant expirationTime = Instant.now().plus(Period.ofDays(1));

            Session newSession = new Session();
            newSession.setId(sessionId);
            newSession.setExpirationTime(expirationTime);

            this.sessionDynamoDbTable.putItem(newSession);
        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        return null;
    }
}
