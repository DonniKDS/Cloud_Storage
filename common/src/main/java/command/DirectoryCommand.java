package command;

import java.io.Serializable;

public class DirectoryCommand implements Serializable {
    private final String directory;
    private final int clientNumber;

    public DirectoryCommand(String dir, int num) {
        this.directory = dir;
        this.clientNumber = num;
    }

    public String getDirectory() {
        return directory;
    }

    public int getClientNum() {
        return clientNumber;
    }
}
