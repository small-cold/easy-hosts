package com.smallcold.hosts.test;

import com.smallcold.hosts.operate.HostsOperator;
import org.junit.Test;

import java.io.FileNotFoundException;

/*
 * Created by smallcold on 2017/9/1.
 */
public class HostsHelperTest {

    @Test
    public void testReadFile() throws FileNotFoundException {
        HostsOperator list = new HostsOperator("/etc/hosts");
        System.out.println(list);
    }
}
