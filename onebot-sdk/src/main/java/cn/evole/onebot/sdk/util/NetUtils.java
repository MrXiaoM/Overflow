package cn.evole.onebot.sdk.util;

import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 13:50
 * Version: 1.0
 */
public class NetUtils {
    /**
     * get request
     *
     * @param url         url
     * @param charsetName charset
     * @return {@link String}
     */
    public static String get(String url, String charsetName) {
        val result = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            val connection = new URL(url).openConnection();
            connection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charsetName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
