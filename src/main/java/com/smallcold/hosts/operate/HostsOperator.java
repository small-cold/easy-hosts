package com.smallcold.hosts.operate;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.smallcold.hosts.utils.IPDomainUtil;
import com.smallcold.hosts.view.controller.HostsSearchResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Hosts 操作工具类
 *
 * @author smallcold
 * @date 2017/9/1
 */
public class HostsOperator {

    static final Logger LOGGER = Logger.getLogger(HostsOperator.class);

    @Getter
    @Setter
    private File file;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<String> lineList;

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

    public HostsOperator init() {
        setDisable(false);
        setChanged(false);
        readFile();
        return this;
    }

    /**
     * 读取Hosts 文件
     */
    private void readFile() {
        if (isDisable()) {
            return;
        }
        FileReader reader;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            setDisable(true);
            return;
        }
        lineList = Lists.newArrayList();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            LOGGER.error("读取Hosts文件发生错误 file=" + file, e);
        }
    }

    /**
     * 刷新写入文件
     */
    public void flush() throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (String line : getLineList()) {
                fileWriter.write(line + "\n");
            }
            setChanged(false);
        }
    }

    /**
     * 根据域名找到已有的配置
     *
     * @param key 关键字
     * @return
     */
    public List<HostsSearchResult> search(String key) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        key = key.toLowerCase();
        List<HostsSearchResult> searchResultList = Lists.newArrayList();
        int index = 0;
        for (String line : getLineList()) {
            if (StringUtils.isNotBlank(line)) {
                int findIndex = line.indexOf(key);
                if (findIndex >= 0) {
                    HostsSearchResult hostsSearchResult = new HostsSearchResult(this, index, line, findIndex);
                    searchResultList.add(hostsSearchResult);
                }
            }
            index++;
        }
        return searchResultList;
    }

    /**
     * 更新内容
     *
     * @param text 新的Hosts内容
     */
    public boolean load(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        lineList = Lists.newArrayList(text.split("\n"));
        return true;
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
        Set<String> hostSet = new LinkedHashSet<>();
        for (HostsOperator hostsOperator : hostsOperators) {
            if (hostsOperator == null) {
                continue;
            }
            for (String line : hostsOperator.getLineList()) {
                if (!line.startsWith("#") && hostSet.contains(line)) {
                    continue;
                }
                hostSet.add(line);
            }
        }
        setLineList(Lists.newArrayList(hostSet));
    }

    @Override
    public String toString() {
        return name;
    }

    public Set<String> getIPSet() {
        Set<String> IPSet = new HashSet<>();
        for (String line : getLineList()) {
            String ipStr = IPDomainUtil.getIPText(line);
            if (StringUtils.isBlank(ipStr)) {
                continue;
            }
            IPSet.add(ipStr);
        }
        return IPSet;
    }

    public String get(int i) {
        return getLineList().get(i);
    }

    public void setDisable(boolean disable) {
        this.isDisable = disable;
    }

    public boolean isDisable() {
        return isDisable || !file.exists() || file.isDirectory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostsOperator that = (HostsOperator) o;
        return Objects.equal(getFile(), that.getFile());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFile());
    }

    public void add(String line) {
        if (getLineList().contains(line)) {
            return;
        }
        getLineList().add(line);
    }
}
