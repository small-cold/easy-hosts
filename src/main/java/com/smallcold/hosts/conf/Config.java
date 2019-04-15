package com.smallcold.hosts.conf;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.smallcold.hosts.utils.SystemUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by smallcold on 2017/9/3.
 */
public class Config {

    private static final Logger logger = Logger.getLogger(Config.class);

    private static final Pattern configPattern = Pattern.compile("(\\{[\\S\\s]*})");

    private static String adminPassword;

    /**
     * 工作目录
     */
    private static Path workPath;

    /**
     * 系统配置属性
     */
    private static Properties sysProperties = new Properties();

    /**
     * 用户配置文件
     */
    @Getter
    private static File userSettingFile;

    /**
     * 缓存文件夹
     */
    private static File cacheFile;

    /**
     * hosts可选文件夹
     */
    private static File hostsFile;

    static {
        try {
            sysProperties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.error("读取属性文件异常", e);
        }
        String workPathStr = sysProperties.getProperty("work-name");
        if (StringUtils.isBlank(workPathStr)) {
            workPathStr = "~/.config/EasyHosts/";
        }
        if (workPathStr.startsWith("~")) {
            workPathStr = workPathStr.replaceFirst("^~", System.getProperties().getProperty("user.home"));
        }
        workPath = Paths.get(workPathStr);

        // 读取用户配置文件
        userSettingFile = new File(Config.workPath.toFile(), sysProperties.getProperty("user-config-file"));
        cacheFile = new File(Config.workPath.toFile(), sysProperties.getProperty("cache-file"));
        hostsFile = new File(workPath.toFile(), "files");
        init();
    }

    private static void init(){
        if (!workPath.toFile().exists()) {
            boolean result = Config.workPath.toFile().mkdirs();
            if (!result) {
                logger.error("创建工作文件夹失败");
            }
        }
        if (!cacheFile.exists()) {
            boolean result = Config.cacheFile.mkdirs();
            if (!result) {
                logger.error("创建缓存目录失败");
            }
        }
        if (!userSettingFile.exists()) {
            // 拷贝Setting.js 到工作目录
            copyFile("user-config-file", userSettingFile);
        }
    }

    private static void copyFile(String resourcePath, File target){
        try (InputStream is = Config.class.getClassLoader()
                .getResourceAsStream(sysProperties.getProperty(resourcePath))) {
            OutputStream outStream = new FileOutputStream(target);
            byte[] bytes = new byte[1024];
            int read;
            while ((read = is.read(bytes)) != -1) {
                outStream.write(bytes, 0, read);
            }
            outStream.close();
        } catch (IOException e) {
            logger.error("初始化配置文件错误", e);
            System.exit(0);
        }
    }

    public static ConfigBean getConfigBean() {
        init();
        try {
            FileReader fileReader = new FileReader(userSettingFile);
            StringBuilder configJsSb = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.isBlank(line) || line.trim().startsWith("//")) {
                        continue;
                    }
                    configJsSb.append(line);
                }
            } catch (IOException e) {
                logger.error("读取配置文件发生错误 workPath=" + Config.workPath, e);
            }
            Matcher matcher = configPattern.matcher(configJsSb.toString());
            String configJson = "";
            if (matcher.find()){
                configJson = matcher.group(1);
            }
            return JSON.parseObject(configJson, ConfigBean.class);
        } catch (Exception e) {
            logger.error("读取用户配置文件异常", e);
            return new ConfigBean();
        }
    }

    public static File getHostsFileCategory(String name) throws IOException {
        File root = getHostsFileRoot();
        File newFile = new File(root, name);
        if (!newFile.exists()) {
            boolean result = newFile.mkdirs();
            if (!result) {
                throw new IOException("创建hosts文件分组失败 name=" + name);
            }
        }
        return newFile;
    }

    public static File getHostsFileRoot(){
        init();
        if (!hostsFile.exists()) {
            boolean result = Config.hostsFile.mkdirs();
            if (!result) {
                throw new RuntimeException("创建hosts文件目录失败");
            }
        }
        return hostsFile;
    }

    public static File getHostsFileRemote(){
        File remoteFile = new File(hostsFile, ResourceBundleUtil.getString("key.remote-hosts-file"));
        if (!remoteFile.exists()) {
            boolean result = remoteFile.mkdirs();
            if (!result) {
                throw new RuntimeException("创建hosts文件目录失败");
            }
        }
        return remoteFile;
    }

    public static List<File> getHostsFileList() throws IOException {
        return getHostsFileList(getHostsFileRoot());
    }

    public static List<File> getHostsFileList(File file) throws IOException {
        List<File> fileList = Lists.newArrayList();
        File[] childFiles = file.listFiles();
        if (childFiles != null) {
            for (File childFile : childFiles) {
                if (childFile.getName().startsWith(".")) {
                    continue;
                }
                fileList.add(childFile);
            }
        }
        return fileList;
    }

    public static File getCommonHostFile(){
        init();
        File file = new File(getHostsFileRoot(), getConfigBean().getCommonHostsFileName());
        if (!file.exists()){
            copyFile("common-hosts-file", file);
        }
        return file;
    }

    /**
     * 监控文件变化
     */
    private static void watch() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            //给path路径加上文件观察服务
            workPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            // start an infinite loop
            while (true) {
                final WatchKey key = watchService.take();

                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    final WatchEvent.Kind<?> kind = watchEvent.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    //创建事件
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                    }
                    //修改事件
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                    }
                    //删除事件
                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

                    }
                    // get the filename for the event
                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    // print it out
                    System.out.println(kind + " -> " + filename);

                }
                // reset the keyf
                boolean valid = key.reset();
                // exit loop if the key is not valid (if the directory was
                // deleted, for
                if (!valid) {
                    break;
                }
            }

        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }
    }

    public static String getAdminPassword() {
        return adminPassword;
    }

    public static boolean setAdminPassword(String adminPassword) {
        if (SystemUtil.changeMod(adminPassword, SystemUtil.getSysHostsPath())) {
            Config.adminPassword = adminPassword;
            return true;
        }
        Config.adminPassword = "";
        return false;
    }

    public static boolean checkAdminPassword() {
        return StringUtils.isNotBlank(adminPassword);
    }

    public static File getCacheFile() {
        return cacheFile;
    }

    public static boolean isValidHostsCategory(File file) {
        if (!file.exists() || file.isFile() || file.getName().startsWith(".")) {
            return false;
        }
        if (!file.getPath().startsWith(hostsFile.getPath())) {
            return false;
        }
        int deep = 0;
        while (!file.equals(hostsFile)) {
            file = file.getParentFile();
            deep++;
        }
        return deep <= getConfigBean().getHostsCategoryDeep();
    }

    public static String getSysHostsPath() {
        String path = SystemUtil.getSysHostsPath();
        if (StringUtils.isNotBlank(path)) {
            return path;
        }
        return getConfigBean().getSysHostsPath();
    }

    public static File getWorkFolder() {
        return workPath.toFile();
    }
}
