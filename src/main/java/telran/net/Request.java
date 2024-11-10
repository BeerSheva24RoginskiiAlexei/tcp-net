package telran.net;

import org.json.*;
import static telran.net.TcpConfigurationProperties.*;
public record Request(String requestType, String requestData) {
    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put(REQUEST_TYPE_FIELD, requestType);
        json.put(REQUEST_DATA_FIELD, requestData);
        return json.toString();
    }
}