package com.smallcold.hosts.test;

import com.smallcold.hosts.conf.Config;
import com.smallcold.hosts.conf.ConfigBean;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * Created by smallcold on 2017/9/4.
 */
public class ConfigTest {

    @Test
    public void testReadConfig() throws IOException {
        ConfigBean configBean = Config.getConfigBean();
        System.out.println(configBean);
    }

    @Test
    public void testGetAllHostsOperator() throws IOException {
        List<File> hostsOperatorList = Config.getHostsFileList();
        for (File hostsOperator : hostsOperatorList) {
            System.out.println(hostsOperator.getName());
        }
    }

    @Test
    public void test() {
        System.out.println(System.getProperties().getProperty("user.home"));
    }
}
