package com.smallcold.hosts.conf;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/*
 * Created by smallcold on 2017/9/3.
 */
@ToString
public class ConfigBean {

    /**
     * 通用Hosts文件名
     */
    @Getter
    @Setter
    private String commonHostsFileName = ResourceBundleUtil.getString("key.common-hosts-file");

    /**
     * 系统环境变量
     */
    @Getter
    @Setter
    private String sysHostsPath = "";

    /**
     * hosts 文件分组深度
     */
    @Getter
    private int hostsCategoryDeep = 1;

    @Getter
    @Setter
    private List<RemoteHostsFile> remoteHostsFileList = Lists.newArrayList();

    public ConfigBean setHostsCategoryDeep(int hostsCategoryDeep) {
        if (hostsCategoryDeep < 1) {
            hostsCategoryDeep = 1;
        }
        if (hostsCategoryDeep > 3) {
            hostsCategoryDeep = 3;
        }
        this.hostsCategoryDeep = hostsCategoryDeep;
        return this;
    }

    public boolean addRemoteHostsFile(String path, String url) {
        RemoteHostsFile remoteHostsFile = new RemoteHostsFile();
        remoteHostsFile.setName(path);
        remoteHostsFile.setUrl(url);
        return remoteHostsFileList.contains(remoteHostsFile) || remoteHostsFileList.add(remoteHostsFile);
    }

    public boolean removeRemoteHostsFile(RemoteHostsFile remoteHostsFile) {
        return remoteHostsFileList.remove(remoteHostsFile);
    }

}
