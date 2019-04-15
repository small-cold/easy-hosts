package com.smallcold.hosts.conf;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/*
 * Created by smallcold on 2017/9/14.
 */
@Getter
@Setter
@ToString
public class RemoteHostsFile {

    /**
     * 文件名称
     */
    private String name;

    /**
     * 地址
     */
    private String url;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        RemoteHostsFile that = (RemoteHostsFile) o;

        return new EqualsBuilder()
                .append(getUrl(), that.getUrl())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUrl())
                .toHashCode();
    }
}
