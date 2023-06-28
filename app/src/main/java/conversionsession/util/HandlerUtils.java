package conversionsession.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class HandlerUtils {
    public static APIGatewayProxyResponseEvent getProxyEventWithCORS() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(Maps.newHashMap());
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        return response;
    }
}
