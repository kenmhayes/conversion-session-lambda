package conversionsession.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandlerUtilsTest {
    @Test
    public void getProxyEventWithCORS_ReturnsEvent() {
        APIGatewayProxyResponseEvent event = HandlerUtils.getProxyEventWithCORS();
        assertEquals(ImmutableMap.of("Access-Control-Allow-Origin", "*"), event.getHeaders());
    }
}
