package com.smallcold.hosts.view.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.smallcold.hosts.operate.HostBean;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorCategory;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import com.smallcold.hosts.utils.IPDomainUtil;
import com.smallcold.hosts.view.DialogUtils;
import com.smallcold.hosts.view.SearchBox;
import com.smallcold.hosts.view.SearchPopover;
import com.smallcold.hosts.view.properties.HostProperty;
import com.smallcold.hosts.view.properties.HostsOperatorProperty;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/*
 * Created by smallcold on 2017/9/4.
 */
public class MainController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MainController.class);

    @FXML
    @Getter
    public BorderPane root;
    @FXML
    public Label messageLabel;
    @FXML
    public Label errorMessageLabel;
    @FXML
    public TableView<HostProperty> hostsTableView;

    @FXML
    public TableColumn<HostProperty, Boolean> enableCol;

    @FXML
    public TableColumn<HostProperty, String> ipCol;
    @FXML
    public TableColumn<HostProperty, String> domainCol;
    @FXML
    public TableColumn<HostProperty, String> commentCol;

    @FXML
    public TreeView<HostsOperatorProperty> hostsFileTreeView;
    @FXML
    public SearchBox searchBox;

    @FXML
    private SearchPopover searchPopover;

    private Map<HostsOperator, TreeItem<HostsOperatorProperty>> treeItemMap = Maps.newHashMap();

    @FXML
    public TreeItem<HostsOperatorProperty> sysHostsOperatorTreeItem;
    @FXML
    public TreeItem<HostsOperatorProperty> rootTreeItem;

    public MainController setHostsOperator(HostsOperator hostsOperator) {
        if (hostsOperator != null) {
            this.hostsOperator = hostsOperator;
        }
        return this;
    }

    /**
     * 当前hosts操作类
     */
    private HostsOperator hostsOperator;

    private List<HostProperty> hostList;

    @Getter
    @Setter
    private ObjectProperty<Callback<Throwable, Integer>> callbackObjectProperty;

    private HostsOperator getHostsOperator() {
        if (hostsOperator == null) {
            hostsOperator = HostsOperatorFactory.getSystemHostsOperator();
        }
        return hostsOperator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enableCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
            if (CollectionUtils.isEmpty(hostList)) {
                return null;
            }
            HostProperty hostProperty = hostList.get(param);
            try {
                hostsOperator.enable(hostProperty.idProperty().get(), hostProperty.enableProperty().getValue());
                if (hostsOperator.isChanged()) {
                    hostsOperator.flush();
                }
            } catch (IOException e) {
                hostProperty.enableProperty().setValue(!hostProperty.enableProperty().get());
                callbackObjectProperty.getValue().call(e);
                LOGGER.error("保存hosts状态失败", e);
            }
            return hostProperty.enableProperty();
        }));
        StringConverter<String> sc = new StringConverter<String>() {
            @Override
            public String toString(String t) {
                return t;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };
        ipCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        domainCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        commentCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        refreshData();

        hostsFileTreeView.setShowRoot(false);
        rootTreeItem.setExpanded(true);
        initHostsOperatorTree();
        searchPopover.init(searchBox, this);

    }

    private void initHostsOperatorTree() {
        hostsFileTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sysHostsOperatorTreeItem.setValue(new HostsOperatorProperty()
                .setHostsOperator(HostsOperatorFactory.getSystemHostsOperator()));
        treeItemMap.put(HostsOperatorFactory.getSystemHostsOperator(), sysHostsOperatorTreeItem);
        try {
            addTreeItem(rootTreeItem, HostsOperatorFactory.getUserHostsOperatorCategory());
        } catch (IOException e) {
            DialogUtils.createExceptionDialog("加载Hosts文件异常", e);
        }
        activeShowSysHosts();
        hostsFileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // 应该在这里调用
            if (observable != null && observable.getValue() != null){
                refreshCurrentHostsOperator(observable.getValue().getValue());
            }
        });
    }

    private void addTreeItem(TreeItem<HostsOperatorProperty> parentItem, HostsOperatorCategory hostsOperatorCategory) {
        if (CollectionUtils.isNotEmpty(hostsOperatorCategory.getHostsOperatorList())) {
            for (HostsOperator hostsOperator : hostsOperatorCategory.getHostsOperatorList()) {
                TreeItem<HostsOperatorProperty> treeItem = new TreeItem<>(new HostsOperatorProperty()
                        .setHostsOperator(hostsOperator));
                parentItem.getChildren().add(treeItem);
                treeItemMap.put(hostsOperator, treeItem);
            }
        }
        if (CollectionUtils.isNotEmpty(hostsOperatorCategory.getSubCategoryList())) {
            for (HostsOperatorCategory operatorCategory : hostsOperatorCategory.getSubCategoryList()) {
                TreeItem<HostsOperatorProperty> subItem = new TreeItem<>(new HostsOperatorProperty()
                        .setHostsOperatorCategory(operatorCategory));
                subItem.setExpanded(true);
                parentItem.getChildren().add(subItem);
                addTreeItem(subItem, operatorCategory);
            }
        }
    }

    public void requestFocus() {
        hostsTableView.requestFocus();
    }

    public void activeShowSysHosts() {
        hostsFileTreeView.getSelectionModel().select(sysHostsOperatorTreeItem);
        setHostsOperator(HostsOperatorFactory.getSystemHostsOperator());
        refreshData();
    }

    @FXML
    public void refreshHostsTable(MouseEvent mouseEvent) {
        // 单击，刷新table
        if (mouseEvent.getSource() == hostsFileTreeView
                && hostsFileTreeView.getSelectionModel().getSelectedItem() != null) {
            HostsOperatorProperty hostsOperatorProperty = hostsFileTreeView.getSelectionModel().getSelectedItem()
                    .getValue();
            if (mouseEvent.getClickCount() == 2 && hostsOperatorProperty.getHostsOperator() != null) {
                // 检查管理员密码
                sysHostsSwitchTo(hostsOperatorProperty.getHostsOperator());
            }
        }
    }

    private void refreshCurrentHostsOperator(HostsOperatorProperty hostsOperatorProperty) {
        if (hostsOperatorProperty.getHostsOperator() != null) {
            setHostsOperator(hostsOperatorProperty.getHostsOperator());
            refreshData();
        }
    }

    public void saveIP(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveIp(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().ipProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            Integer result = getCallbackObjectProperty().getValue().call(e);
            if (result == 1) {
                saveIP(event);
            } else {
                LOGGER.error("保存hosts IP 失败 result = " + result, e);
            }
        }
    }

    public void saveDomain(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveDomain(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().domainProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            Integer result = getCallbackObjectProperty().getValue().call(e);
            if (result == 1) {
                saveDomain(event);
            } else {
                LOGGER.error("保存hosts 域名 失败 result = " + result, e);
            }
        }
    }

    public void saveComment(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveComment(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().commentProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            Integer result = getCallbackObjectProperty().getValue().call(e);
            if (result == 1) {
                saveComment(event);
            } else {
                LOGGER.error("保存hosts 备注 失败 result = " + result, e);
            }
        }
    }

    public void refreshData() {
        if (getHostsOperator() == null) {
            return;
        }
        getHostsOperator().init();
        hostList = Lists.newArrayList();
        for (HostBean hostBean : getHostsOperator().getHostBeanList()) {
            hostList.add(new HostProperty(hostBean));
        }
        final ObservableList<HostProperty> data = FXCollections.observableArrayList(
                hostProperty -> new Observable[] { hostProperty.enableProperty() });
        data.addAll(hostList);
        hostsTableView.setEditable(!getHostsOperator().isOnlyRead());
        hostsTableView.setItems(data);
        hostsTableView.refresh();
        // hostsTableView.requestFocus();
    }

    public Map<HostsOperator, List<HostsSearchResult>> search(String key) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyMap();
        }
        Map<HostsOperator, List<HostsSearchResult>> result = Maps.newLinkedHashMap();
        result.put(getHostsOperator(), getHostsOperator().search(key));
        return result;
    }

    /**
     * 找到对应的节点
     *
     * @param result   搜索结果
     * @param isSwitch 是否切换
     */
    public void getToItem(HostsSearchResult result, boolean isSwitch) {
        // 如果切换
        if (isSwitch) {
            // 页面切换到系统
            if (getHostsOperator() != sysHostsOperatorTreeItem.getValue().getHostsOperator()) {
                activeShowSysHosts();
            }
            // 搜索结果不是系统的
            if (result.getHostsOperator() != getHostsOperator()) {
                HostBean hostBean = result.getHostsOperator().get(result.getId());
                getHostsOperator().saveHost(hostBean);
            } else if (result.isEnable()) {
                getHostsOperator().changeStatus(IPDomainUtil.SELF_IP, result.getDomain(),
                        !IPDomainUtil.SELF_IP.equals(result.getIp()));
            } else if (!result.isEnable()) {
                getHostsOperator().enable(result.getId(), true);
            }
        } else if (result.getHostsOperator() != getHostsOperator()) {
            // 激活tree
            hostsFileTreeView.getSelectionModel().select(treeItemMap.get(result.getHostsOperator()));
            setHostsOperator(result.getHostsOperator());
            refreshData();
        }
        // 自动保存
        if (getHostsOperator().isChanged()) {
            try {
                getHostsOperator().flush();
                refreshData();
            } catch (IOException e) {
                getCallbackObjectProperty().getValue().call(e);
            }
        }
        // 定位到
        if (CollectionUtils.isNotEmpty(hostList)) {
            int index = 0;
            for (HostProperty hostProperty : hostList) {
                if (hostProperty.idProperty().get() == result.getId()) {
                    hostsTableView.getSelectionModel().select(hostProperty);
                    hostsTableView.scrollTo(index > 6 ? index - 2 : 0);
                    // hostsTableView.getFocusModel().focus(index);
                    hostsTableView.setFocusTraversable(true);
                    Platform.runLater(() -> {
                        hostsTableView.requestFocus();
                    });
                    break;
                }
                index++;
            }
        }
    }

    private void sysHostsSwitchTo(HostsOperator newHostsOperator) {
        if (newHostsOperator == null) {
            return;
        }
        HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator();
        hostsOperator.switchTo(HostsOperatorFactory.getCommonHostsOperator(), newHostsOperator);
        if (HostsOperatorFactory.getSystemHostsOperator().isChanged()) {
            try {
                HostsOperatorFactory.getSystemHostsOperator().flush();
                messageLabel.setText("当前使用【" + newHostsOperator.getName() + "】");
                activeShowSysHosts();
            } catch (IOException e) {
                // FIXME 应该直接调用undo 方法
                hostsOperator.init();
                getCallbackObjectProperty().getValue().call(e);
            }
        }
    }

    public void rootKeyPressed(KeyEvent event) {
        if (event.isShortcutDown() && event.getCode() == KeyCode.F) {
            searchBox.requestFocus();
        }
    }


    public void treeKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (hostsFileTreeView.getSelectionModel().getSelectedItem().getValue() != null) {
                sysHostsSwitchTo(hostsFileTreeView.getSelectionModel().getSelectedItem().getValue().getHostsOperator());
            }
        }
    }

    public void refreshFileTree() {
        if (rootTreeItem.getChildren().size() > 1) {
            rootTreeItem.getChildren().remove(1, rootTreeItem.getChildren().size());
        }
        initHostsOperatorTree();
    }
}
