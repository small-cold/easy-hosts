package com.smallcold.hosts.test;

import com.smallcold.hosts.operate.HostBean;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Created by smallcold on 2017/9/1.
 */
public class HostsHelperTest {

    @Test
    public void test() {
        HostBean.build("127.0.0.1\tlocalhost  ChaoMBP");
    }

    @Test
    public void testReadFile() throws FileNotFoundException {
        HostsOperator list = new HostsOperator("/etc/hosts");
        System.out.println(list);
    }

    @Test
    public void testChange() throws IOException {
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator();
        hostsOperator.changeStatus("192.168.76.2", "www.liepin.com", false);
        for (HostBean hostBean : hostsOperator.getHostBeanList()) {
            if (hostBean.isValid() && hostBean.getDomain().equals("www.liepin.com")) {
                System.out.println("修改后的：" + hostBean);
            }
        }
    }
}
