package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.Conversion;
import conversionsession.model.getsession.ConversionOutput;
import conversionsession.model.Session;
import conversionsession.model.getsession.GetSessionResponseBody;
import conversionsession.util.HandlerUtils;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

public class GetSessionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private DynamoDbTable<Session> sessionDynamoDbTable;
    private DynamoDbIndex<Conversion> sessionIdIndex;
    private ObjectMapper objectMapper;

    public GetSessionHandler() {
        HandlerComponent handlerComponent = HandlerComponent.create();
        this.sessionDynamoDbTable = handlerComponent.sessionDynamoDbTable();
        this.sessionIdIndex = handlerComponent.sessionIdIndex();
        this.objectMapper = handlerComponent.objectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent response = HandlerUtils.getProxyEventWithCORS();

        try {
            String sessionId = event.getQueryStringParameters().get("sessionid");

            Key sessionIdKey = Key.builder()
                    .partitionValue(sessionId)
                    .build();
            Session session = sessionDynamoDbTable.getItem(sessionIdKey);

            QueryConditional getConversionsQuery = QueryConditional.keyEqualTo(sessionIdKey);

            SdkIterable<Page<Conversion>> queryResults = sessionIdIndex.query(getConversionsQuery);
            List<ConversionOutput> conversions = queryResults.stream().flatMap(page -> page.items().stream().map(
                            conversion -> ConversionOutput.builder()
                                    .fileName(conversion.getFileName())
                                    .id(conversion.getId())
                                    .conversionStatus(conversion.getConversionStatus())
                                    .build()
                            ))
                .collect(Collectors.toList());

            GetSessionResponseBody responseBody = GetSessionResponseBody.builder()
                    .expirationTime(session.getExpirationTime())
                    .conversions(conversions)
                    .build();

            response.setBody(objectMapper.writeValueAsString(responseBody));
        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        response.setStatusCode(200);
        return response;
    }
}
