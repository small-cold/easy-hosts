package com.smallcold.hosts.view.controller;

import com.smallcold.hosts.operate.HostBean;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.utils.IPDomainUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * Created by smallcold on 2017/9/12.
 */
@Getter
@Setter
@ToString
public class HostsSearchResult {

    private HostsOperator hostsOperator;

    private int id;

    private String title;
    private String detail;
    private String domain;
    private boolean enable;
    private String ip;

    private int score;

    public HostsSearchResult(HostsOperator hostsOperator, HostBean hostBean) {
        this.hostsOperator = hostsOperator;
        id = hostBean.getId() == null? hostsOperator.getHostBeanList().size() -1: hostBean.getId();
        title = hostBean.getDomain();
        domain = hostBean.getDomain();
        enable = hostBean.isEnable();
        detail = hostBean.toString();
        ip = IPDomainUtil.longToIP(hostBean.getIp());
    }
}
