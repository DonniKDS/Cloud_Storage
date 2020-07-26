import java.io.IOException;

public class Client {

    public static void main(String[] args) {
        try {
            new NetworkClient("localhost", 8189).connect();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка подключения.");
        }
    }
}
