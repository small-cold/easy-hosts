package com.smallcold.hosts.operate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.smallcold.hosts.conf.Config;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/*
 * Created by smallcold on 2017/9/3.
 */
public class HostsOperatorFactory {

    private static HostsOperator comHostsOperator;

    private static Map<File, HostsOperator> hostsOperatorMap = Maps.newHashMap();

    public static HostsOperator getSystemHostsOperator(){
        return SysHostsOperator.getInstance();
    }

    public static HostsOperatorCategory getUserHostsOperatorCategory() throws IOException {
        return build(Config.getHostsFileRoot());
    }

    private static HostsOperator getHostsOperator(File file) {
        return hostsOperatorMap.computeIfAbsent(file, k -> new HostsOperator(file));
    }

    private static HostsOperatorCategory build(File file) throws IOException {
        HostsOperatorCategory hostsOperatorCategory = new HostsOperatorCategory();
        List<HostsOperatorCategory> subCategoryList = Lists.newArrayList();
        hostsOperatorCategory.setName(file.getName());
        for (File subFile : Config.getHostsFileList(file)) {
            if (subFile.isFile()) {
                hostsOperatorCategory.getHostsOperatorList().add(getHostsOperator(subFile));
            } else if (Config.isValidHostsCategory(subFile)) {
                hostsOperatorCategory.setSort(100);
                subCategoryList.add(build(subFile));
            }
        }
        hostsOperatorCategory.setSubCategoryList(subCategoryList);
        hostsOperatorCategory.getHostsOperatorList().sort(Comparator.comparing(HostsOperator::getName));
        subCategoryList.sort((category1, category2) -> {
            if (category1 == null || category2 == null) {
                return 0;
            }
            if (category1.getSort() != category2.getSort()) {
                return category1.getSort() - category2.getSort();
            }
            if (StringUtils.isBlank(category1.getName()) || StringUtils.isBlank(category2.getName())) {
                return 0;
            }
            return category1.getName().compareTo(category2.getName());
        });
        return hostsOperatorCategory;
    }

    public static HostsOperator getCommonHostsOperator() {
        File file = Config.getCommonHostFile();
        if (file.exists() && file.isFile()) {
            comHostsOperator = getHostsOperator(file);
            comHostsOperator.init();
        }
        return comHostsOperator;
    }
}
