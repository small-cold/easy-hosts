package com.smallcold.hosts.operate;

import com.smallcold.hosts.conf.Config;
import com.smallcold.hosts.enums.EnumOS;
import com.smallcold.hosts.exception.PermissionIOException;
import com.smallcold.hosts.utils.SystemUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author smallcold
 * @date 2017/9/1
 */
public class SysHostsOperator extends HostsOperator {

    public static SysHostsOperator instance;

    public static SysHostsOperator getInstance() {
        if (instance == null) {
            instance = new SysHostsOperator();
            instance.setName("当前配置");
        }
        return instance;
    }

    private SysHostsOperator() {
        super(Config.getSysHostsPath());
    }

    @Override
    public boolean isOnlyRead() {
        return StringUtils.isBlank(Config.getAdminPassword());
    }

    @Override
    public void flush() throws IOException {
        // macOS  Linux
        if (SystemUtil.CURRENT_OS != EnumOS.Windows
                && StringUtils.isBlank(Config.getAdminPassword())) {
            throw new PermissionIOException("需要管理员权限");
        }
        File cacheFile;
        if (SystemUtil.CURRENT_OS == EnumOS.Windows) {
            cacheFile = new File(SystemUtil.getSysHostsPath());
        } else {
            cacheFile = new File(Config.getCacheFile(), "currentHost");
        }
        try (FileWriter fileWriter = new FileWriter(cacheFile)) {
            for (String line : getLineList()) {
                fileWriter.write(line + "\n");
            }
            //  copy to 系统目录
            if (SystemUtil.CURRENT_OS != EnumOS.Windows) {
                SystemUtil.adminMove(cacheFile, new File(SystemUtil.getSysHostsPath()), Config.getAdminPassword());
            }
            // 清除系统缓存
            SystemUtil.clearDNSCache(null);
        } catch (IOException e) {
            LOGGER.error("写入Hosts文件发生错误 file=" + cacheFile, e);
            throw e;
        }
        // super.flush();
    }
}
