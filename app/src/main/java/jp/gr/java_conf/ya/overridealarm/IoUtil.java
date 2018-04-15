package jp.gr.java_conf.ya.overridealarm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class IoUtil {
    private static final String filePath = "loc_log.txt";

    public static void appendText(String text) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(filePath), true);
            fos .write(text.getBytes());
        } catch (IOException e) {
        } catch (Exception e) {
        }
    }
}
