package command;

import java.io.File;
import java.io.Serializable;

public class DownUpLoadCommand implements Serializable {
    private final File file;

    public DownUpLoadCommand(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
