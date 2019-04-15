package com.smallcold.hosts.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by smallcold on 2017/9/1.
 */
public class IPDomainUtil {

    public static String SELF_IP = "127.0.0.1";
    public static long SELF_IP_LONG = ipToLong(SELF_IP);

    private static final Pattern IP_PATTERN = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d))\\.){3}"
            + "(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d)))");

    /**
     * 域名最少由两部分组成 xxx.xxx，但是本地域名可以是任意的
     */
    private static final Pattern DOMAIN_PATTERN = Pattern
            .compile("(?:((?:[a-z0-9](?:[a-z0-9\\-]{0,61}[a-z0-9]?\\.))+[a-z]{2,6})\\s*)+?",
                    Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES);

    private static final Pattern SELF_DOMAIN_PATTERN = Pattern
            .compile("(?:((?:[a-z0-9](?:[a-z0-9\\-]{0,61}[a-z0-9]?\\.))*[a-z]{2,})\\s*)+?",
                    Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES);

    public static List<String> getDomainList(String input) {
        Matcher domainMatcher = DOMAIN_PATTERN.matcher(input);
        List<String> domainList = Lists.newArrayList();
        while (domainMatcher.find()) {
            domainList.add(domainMatcher.group(1));
        }
        return domainList;
    }

    public static List<String> getSelfDomainList(String input) {
        // 本地域名
        Matcher domainMatcher = SELF_DOMAIN_PATTERN.matcher(input);
        List<String> domainList = Lists.newArrayList();
        while (domainMatcher.find()) {
            domainList.add(domainMatcher.group(1));
        }
        return domainList;
    }

    public static String getIPText(String text) {
        Matcher matcher = IP_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static long getIPLong(String text) {
        return ipToLong(getIPText(text));
    }

    /**
     * 长整型转ip格式字符串
     *
     * @param longIp
     * @return
     */
    public static String longToIP(long longIp) {
        if (longIp < 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i > 0; i--) {
            sb.append(String.valueOf((longIp >> i * 8 & 0x000000FF)));
            sb.append(".");
        }
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    /**
     * ip格式字符串转长整型
     *
     * @param strIp
     * @return
     */
    public static long ipToLong(String ipStr) {
        if (StringUtils.isBlank(ipStr)) {
            return -1;
        }
        long ip = 0;
        String[] attrs = ipStr.split("\\.");
        if (attrs.length != 4) {
            throw new RuntimeException("IP 格式错误 ipStr=" + ipStr);
        }
        for (String ipByte : attrs) {
            ip = ip << 8 | Integer.parseInt(ipByte);
        }
        return ip;
    }

    public static boolean isDomain(String input) {
        return DOMAIN_PATTERN.matcher(input).matches();
    }

    public static boolean isSelfDomain(String input) {
        return SELF_DOMAIN_PATTERN.matcher(input).matches();
    }

    public static boolean isIp(String input) {
        return IP_PATTERN.matcher(input).matches();
    }
}
