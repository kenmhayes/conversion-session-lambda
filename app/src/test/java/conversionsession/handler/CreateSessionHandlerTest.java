package conversionsession.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.*;
import conversionsession.model.createsession.ConversionInput;
import conversionsession.model.createsession.CreateSessionInput;
import conversionsession.model.createsession.CreateSessionResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class CreateSessionHandlerTest {
    private static APIGatewayProxyRequestEvent TEST_EVENT = createEvent();
    private static UUID SESSION_ID = UUID.fromString("6dc40d21-e20c-4444-9258-bec4d00c2abd");
    private static Map<String, String> CORS_HEADERS = ImmutableMap.of("Access-Control-Allow-Origin", "*");
    private CreateSessionHandler createSessionHandler;
    private MockedStatic mockHandlerComponent;
    private Context mockContext;
    private LambdaLogger mockLambdaLogger;
    private DynamoDbTable<Session> mockSessionTable;
    private DynamoDbTable<Conversion> mockConversionsTable;
    private ObjectMapper mockObjectMapper;
    private MockedStatic mockUUID;

    @BeforeEach
    public void setupTests() {
        this.mockContext = mock(Context.class);
        this.mockLambdaLogger = mock(LambdaLogger.class);
        this.mockSessionTable = mock(DynamoDbTable.class);
        this.mockConversionsTable = mock(DynamoDbTable.class);
        this.mockObjectMapper = mock(ObjectMapper.class);
        this.mockUUID = mockStatic(UUID.class);

        this.mockHandlerComponent = mockStatic(HandlerComponent.class);
        HandlerComponent mockDaggerHandlerComponent = mock(HandlerComponent.class);

        when(mockDaggerHandlerComponent.sessionDynamoDbTable()).thenReturn(this.mockSessionTable);
        when(mockDaggerHandlerComponent.conversionsDynamoDbTable()).thenReturn(this.mockConversionsTable);
        when(mockDaggerHandlerComponent.objectMapper()).thenReturn(this.mockObjectMapper);
        when(HandlerComponent.create()).thenReturn(mockDaggerHandlerComponent);

        when(this.mockContext.getLogger()).thenReturn(this.mockLambdaLogger);
        when(UUID.randomUUID()).thenReturn(SESSION_ID);

        this.createSessionHandler = new CreateSessionHandler();
    }

    @AfterEach
    public void teardownTests() {
        // Need to close static mocks before setup is run again
        this.mockHandlerComponent.close();
        this.mockUUID.close();
    }

    @Test
    public void handleRequest_DefaultConstructor_InjectsWithDagger() {
        mockHandlerComponent.verify(() -> HandlerComponent.create());
    }

    @Test
    public void handleRequest_ValidInput_CreatesSession() throws IOException {
        when(mockObjectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(
                CreateSessionInput.builder()
                        .conversions(Arrays.asList(
                                ConversionInput.builder()
                                        .fileName("test1.jpg")
                                        .originalFileFormat("jpg")
                                        .convertedFileFormat("png")
                                        .build(),
                                ConversionInput.builder()
                                        .fileName("test2.jpg")
                                        .originalFileFormat("jpg")
                                        .convertedFileFormat("png")
                                        .build()
                        ))
                        .build()
        );
        String responseBodyString = "{\"sessionId\":\"" + SESSION_ID.toString() + "\"}";
        when(mockObjectMapper.writeValueAsString(any(CreateSessionResponseBody.class))).thenReturn(responseBodyString);

        APIGatewayProxyResponseEvent response = this.createSessionHandler.handleRequest(TEST_EVENT, this.mockContext);

        verify(this.mockSessionTable).putItem(any(Session.class));
        verify(this.mockConversionsTable, times(2)).putItem(any(Conversion.class));

        assertEquals(200, response.getStatusCode());
        assertEquals(CORS_HEADERS, response.getHeaders());
        assertEquals(responseBodyString, response.getBody());
    }

    @Test
    public void handleRequest_DynamoDBException_LogsErrorMessage() {
        String errorMessage = "This is an error";
        doThrow(new RuntimeException(errorMessage)).when(this.mockSessionTable).putItem(any(Session.class));
        APIGatewayProxyResponseEvent response = this.createSessionHandler.handleRequest(TEST_EVENT, this.mockContext);

        verify(this.mockLambdaLogger).log(errorMessage);
        assertEquals(200, response.getStatusCode());
        assertEquals(CORS_HEADERS, response.getHeaders());
    }

    private static APIGatewayProxyRequestEvent createEvent() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("");
        return event;
    }
}
