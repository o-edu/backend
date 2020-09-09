package net.oedu.backend.base.upload;

import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class UploadFile {

    @Getter
    private File file;
    @Getter
    private FileOutputStream fos = null;
    @Getter
    private final String uuid;

    public UploadFile(final File file) throws FileNotFoundException {
        this.fos = new FileOutputStream(file);
        this.file = file;
        this.uuid = file.getName();
    }

    public UploadFile(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * creates the {@link FileOutputStream}.
     * @param file the file for the {@link FileOutputStream}
     */
    public void create(final File file) throws FileNotFoundException {
        if (fos == null) {
            fos = new FileOutputStream(file);
        }
    }
}
