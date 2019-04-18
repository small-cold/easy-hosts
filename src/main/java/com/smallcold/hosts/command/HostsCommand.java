package com.smallcold.hosts.command;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.smallcold.hosts.conf.Config;
import com.smallcold.hosts.conf.ConfigBean;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorCategory;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import com.smallcold.hosts.utils.IPDomainUtil;
import com.smallcold.hosts.view.controller.HostsSearchResult;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/*
 * Created by smallcold on 2017/9/1.
 */
public class HostsCommand {

    private static Logger logger = Logger.getLogger(HostsCommand.class);

    private static Options options = new Options();

    static {
        logger.setLevel(Level.DEBUG);
        // 切换命令
        options.addOption("c", false, "复制一份文件替换系统hosts文件");
        options.addOption(Option.builder("s")
                .hasArg()
                .desc("切换host配置")
                .argName("switch")
                .build());
        options.addOption("disable", false, "禁用配置");
        options.addOption("show", false, "显示可用配置");
    }

    public static void main(String[] args) {
        doCommand(args);
    }

    public static void doCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            ConfigBean configBean = Config.getConfigBean();
            // 解析参数
            CommandLine line = parser.parse(options, args);
            boolean disabled = line.hasOption("disable");
            if (line.hasOption("c")) {
                switchFile(line.getOptionValue("c"));
            } else if (line.hasOption("s")) {
                String optionValue = line.getOptionValue("switch");
                switchTo(optionValue, disabled);
            }
        } catch (Exception exp) {
            logger.error("发生异常: args=" + JSON.toJSONString(args), exp);
        }
    }

    private static void switchFile(String fileName) throws IOException {
        HostsOperatorCategory hostsOperatorMap = HostsOperatorFactory.getUserHostsOperatorCategory();
        // FIXME 多层分组，这样有问题呀，没输入文件名，提示选择
        if (StringUtils.isBlank(fileName)) {
            StringBuilder msg = new StringBuilder("可选环境如下：");
            int index = 0;
            for (HostsOperator hostsOperator : hostsOperatorMap.getHostsOperatorList()) {
                msg.append(index).append(". ").append(hostsOperator.getName());
                index++;
                if (index < hostsOperatorMap.getHostsOperatorList().size()) {
                    msg.append(", ");
                }
            }
            logger.info(msg.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            fileName = br.readLine();
        }
        HostsOperator sysHostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        HostsOperator comHostsOperator = HostsOperatorFactory.getCommonHostsOperator();
        if (comHostsOperator != null) {
            comHostsOperator.init();
        }
        HostsOperator newHostsOperator = hostsOperatorMap.getHostsOperator(fileName);
        if (newHostsOperator == null && fileName.matches("\\d+")) {
            Integer fileIndex = Integer.parseInt(fileName);
            if (fileIndex >= 0 && fileIndex < hostsOperatorMap.getHostsOperatorList().size()) {
                newHostsOperator = Lists.newArrayList(hostsOperatorMap.getHostsOperatorList().iterator())
                        .get(fileIndex).init();
            }
        }
        sysHostsOperator.switchTo(comHostsOperator, newHostsOperator);
    }

    private static void switchTo(String opt, boolean disabled) throws IOException {
        if (IPDomainUtil.isIp(opt)) {
            switchByIp(opt);
        } else if (IPDomainUtil.isDomain(opt)) {
            switchByDomain(opt, disabled);
        }
    }

    private static void switchByIp(String targetIp) throws IOException {
        // 切换配置文件
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        List<String> ipList = Lists.newArrayList(hostsOperator.getIPSet().iterator());
        logger.info("请选择带替换IP地址：(默认第0个)");
        int index = 0;
        for (String ip : ipList) {
            logger.info(index + ". " + ip);
            index++;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String source = br.readLine();
        Integer selectedIndex = Integer.parseInt(source);
        if (StringUtils.isBlank(source)) {
            selectedIndex = 0;
        }
        // hostsOperator.replaceIP(ipList.get(selectedIndex), targetIp);
        hostsOperator.flush();
    }

    private static void switchByDomain(String domain, boolean disabled) throws IOException {
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator().init();
        List<HostsSearchResult> hostBeanList = hostsOperator.search(domain);
        for (HostsSearchResult hostBean : hostBeanList) {
            logger.info(hostBean.getLineNum() + ". " + hostBean.getLine());
        }
        logger.info("请输入要切换的IP或要启用的配置序号(默认为127.0.0.1):");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String opt = br.readLine();
        if (StringUtils.isBlank(opt)) {
            opt = IPDomainUtil.SELF_IP;
        }
        if (IPDomainUtil.isIp(opt)) {
            // hostsOperator.changeStatus(opt, domain, !disabled);
        } else if (opt.matches("[0-9]+")) {
            int indexSelected = Integer.parseInt(opt);
            // hostsOperator.enable(indexSelected, !disabled);
        }
        hostsOperator.flush();
    }
}
