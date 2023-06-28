package conversionsession.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.Conversion;
import conversionsession.model.Session;
import conversionsession.model.getsession.GetSessionInput;
import conversionsession.model.getsession.GetSessionResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class GetSessionHandlerTest {
    private static Map<String, String> CORS_HEADERS = ImmutableMap.of("Access-Control-Allow-Origin", "*");
    private static String SESSION_ID = "6dc40d21-e20c-4444-9258-bec4d00c2abd";
    private static long EXPIRATION_TIME = 1234567L;
    private static APIGatewayProxyRequestEvent TEST_EVENT = createEvent();
    private GetSessionHandler getSessionHandler;
    private MockedStatic mockHandlerComponent;
    private Context mockContext;
    private LambdaLogger mockLambdaLogger;
    private DynamoDbTable<Session> mockSessionTable;
    private DynamoDbIndex<Conversion> mockSessionIdIndex;
    private ObjectMapper mockObjectMapper;

    @BeforeEach
    public void setupTests() throws IOException {
        this.mockContext = mock(Context.class);
        this.mockLambdaLogger = mock(LambdaLogger.class);
        this.mockSessionTable = mock(DynamoDbTable.class);
        this.mockSessionIdIndex = mock(DynamoDbIndex.class);
        this.mockObjectMapper = mock(ObjectMapper.class);

        this.mockHandlerComponent = mockStatic(HandlerComponent.class);
        HandlerComponent mockDaggerHandlerComponent = mock(HandlerComponent.class);

        when(mockDaggerHandlerComponent.sessionDynamoDbTable()).thenReturn(this.mockSessionTable);
        when(mockDaggerHandlerComponent.sessionIdIndex()).thenReturn(this.mockSessionIdIndex);
        when(mockDaggerHandlerComponent.objectMapper()).thenReturn(this.mockObjectMapper);
        when(HandlerComponent.create()).thenReturn(mockDaggerHandlerComponent);

        when(this.mockContext.getLogger()).thenReturn(this.mockLambdaLogger);

        when(mockObjectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(
                GetSessionInput.builder()
                        .sessionId(SESSION_ID)
                        .build()
        );

        this.getSessionHandler = new GetSessionHandler();
    }

    @AfterEach
    public void teardownTests() {
        // Need to close static mocks before setup is run again
        this.mockHandlerComponent.close();
    }

    @Test
    public void handleRequest_DefaultConstructor_InjectsWithDagger() {
        mockHandlerComponent.verify(() -> HandlerComponent.create());
    }

    @Test
    public void handleRequest_ValidInput_GetSession() throws IOException {
        Conversion conversion1 = Conversion.builder()
                .fileName("test1.jpg")
                .id("123")
                .conversionStatus("Done")
                .build();
        List<Conversion> conversions = Arrays.asList(conversion1);
        Page<Conversion> page = Page.create(conversions);
        List<Page<Conversion>> pages = Arrays.asList(page);

        String responseBody = "TEST RESPONSE BODY";

        SdkIterable<Page<Conversion>> mockSdkIterable = mock(SdkIterable.class);
        when(mockSdkIterable.stream()).thenReturn(pages.stream());

        when(mockSessionTable.getItem(any(Key.class))).thenReturn(Session.builder().expirationTime(EXPIRATION_TIME).build());

        when(mockSessionIdIndex.query(any(QueryConditional.class))).thenReturn(mockSdkIterable);
        when(mockObjectMapper.writeValueAsString(any(GetSessionResponseBody.class))).thenReturn(responseBody);

        APIGatewayProxyResponseEvent response = this.getSessionHandler.handleRequest(TEST_EVENT, this.mockContext);
        assertEquals(200, response.getStatusCode());
        assertEquals(CORS_HEADERS, response.getHeaders());
        verify(mockObjectMapper).writeValueAsString(any(GetSessionResponseBody.class));
        assertEquals(responseBody, response.getBody());
    }

    @Test
    public void handleRequest_DynamoDBException_LogsErrorMessage() {
        String errorMessage = "This is an error";
        doThrow(new RuntimeException(errorMessage)).when(this.mockSessionTable).getItem(any(Key.class));
        APIGatewayProxyResponseEvent response = this.getSessionHandler.handleRequest(TEST_EVENT, this.mockContext);

        verify(this.mockLambdaLogger).log(errorMessage);
        assertEquals(200, response.getStatusCode());
        assertEquals(CORS_HEADERS, response.getHeaders());
    }

    private static APIGatewayProxyRequestEvent createEvent() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("");
        event.setQueryStringParameters(ImmutableMap.of("sessionid", SESSION_ID));
        return event;
    }
}
