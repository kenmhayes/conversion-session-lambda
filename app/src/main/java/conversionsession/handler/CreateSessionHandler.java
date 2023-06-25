package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.*;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.Instant;
import java.time.Period;
import java.util.UUID;

public class CreateSessionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private DynamoDbTable<Session> sessionDynamoDbTable;
    private DynamoDbTable<ConversionRequest> conversionRequestDynamoDbTable;
    private ObjectMapper objectMapper;

    public CreateSessionHandler() {
        HandlerComponent handlerComponent = HandlerComponent.create();
        this.sessionDynamoDbTable = handlerComponent.sessionDynamoDbTable();
        this.conversionRequestDynamoDbTable = handlerComponent.conversionRequestDynamoDbTable();
        this.objectMapper = handlerComponent.objectMapper();
    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            CreateSessionInput createSessionInput = objectMapper.readValue(event.getBody(), CreateSessionInput.class);

            String sessionId = UUID.randomUUID().toString();
            // Get instant of 1 week from now
            long expirationTime = Instant.now().plus(Period.ofDays(1)).getEpochSecond();

            Session newSession = Session.builder()
                    .id(sessionId)
                    .expirationTime(expirationTime)
                    .build();

            this.sessionDynamoDbTable.putItem(newSession);

            for (ConversionRequestInput input : createSessionInput.getConversionRequests()) {
                ConversionRequest newConversionRequest = ConversionRequest.builder()
                        .id(UUID.randomUUID().toString())
                        .sessionId(sessionId)
                        .fileName(input.getFileName())
                        .originalFileFormat(input.getOriginalFileFormat())
                        .convertedFileFormat(input.getConvertedFileFormat())
                        .conversionStatus(ConversionStatus.NOT_STARTED.name())
                        .expirationTime(expirationTime)
                        .build();

                this.conversionRequestDynamoDbTable.putItem(newConversionRequest);

                response.setBody(objectMapper.writeValueAsString(
                        CreateSessionResponseBody.builder().sessionId(sessionId).build()
                ));
            }
        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        response.setStatusCode(200);
        response.setHeaders(ImmutableMap.of("Access-Control-Allow-Origin", "*"));
        return response;
    }
}
