package com.smallcold.hosts.utils;

import com.smallcold.hosts.enums.EnumOS;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Created by smallcold on 2017/9/3.
 */
public class SystemUtil {
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SYS_HOSTS_WRITE_MOD = "644";
    private static String SYS_HOSTS_PATH = null;

    public static final EnumOS CURRENT_OS = getCurrentOS();

    private static EnumOS getCurrentOS() {
        if (OS_NAME.contains("mac")) {
            return EnumOS.MacOS;
        }
        if (OS_NAME.contains("win")) {
            return EnumOS.Windows;
        }
        if (OS_NAME.contains("linux")) {
            return EnumOS.Linux;
        }
        return EnumOS.OTHER;
    }

    public static boolean runProcessExce(String... cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return p.exitValue() == 0;
    }

    public static String getSysHostsPath() {
        if (StringUtils.isBlank(SYS_HOSTS_PATH)) {
            if (CURRENT_OS == EnumOS.Windows) {
                File[] files = File.listRoots();

                for (int i = 0; i < files.length; ++i) {
                    File tmp = new File(files[i] + "\\Windows");
                    if (tmp.exists()) {
                        SYS_HOSTS_PATH = files[i] + "\\Windows\\System32\\drivers\\etc\\hosts";
                    }
                }
            } else if (CURRENT_OS == EnumOS.MacOS || CURRENT_OS == EnumOS.Linux) {
                SYS_HOSTS_PATH = "/etc/hosts";
            }
        }
        return SYS_HOSTS_PATH;
    }

    /**
     * 修改文件权限
     *
     * @param pwd
     * @param path
     * @return
     */
    public static boolean changeMod(String pwd, String path) {
        if (StringUtils.isBlank(pwd)) {
            return false;
        }
        if (pwd.equals("no")) {
            return true;
        }
        try {
            String[] cmds = new String[] { "/bin/bash",
                    "-c",
                    "echo \"" + pwd + "\"| sudo -S chmod " + SYS_HOSTS_WRITE_MOD + " " + path };
            return runProcessExce(cmds);
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(SystemUtil.class).error("修改文件权限失败", e);
        }
        return false;
    }

    public static boolean adminMove(File source, File target, String pwd) throws FileNotFoundException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("source and target file must be not null");
        }
        if (!source.exists()) {
            throw new FileNotFoundException("source is not exists");
        }
        try {
            String[] cmds = new String[] { "/bin/bash",
                    "-c",
                    "echo \"" + pwd + "\"| sudo -S mv " + source.getPath() + " " + target.getPath() };
            return runProcessExce(cmds);
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(SystemUtil.class).error("修改文件权限失败", e);
        }
        return false;
    }

    public static void clearDNSCache(String cmd) {
        if (StringUtils.isBlank(cmd)) {
            if (CURRENT_OS == EnumOS.MacOS) {
                cmd = "killall -HUP mDNSResponder";
            } else if (CURRENT_OS == EnumOS.Windows) {
                cmd = "ipconfig /flushdns";
            }
        }
        try {
            Runtime.getRuntime().exec(cmd);
            try {
                Thread.sleep(200L);
            } catch (InterruptedException ie) {
                Logger.getLogger(SystemUtil.class).warn(ie);
            }
        } catch (IOException ioe) {
            Logger.getLogger(SystemUtil.class).warn(ioe);
        }
    }
}
