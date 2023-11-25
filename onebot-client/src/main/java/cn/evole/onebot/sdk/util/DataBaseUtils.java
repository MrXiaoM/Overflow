package cn.evole.onebot.sdk.util;

import lombok.val;

import java.util.zip.CRC32;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/2 22:31
 * Version: 1.0
 */
public class DataBaseUtils {

    public static int toMessageId(int[] from, long botId, long contactId){
        val crc = new CRC32();
        String messageId = botId + "-" + contactId;
        crc.update(messageId.getBytes());
        return (int) crc.getValue();
    }

}
