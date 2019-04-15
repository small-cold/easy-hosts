package com.smallcold.hosts.view.properties;

import com.smallcold.hosts.operate.HostBean;
import com.smallcold.hosts.utils.IPDomainUtil;
import javafx.beans.property.*;

/*
 * Created by smallcold on 2017/9/4.
 */
public class HostProperty {

    private final IntegerProperty id;
    private final BooleanProperty enable;
    private final StringProperty ip;
    private final StringProperty domain;
    private final StringProperty comment;

    public HostProperty() {
        this.id = new SimpleIntegerProperty(-1);
        this.enable = new SimpleBooleanProperty(false);
        this.ip = new SimpleStringProperty("");
        this.domain = new SimpleStringProperty("");
        this.comment = new SimpleStringProperty("");
    }

    public HostProperty(HostBean hostBean) {
        this.id = new SimpleIntegerProperty(hostBean.getId());
        this.enable = new SimpleBooleanProperty(hostBean.isEnable());
        this.ip = new SimpleStringProperty(IPDomainUtil.longToIP(hostBean.getIp()));
        this.domain = new SimpleStringProperty(hostBean.getDomain());
        this.comment = new SimpleStringProperty(hostBean.getComment());
    }

    public BooleanProperty enableProperty() {
        return enable;
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty ipProperty() {
        return ip;
    }

    public StringProperty domainProperty() {
        return domain;
    }

    public StringProperty commentProperty() {
        return comment;
    }
}
