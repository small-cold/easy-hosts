package com.smallcold.hosts.operate;

import com.google.common.collect.Lists;
import com.smallcold.hosts.utils.IPDomainUtil;
import com.smallcold.hosts.view.controller.HostsSearchResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hosts 操作工具类
 * Created by smallcold on 2017/9/1.
 */
public class HostsOperator {
    protected static final Logger LOGGER = Logger.getLogger(HostsOperator.class);

    @Setter
    private List<HostBean> hostBeanList;

    @Getter
    @Setter
    private File file;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private boolean isChanged = false;

    /**
     * 是否不可用
     */
    private boolean isDisable = false;

    public HostsOperator(String path) {
        this(new File(path));
    }

    public HostsOperator(File file) {
        this(file, file.getName());
    }

    public HostsOperator(File file, String name) {
        this.file = file;
        this.name = name;
    }

    public boolean isOnlyRead() {
        return false;
    }

    public List<HostBean> getHostBeanList() {
        if (hostBeanList == null) {
            throw new Error("HostsOperator not be init, please call init()");
        }
        return hostBeanList;
    }

    public HostsOperator init() {
        setDisable(false);
        setChanged(false);
        hostBeanList = readHostFile();
        return this;
    }

    /**
     * 读取Hosts 文件
     *
     * @param path hosts 文件地址
     * @return List<HostBean>
     */
    public List<HostBean> readHostFile() {
        List<HostBean> hostBeanList = Lists.newArrayList();
        if (isDisable()) {
            return hostBeanList;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            setDisable(true);
            return hostBeanList;
        }
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
                for (HostBean hostBean : HostBean.build(line)) {
                    hostBean.setId(hostBeanList.size());
                    hostBeanList.add(hostBean);
                }
            }
            text = stringBuilder.toString();
        } catch (IOException e) {
            LOGGER.error("读取Hosts文件发生错误 file=" + file, e);
        }
        return hostBeanList;
    }

    /**
     * 刷新写入文件
     *
     * @param hostBeanList host实体类
     * @param path         写入地址
     */
    public void flush() throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (HostBean hostBean : getHostBeanList()) {
                fileWriter.write(hostBean.toString() + "\n");
            }
            setChanged(false);
        }
    }

    /**
     * 根据域名找到已有的配置
     *
     * @param key
     * @return
     */
    public List<HostsSearchResult> search(String key) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        key = key.toLowerCase();
        List<HostsSearchResult> matchHostBeanList = Lists.newArrayList();
        boolean isDisable = key.startsWith("#");
        List<String> domainList = IPDomainUtil.getDomainList(key);
        for (HostBean hostBean : getHostBeanList()) {
            if (StringUtils.isBlank(hostBean.getDomain())
                    || isDisable && hostBean.isEnable()) {
                continue;
            }
            if (domainList.contains(hostBean.getDomain())) {
                HostsSearchResult hostsSearchResult = new HostsSearchResult(this, hostBean);
                hostsSearchResult.setScore(-1);
                matchHostBeanList.add(hostsSearchResult);
            } else if (hostBean.getDomain().toLowerCase().contains(key)) {
                HostsSearchResult hostsSearchResult = new HostsSearchResult(this, hostBean);
                hostsSearchResult.setScore(hostBean.getDomain().indexOf(key));
                matchHostBeanList.add(hostsSearchResult);
            }
        }
        matchHostBeanList.sort((result, resultOther) -> {
            if (result.getScore() != resultOther.getScore()) {
                return result.getScore() - resultOther.getScore();
            }
            return result.getTitle().compareTo(resultOther.getTitle());
        });
        return matchHostBeanList;
    }

    /**
     * 添加host
     *
     * @param hostBean
     */
    public boolean saveHost(HostBean hostBean) {
        if (hostBean == null || getHostBeanList().contains(hostBean)) {
            return false;
        }
        if (!hostBean.isValid()) {
            return getHostBeanList().add(hostBean);
        }
        if (hostBean.isEnable()) {
            enable(hostBean.getIp(), hostBean.getDomain());
        } else {
            disable(hostBean.getIp(), hostBean.getDomain());
        }
        return true;
    }

    public void changeStatus(String ipStr, String domain, boolean enable) {
        if (enable) {
            enable(IPDomainUtil.ipToLong(ipStr), domain);
        } else {
            disable(IPDomainUtil.ipToLong(ipStr), domain);
        }
    }

    private void disable(long ip, String domain) {
        if (StringUtils.isBlank(domain)) {
            throw new RuntimeException("禁用域名不能为空");
        }
        List<HostBean> hostBeanList = Lists.newArrayList(getHostBeanList().iterator());
        for (HostBean existHostBean : hostBeanList) {
            if (!existHostBean.isValid()
                    || !existHostBean.getDomain().startsWith(domain)) {
                continue;
            }
            // 指定IP 只禁用对应的，否则禁用全部
            if (ip >= 0 && existHostBean.getIp() != ip) {
                continue;
            }
            existHostBean.setEnable(false);
            isChanged = true;
        }
    }

    private void enable(long ip, String domain) {
        if (ip < 0) {
            throw new RuntimeException("启用IP无效");
        }
        if (StringUtils.isBlank(domain)) {
            throw new RuntimeException("启用域名不能为空");
        }
        List<HostBean> hostBeanList = Lists.newArrayList(getHostBeanList().iterator());
        for (HostBean existHostBean : hostBeanList) {
            if (!existHostBean.isValid()) {
                continue;
            }
            // 不包含，跳过
            if (!existHostBean.getDomain().equals(domain)) {
                continue;
            }
            // 包含，但是已经启用了
            if (existHostBean.isEnable() && existHostBean.getIp() == ip) {
                continue;
            }
            if (existHostBean.getIp() != ip) { // IP 不同，全部禁用
                existHostBean.setEnable(false);
                isChanged = true;
            } else if (!existHostBean.isEnable()) { // IP 相同，原来的禁用
                existHostBean.setEnable(true);
                isChanged = true;
            }
        }
        HostBean newHostBean = new HostBean(ip, domain, true);
        if (!getHostBeanList().contains(newHostBean)) {
            getHostBeanList().add(newHostBean);
            isChanged = true;
        }
    }

    /**
     * 切换配置到
     *
     * @param otherHostsOperator
     * @param isBackup
     */
    public void switchTo(HostsOperator... hostsOperators) {
        if (hostsOperators == null) {
            return;
        }
        List<HostBean> newHostBeanList = Lists.newArrayList();
        for (HostsOperator hostsOperator : hostsOperators) {
            if (hostsOperator == null) {
                continue;
            }
            for (HostBean hostBean : hostsOperator.getHostBeanList()) {
                if (hostBean.isValid() && newHostBeanList.contains(hostBean)) {
                    continue;
                }
                if (!isChanged) {
                    isChanged = !getHostBeanList().contains(hostBean);
                }
                newHostBeanList.add(hostBean);
            }
        }
        if (isChanged || newHostBeanList.size() != getHostBeanList().size()) {
            isChanged = true;
            setHostBeanList(newHostBeanList);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void replaceIP(String source, String target) throws IOException {
        long sourceIp = IPDomainUtil.ipToLong(source);
        long targetIp = IPDomainUtil.ipToLong(target);
        if (sourceIp == targetIp) {
            return;
        }
        for (HostBean hostBean : getHostBeanList()) {
            if (!hostBean.isValid()) {
                continue;
            }
            if (hostBean.getIp() == sourceIp) {
                hostBean.setIp(targetIp);
                isChanged = true;
            }
        }
    }

    public Set<String> getIPSet() {
        Set<String> IPSet = new HashSet<>();
        for (HostBean hostBean : getHostBeanList()) {
            if (hostBean.isValid()) {
                IPSet.add(IPDomainUtil.longToIP(hostBean.getIp()));
            }
        }
        return IPSet;
    }

    public boolean enable(int i, boolean enable) {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        HostBean hostBean = hostBeanList.get(i);
        if (hostBean.isEnable() == enable) {
            return true;
        }
        if (enable) {
            enable(hostBean.getIp(), hostBean.getDomain());
        } else {
            disable(hostBean.getIp(), hostBean.getDomain());
        }
        return true;
    }

    public boolean saveDomain(int i, @NonNull String domain) {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        HostBean hostBean = hostBeanList.get(i);
        if (StringUtils.isBlank(domain)) {
            throw new IllegalArgumentException("域名不能为空");
        }
        if (hostBean.getIp() != IPDomainUtil.SELF_IP_LONG && !IPDomainUtil.isDomain(domain)
                || hostBean.getIp() == IPDomainUtil.SELF_IP_LONG && !IPDomainUtil.isSelfDomain(domain)) {
            throw new IllegalArgumentException("域名非法");
        }

        if (!hostBean.getDomain().equals(domain)) {
            hostBean.setDomain(domain);
            isChanged = true;
            return true;
        }
        return false;
    }

    public boolean saveIp(int i, @NonNull String ipStr) {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        if (StringUtils.isBlank(ipStr)) {
            throw new IllegalArgumentException("IP不能为空");
        }
        if (!IPDomainUtil.isIp(ipStr)) {
            throw new IllegalArgumentException("IP非法");
        }
        long ip = IPDomainUtil.getIPLong(ipStr);
        HostBean hostBean = hostBeanList.get(i);
        if (hostBean.getIp() != ip) {
            hostBean.setIp(ip);
            isChanged = true;
            return true;
        }
        return false;
    }

    public HostBean get(int i) {
        return getHostBeanList().get(i);
    }

    public boolean saveComment(int i, String newValue) {
        if (i < 0 || i >= hostBeanList.size()) {
            return false;
        }
        hostBeanList.get(i).setComment(newValue);
        isChanged = true;
        return false;
    }

    public void setDisable(boolean disable) {
        this.isDisable = disable;
    }

    public boolean isDisable() {
        return isDisable || !file.exists() || file.isDirectory();
    }
}
