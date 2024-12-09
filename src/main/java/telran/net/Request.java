package telran.net;

import org.json.*;
import static telran.net.TcpConfigurationProperties.*;
public record Request(String requestType, String requestData) {
    @Override
    public String toString(){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(REQUEST_TYPE_FIELD, requestType);
        jsonObj.put(REQUEST_DATA_FIELD, requestData);
        return jsonObj.toString();
    }
}