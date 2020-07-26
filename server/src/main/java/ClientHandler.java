import command.Command;
import command.DownUpLoadCommand;
import command.FileListCommand;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private static final String SERVER_ROOT = "./bd/server/";
    private static final int BUFFER_SIZE = 8192;
    private final Socket socket;
    private final int clientNum;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ExecutorService executorService;
    private String clientHomeDir;

    public ClientHandler(Socket socket, int clientNum) {
        this.socket = socket;
        this.clientNum = clientNum;
    }

    public void run() {
        doHandle(socket);
    }

    private void doHandle(Socket socket) {
        try {
            in = new ObjectInputStream((socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
            executorService = Executors.newFixedThreadPool(1);
            changeClientHomeDir();
            executorService.execute(() -> {
                try {
                    readClientCommand();
                } catch (IOException e) {
                    System.out.println("Соединение с клиентом закрыто.");
                    NetworkServer.disconnect();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            System.out.println("Соединение разорвано");
        } finally {
            executorService.shutdown();
        }
    }

    private void readClientCommand() throws IOException {
        while (true) {
            Command command = readCommands();
            if(command == null) continue;
            switch (command.getType()) {
                case GET_FILE_LIST: {
                    FileListCommand commandData = (FileListCommand) command.getData();
                    sendCommand(Command.FileListPutCommand(commandData.getDir().listFiles()));
                    break;
                }
                case DOWNLOAD: {
                    DownUpLoadCommand commandData = (DownUpLoadCommand) command.getData();
                    if (commandData.getFile().exists()) {
                        sendFileToClient(commandData);
                    } else {
                        out.writeBoolean(false);
                        out.flush();
                    }
                    break;
                }
                case UPLOAD: {
                    DownUpLoadCommand commandData = (DownUpLoadCommand) command.getData();
                    getFileFromClient(commandData);
                    break;
                }
            }
        }
    }

    private void getFileFromClient(DownUpLoadCommand commandData) throws IOException {
        File localFile = new File(clientHomeDir + "/" + commandData.getFile().getName());
        FileOutputStream outFile = new FileOutputStream(localFile);
        byte[] bytes = new byte[BUFFER_SIZE];
        long sizeRemote = in.readLong();
        long sizeLocal;
        int bytesRead;

        while (true) {
            sizeLocal = localFile.length();
            if (sizeLocal == sizeRemote) break;
            bytesRead = in.read(bytes);
            outFile.write(bytes, 0, bytesRead);
        }
        outFile.close();
    }

    private void sendFileToClient(DownUpLoadCommand commandData) throws IOException {
        out.writeBoolean(true);
        out.flush();
        out.writeLong(commandData.getFile().length());
        out.flush();
        FileInputStream fileStream = new FileInputStream(commandData.getFile());
        byte[] bytes = new byte[8192];
        int count;
        while ((count = fileStream.read(bytes)) > 0) {
            out.write(bytes, 0, count);
            out.flush();
        }
        fileStream.close();
    }

    private Command readCommands() throws IOException {
        try {
            return (Command) in.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("Неизвестный тип объекта от клиента.");
            return null;
        }
    }

    private void sendCommand(Command command) throws IOException {
        out.writeObject(command);
    }

    private void changeClientHomeDir() throws IOException {
        clientHomeDir = SERVER_ROOT + "client" + clientNum;
        File dir = new File(clientHomeDir);
        if (dir.mkdir()) {
            System.out.println("Папка пользователя не существует. Создание.");
        } else {
            System.out.println("Клиент подключен к папке: " + clientHomeDir);
        }
        sendCommand(Command.setDirectoryCommand(clientHomeDir, clientNum));
    }
}
