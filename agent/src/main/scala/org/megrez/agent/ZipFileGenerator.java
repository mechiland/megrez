package org.megrez.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileGenerator {
    static String getZipFile(String path, ArrayList<File> artifactFiles, String tags) {
        int b = 0;
        File temFile = null;
        try {
            temFile = File.createTempFile(path, ".zip");
            ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(temFile));
            zipFile.setComment("tag info:" + tags);
            byte[] buf = new byte[1024];
            for (File file : artifactFiles) {
                zipFile.putNextEntry(new ZipEntry(file.getAbsolutePath()));
                FileInputStream fileStream = new FileInputStream(file);
                while ((b = fileStream.read(buf)) > 0) {
                    zipFile.write(buf, 0, b);
                }
                zipFile.closeEntry();
                fileStream.close();
            }
            zipFile.close();
            return temFile.getAbsolutePath();
        } catch (IOException e) {
            return "";
        }
    }
}
