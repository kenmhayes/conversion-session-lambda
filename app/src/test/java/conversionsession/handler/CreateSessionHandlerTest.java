package conversionsession.handler;

import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.common.collect.Maps;
import conversionsession.dagger.HandlerComponent;
import conversionsession.model.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class CreateSessionHandlerTest {
    private CreateSessionHandler createSessionHandler;
    private MockedStatic mockHandlerComponent;
    private Context mockContext;
    private LambdaLogger mockLambdaLogger;
    private DynamoDbTable<Session> mockSessionTable;

    @BeforeEach
    public void setupTests() {
        this.mockContext = mock(Context.class);
        this.mockLambdaLogger = mock(LambdaLogger.class);
        this.mockSessionTable = mock(DynamoDbTable.class);

        this.mockHandlerComponent = mockStatic(HandlerComponent.class);
        HandlerComponent mockDaggerHandlerComponent = mock(HandlerComponent.class);

        when(mockDaggerHandlerComponent.sessionDynamoDbTable()).thenReturn(this.mockSessionTable);
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
        this.createSessionHandler.handleRequest(Maps.newHashMap(), this.mockContext);
        verify(this.mockSessionTable).putItem(any(Session.class));
    }

    @Test
    public void handleRequest_DynamoDBException_LogsErrorMessage() {
        String errorMessage = "This is an error";
        doThrow(new RuntimeException(errorMessage)).when(this.mockSessionTable).putItem(any(Session.class));
        this.createSessionHandler.handleRequest(Maps.newHashMap(), this.mockContext);

        verify(this.mockLambdaLogger).log(errorMessage);
    }
}
