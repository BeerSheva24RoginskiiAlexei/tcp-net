package telran.net;
import java.net.*;
import org.json.JSONObject;
import java.io.*;

public class TcpClientServerSession implements Runnable {
    private static final int IDLE_TIMEOUT = 5000; 
    private static final int MAX_REQUESTS_PER_SECOND = 10;
    private static final int MAX_WRONG_RESPONSES = 5;

    private Protocol protocol;
    private Socket socket;
    private int requestCount = 0;
    private int wrongResponseCount = 0;

    public TcpClientServerSession(Protocol protocol, Socket socket) throws SocketException {
        this.protocol = protocol;
        this.socket = socket;
        this.socket.setSoTimeout(IDLE_TIMEOUT);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintStream writer = new PrintStream(socket.getOutputStream())) {

            long startTime = System.currentTimeMillis();
            String request;

            while ((request = reader.readLine()) != null) {
                long currentTime = System.currentTimeMillis();
                requestCount++;

                if (requestCount / ((currentTime - startTime) / 1000.0) > MAX_REQUESTS_PER_SECOND) {
                    System.out.println("Too many requests per second. Closing connection.");
                    break;
                }

                String responseJSON = protocol.getResponseWithJSON(request);
                Response response = parseResponse(responseJSON);

                if (response.responseCode() != ResponseCode.OK) {
                    wrongResponseCount++;
                }

                if (wrongResponseCount > MAX_WRONG_RESPONSES) {
                    System.out.println("Too many wrong responses. Closing connection.");
                    break;
                }

                writer.println(responseJSON);
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Idle timeout reached. Closing connection.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response parseResponse(String responseJSON) {
        JSONObject jsonObj = new JSONObject(responseJSON);
        ResponseCode responseCode = jsonObj.getEnum(ResponseCode.class, TcpConfigurationProperties.RESPONSE_CODE_FIELD);
        String responseData = jsonObj.getString(TcpConfigurationProperties.RESPONSE_DATA_FIELD);
        return new Response(responseCode, responseData);
    }
}
