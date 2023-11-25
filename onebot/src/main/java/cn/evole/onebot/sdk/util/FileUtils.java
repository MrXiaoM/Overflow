package cn.evole.onebot.sdk.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/1/1 15:30
 * Version: 1.0
 */
public class FileUtils {

    public static void checkFolder(Path folder){
        if (!folder.toFile().isDirectory()) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






}
