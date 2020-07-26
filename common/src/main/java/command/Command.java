package command;

import java.io.File;
import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private Object data;

    public CommandType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public static Command fileListGetCommand(File dir) {
        Command command = new Command();
        command.type = CommandType.GET_FILE_LIST;
        command.data = new FileListCommand(dir);
        return command;
    }

    public static Command FileListPutCommand(File[] fileList) {
        Command command = new Command();
        command.type = CommandType.PUT_FILE_LIST;
        command.data = new FileListCommand(fileList);
        return command;
    }

    public static Command setDirectoryCommand(String dir, int num) {
        Command command = new Command();
        command.type = CommandType.HOME;
        command.data = new DirectoryCommand(dir, num);
        return command;
    }

    public static Command downloadCommand(File file) {
        Command command = new Command();
        command.type = CommandType.DOWNLOAD;
        command.data = new DownUpLoadCommand(file);
        return command;
    }

    public static Command uploadCommand(File file) {
        Command command = new Command();
        command.type = CommandType.UPLOAD;
        command.data = new DownUpLoadCommand(file);
        return command;
    }
}
