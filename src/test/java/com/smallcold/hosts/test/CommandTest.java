package com.smallcold.hosts.test;

import com.smallcold.hosts.command.HostsCommand;

/*
 * Created by smallcold on 2017/9/4.
 */
public class CommandTest {

    public static void main(String[] args) {
        // HostsCommand.doCommand(new String[] { "-s", "10.10.102.106" });
        // HostsCommand.doCommand(new String[] { "-c" });
        HostsCommand.doCommand(new String[] { "-s", "m.liepin.com" });
        HostsCommand.doCommand(new String[] { "-s", "m.liepin.com", "-disable" });
    }
}
