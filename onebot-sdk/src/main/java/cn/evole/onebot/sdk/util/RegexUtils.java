package cn.evole.onebot.sdk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 13:49
 * Version: 1.0
 */
public class RegexUtils {
    /**
     * 消息正则匹配
     *
     * @param regex 正则表达式
     * @param text  匹配内容
     * @return Matcher
     */
    public static Matcher regexMatcher(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            return matcher;
        } else {
            return null;
        }
    }
}
