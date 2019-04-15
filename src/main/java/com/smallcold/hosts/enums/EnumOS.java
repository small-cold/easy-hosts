package com.smallcold.hosts.enums;

/*
 * Created by smallcold on 2017/9/3.
 */
public enum EnumOS {

    MacOS("MacOS"),
    Linux("Linux"),
    Windows("Windows"),
    OTHER("Other"),;

    private EnumOS(String desc) {
        this.description = desc;
    }

    public String toString() {
        return description;
    }

    private String description;
}
