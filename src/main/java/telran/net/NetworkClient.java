package telran.net;

public interface NetworkClient {
String sendAndReceive(String requestType, String requestData);
}