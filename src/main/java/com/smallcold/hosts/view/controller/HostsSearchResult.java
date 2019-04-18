package com.smallcold.hosts.view.controller;

import com.smallcold.hosts.operate.HostsOperator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author smallcold
 * @date 2017/9/12
 */
@Getter
@Setter
@ToString
public class HostsSearchResult {

    private HostsOperator hostsOperator;

    private int lineNum;
    private String line;
    private int score;

    public HostsSearchResult(HostsOperator hostsOperator, int lineNum, String line, int score) {
        this.hostsOperator = hostsOperator;
        this.lineNum = lineNum;
        this.line = line;
        this.score = score;
    }
}
