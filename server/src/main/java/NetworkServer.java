import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServer {

    private final  int port;
    private static int clientNum = 0;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Сервер запущен");
            while (true) {
                System.out.println("Ожидается подлючение клиента");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                createClientHandler(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createClientHandler(Socket socket) {
        clientNum += 1;
        ClientHandler clientHandler = new ClientHandler(socket, clientNum);
        clientHandler.run();
    }

    public static void disconnect() {
        clientNum -= 1;
    }


}
