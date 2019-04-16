package com.smallcold.hosts.test;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/*
 * Created by smallcold on 2017/11/12.
 */
public class LoveTest {

    @Test
    public void test() throws InterruptedException {
        int day = 0;
        while (true){
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("I Love You, Tang Xiaoyan. " + day);
            day ++;
        }
    }
}
