package com.smallcold.hosts.operate;

import com.google.common.collect.Lists;
import com.smallcold.hosts.utils.IPDomainUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/*
 * Created by smallcold on 2017/9/1.
 */
public class HostBean {

    @Getter
    @Setter
    private Integer id;

    /**
     * 是否启用，如果为否，则是注释状态
     */
    @Getter
    @Setter
    private boolean enable;

    /**
     * 原始内容
     */
    @Getter
    @Setter
    private String comment;

    /**
     * IP 地址
     */
    @Getter
    private long ip;

    /**
     * 域名
     */
    @Getter
    @Setter
    private String domain;

    public HostBean() {
        enable = true;
        comment = "";
        ip = -1;
        domain = "";
    }

    public HostBean(long ip, String domain, boolean enable) {
        this(ip, domain, enable, "");
    }

    public HostBean(long ip, String domain, boolean enable, String content) {
        this.enable = enable;
        this.ip = ip;
        this.domain = domain;
        if (!isValid() && StringUtils.isNotBlank(content) && content.startsWith("#")) {
            this.comment = content;
        }
    }

    public static List<HostBean> build(String hostLine) {
        List<HostBean> hostBeanList = Lists.newArrayList();
        boolean isBlank = StringUtils.isBlank(hostLine) || StringUtils.isBlank(hostLine.trim());
        if (!isBlank) {
            List<String> domainList = IPDomainUtil.getDomainList(hostLine);
            long ip = IPDomainUtil.getIPLong(hostLine);
            boolean enable = !hostLine.startsWith("#");
            if (ip == IPDomainUtil.SELF_IP_LONG) {
                domainList = IPDomainUtil.getSelfDomainList(hostLine);
            }
            if (CollectionUtils.isEmpty(domainList)) {
                hostBeanList.add(new HostBean(-1, "", false, hostLine));
            } else {
                for (String domain : domainList) {
                    hostBeanList.add(new HostBean(ip, domain, enable, hostLine));
                }
            }
        }
        return hostBeanList;
    }

    public boolean isValid() {
        return getIp() >= 0 && StringUtils.isNotBlank(domain);
    }

    public HostBean setIp(long ip) {
        if (ip < 0) {
            this.enable = false;
        }
        this.ip = ip;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        HostBean hostBean = (HostBean) o;

        return new EqualsBuilder()
                .append(isEnable(), hostBean.isEnable())
                .append(getIp(), hostBean.getIp())
                .append(getDomain(), hostBean.getDomain())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(isEnable())
                .append(getIp())
                .append(getDomain())
                .toHashCode();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return comment;
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(comment)) {
            if (!comment.startsWith("#")) {
                sb.append("# ");
            }
            sb.append(comment.replaceAll("\\s", "")).append("\n");
        }
        if (!isEnable()) {
            sb.append("# ");
        }
        sb.append(IPDomainUtil.longToIP(this.getIp()));
        sb.append("\t").append(domain);
        return sb.toString();
    }
}
