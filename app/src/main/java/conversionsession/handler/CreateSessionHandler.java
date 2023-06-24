package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.ConversionRequest;
import conversionsession.model.ConversionStatus;
import conversionsession.model.Session;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public class CreateSessionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private DynamoDbTable<Session> sessionDynamoDbTable;
    private DynamoDbTable<ConversionRequest> conversionRequestDynamoDbTable;

    public CreateSessionHandler() {
        HandlerComponent handlerComponent = HandlerComponent.create();
        this.sessionDynamoDbTable = handlerComponent.sessionDynamoDbTable();
        this.conversionRequestDynamoDbTable = handlerComponent.conversionRequestDynamoDbTable();
    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();

        try {
            String sessionId = UUID.randomUUID().toString();
            // Get instant of 1 week from now
            Instant expirationTime = Instant.now().plus(Period.ofDays(1));

            Session newSession = Session.builder()
                    .id(sessionId)
                    .expirationTime(expirationTime)
                    .build();

            this.sessionDynamoDbTable.putItem(newSession);

            ConversionRequest newConversionRequest = ConversionRequest.builder()
                    .id(UUID.randomUUID().toString())
                    .sessionId(sessionId)
                    .fileName("test.jpg")
                    .originalFileFormat("jpg")
                    .convertedFileFormat("png")
                    .conversionStatus(ConversionStatus.COMPLETED.name())
                    .expirationTime(expirationTime)
                    .build();

            this.conversionRequestDynamoDbTable.putItem(newConversionRequest);
        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        return response;
    }
}
