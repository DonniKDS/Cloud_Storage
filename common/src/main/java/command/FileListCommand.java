package command;

import java.io.File;
import java.io.Serializable;

public class FileListCommand implements Serializable {
    private File dir;
    private File[] fileList;

    public FileListCommand(File[] fileList) {
        this.fileList = fileList;
    }

    public FileListCommand(File dir) {
        this.dir = dir;
    }

    public File[] getFileList() {
        return fileList;
    }

    public File getDir() {
        return dir;
    }
}
