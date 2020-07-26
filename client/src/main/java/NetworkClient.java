import command.Command;
import command.DirectoryCommand;
import command.FileListCommand;

import java.io.*;
import java.net.Socket;

public class NetworkClient {

    private static final int BUFFER_SIZE = 8192;
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private File serverHomeDir;
    private File clientHomeDir;

    public NetworkClient(String serverName, int serverPort) {
        this.host = serverName;
        this.port = serverPort;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        getCommand();
        runReadThread();
        printInfo();
    }

    private void runReadThread() {
        new Thread(() -> {
            while(true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    String[] commandPart = reader.readLine().split(" ", 2);
                    if (commandPart.length > 0 & commandPart[0].equals("/file_list")) {
                        sendCommand(Command.fileListGetCommand(serverHomeDir));
                        getCommand();
                    } else if (commandPart.length > 1 & commandPart[0].equals("/download")) {
                        String fileName = commandPart[1];
                        sendCommand(Command.downloadCommand(new File(serverHomeDir +"\\"+fileName)));
                        getFileFromServer(fileName);
                    } else if (commandPart.length > 1 & commandPart[0].equals("/upload")) {
                        String fileName = commandPart[1];
                        File inFile = new File(clientHomeDir +"\\"+fileName);
                        if (inFile.exists()) {
                            sendCommand(Command.uploadCommand(inFile));
                            putFileToServer(inFile);
                        } else {
                            System.out.println("Файл не найден!");
                        }
                    } else if (commandPart[0].equals("/help")){
                        printInfo();
                    } else if (commandPart[0].equals("/exit")){
                        System.exit(0);
                    } else {
                        System.out.println("Неизвестная команда");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void putFileToServer(File file) throws IOException {
        long sizeLocal = file.length();
        out.writeLong(sizeLocal);
        out.flush();
        FileInputStream fileStream = new FileInputStream(file);
        byte[] bytes = new byte[8192];
        int bytesRead;
        int count = (int) (sizeLocal / BUFFER_SIZE) / 10;
        int readBuckets = 0;
        System.out.print("|");
        while ((bytesRead = fileStream.read(bytes)) > 0) {
            readBuckets++;
            if (count > 0) {
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
            }
            out.write(bytes, 0, bytesRead);
            out.flush();
        }
        System.out.println("|");
        fileStream.close();
    }

    private void getFileFromServer(String fileName) throws IOException {
        if (in.readBoolean()) {
            System.out.println(clientHomeDir.mkdir() ? "Создана папка" + clientHomeDir : "");
            File localFile = new File(clientHomeDir + "/" + fileName);
            FileOutputStream outFile = new FileOutputStream(localFile);

            byte[] bytes = new byte[BUFFER_SIZE];
            long sizeRemote = in.readLong();
            long sizeLocal;
            int bytesRead;
            int count = (int) (sizeRemote / BUFFER_SIZE) / 10;
            int readBuckets = 0;

            System.out.print("|");
            while (true) {
                sizeLocal = localFile.length();
                if (sizeLocal == sizeRemote) break;
                bytesRead = in.read(bytes);
                readBuckets++;
                if (count > 0) {
                    if (readBuckets % count == 0) {
                        System.out.print("=");
                    }
                }
                outFile.write(bytes, 0, bytesRead);
            }
            System.out.println("|");
            outFile.close();
        } else {
            System.out.println("Файл не найден на сервере!");
        }
    }

    private void printInfo() {
        System.out.println("Команды клиента:\n" +
                "/file_list - список файлов в домашней директории на сервере\n" +
                "/download <filename> - скачать файл\n" +
                "/upload <filename> - загрузить файл на сервер\n" +
                "/help - вывести эту справку\n" +
                "/exit - выход из программы");
    }

    private void sendCommand(Command command) throws IOException {
        out.writeObject(command);
    }

    private void getCommand() throws IOException {
        try {
            Command command = (Command) in.readObject();
            switch (command.getType()) {
                case HOME: {
                    DirectoryCommand commandData = (DirectoryCommand) command.getData();
                    serverHomeDir =  new File(commandData.getDirectory());
                    String clientName = "client" + commandData.getClientNum();
                    clientHomeDir = new File("./bd/clients/" + clientName);
                    break;
                }
                case PUT_FILE_LIST: {
                    FileListCommand commandData = (FileListCommand) command.getData();
                    File[] fileList = commandData.getFileList();
                    for (File file : fileList) {
                        System.out.println(file.getName());
                    }
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Получен неизвестный объект");
        }
    }
}
