package telran.net;

import java.util.Scanner;

public class ApplServerTest {
public static void main(String[] args) {
    Protocol protocol = req -> {
        return switch(req.requestType()) {
            case "ok" -> new Response(ResponseCode.OK, "");
            default -> new Response(ResponseCode.WRONG_TYPE, "");
        };
    };
    TcpServer server = new TcpServer(protocol, 5000, 10000);
    new Thread(server).start();
    Scanner scanner = new Scanner(System.in);
    while(true) {
        System.out.println("Enetr shutdown for stopping server");
        String line = scanner.nextLine();
        if(line.equals("shutdown")) {
            server.shutdown();
            break;
        }
    }
}
}
    