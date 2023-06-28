package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.*;
import conversionsession.model.createsession.ConversionInput;
import conversionsession.model.createsession.CreateSessionInput;
import conversionsession.model.createsession.CreateSessionResponseBody;
import conversionsession.util.HandlerUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.UUID;

public class CreateSessionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private DynamoDbTable<Session> sessionDynamoDbTable;
    private DynamoDbTable<Conversion> conversionDynamoDbTable;
    private ObjectMapper objectMapper;

    public CreateSessionHandler() {
        HandlerComponent handlerComponent = HandlerComponent.create();
        this.sessionDynamoDbTable = handlerComponent.sessionDynamoDbTable();
        this.conversionDynamoDbTable = handlerComponent.conversionsDynamoDbTable();
        this.objectMapper = handlerComponent.objectMapper();
    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent response = HandlerUtils.getProxyEventWithCORS();

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

            Map<String, String> fileNameToId = Maps.newHashMap();
            for (ConversionInput input : createSessionInput.getConversions()) {
                String conversionId = UUID.randomUUID().toString();
                String fileName = input.getFileName();

                Conversion newConversion = Conversion.builder()
                        .id(conversionId)
                        .sessionId(sessionId)
                        .fileName(fileName)
                        .originalFileFormat(input.getOriginalFileFormat())
                        .convertedFileFormat(input.getConvertedFileFormat())
                        .conversionStatus(ConversionStatus.NOT_STARTED.name())
                        .expirationTime(expirationTime)
                        .build();

                this.conversionDynamoDbTable.putItem(newConversion);
                fileNameToId.put(fileName, conversionId);
            }

            response.setBody(objectMapper.writeValueAsString(
                    CreateSessionResponseBody.builder()
                            .sessionId(sessionId)
                            .fileNameToId(fileNameToId)
                            .build()
            ));
        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        response.setStatusCode(200);
        return response;
    }
}
