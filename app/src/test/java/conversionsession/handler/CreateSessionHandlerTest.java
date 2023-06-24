package conversionsession.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.ConversionRequest;
import conversionsession.model.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

public class CreateSessionHandlerTest {
    private APIGatewayProxyRequestEvent TEST_EVENT = new APIGatewayProxyRequestEvent();
    private CreateSessionHandler createSessionHandler;
    private MockedStatic mockHandlerComponent;
    private Context mockContext;
    private LambdaLogger mockLambdaLogger;
    private DynamoDbTable<Session> mockSessionTable;
    private DynamoDbTable<ConversionRequest> mockConversionRequestTable;

    @BeforeEach
    public void setupTests() {
        this.mockContext = mock(Context.class);
        this.mockLambdaLogger = mock(LambdaLogger.class);
        this.mockSessionTable = mock(DynamoDbTable.class);
        this.mockConversionRequestTable = mock(DynamoDbTable.class);

        this.mockHandlerComponent = mockStatic(HandlerComponent.class);
        HandlerComponent mockDaggerHandlerComponent = mock(HandlerComponent.class);

        when(mockDaggerHandlerComponent.sessionDynamoDbTable()).thenReturn(this.mockSessionTable);
        when(mockDaggerHandlerComponent.conversionRequestDynamoDbTable()).thenReturn(this.mockConversionRequestTable);
        when(HandlerComponent.create()).thenReturn(mockDaggerHandlerComponent);
        when(this.mockContext.getLogger()).thenReturn(this.mockLambdaLogger);

        this.createSessionHandler = new CreateSessionHandler();
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
    public void handleRequest_ValidInput_CreatesSession() {
        APIGatewayProxyResponseEvent response = this.createSessionHandler.handleRequest(TEST_EVENT, this.mockContext);
        verify(this.mockSessionTable).putItem(any(Session.class));
        verify(this.mockConversionRequestTable).putItem(any(ConversionRequest.class));

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void handleRequest_DynamoDBException_LogsErrorMessage() {
        String errorMessage = "This is an error";
        doThrow(new RuntimeException(errorMessage)).when(this.mockSessionTable).putItem(any(Session.class));
        APIGatewayProxyResponseEvent response = this.createSessionHandler.handleRequest(TEST_EVENT, this.mockContext);

        verify(this.mockLambdaLogger).log(errorMessage);
        assertEquals(200, response.getStatusCode());
    }
}
