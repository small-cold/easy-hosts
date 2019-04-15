package com.smallcold.hosts.view.properties;

import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorCategory;
import lombok.Getter;

/*
 * Created by smallcold on 2017/9/5.
 */
public class HostsOperatorProperty {

    @Getter
    private HostsOperator hostsOperator;

    @Getter
    private HostsOperatorCategory hostsOperatorCategory;

    public HostsOperatorProperty setHostsOperator(HostsOperator hostsOperator) {
        this.hostsOperator = hostsOperator;
        return this;
    }

    public HostsOperatorProperty setHostsOperatorCategory(
            HostsOperatorCategory hostsOperatorCategory) {
        this.hostsOperatorCategory = hostsOperatorCategory;
        return this;
    }

    @Override
    public String toString() {
        String result = "--";
        if (hostsOperator != null) {
            result = hostsOperator.getName();
        }
        if (hostsOperatorCategory != null) {
            result = hostsOperatorCategory.getName();
        }
        return result;
    }
}
