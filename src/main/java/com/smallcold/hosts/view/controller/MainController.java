package com.smallcold.hosts.view.controller;

import com.google.common.collect.Maps;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorCategory;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import com.smallcold.hosts.view.DialogUtils;
import com.smallcold.hosts.view.SearchBox;
import com.smallcold.hosts.view.SearchPopover;
import com.smallcold.hosts.view.properties.HostsOperatorProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Created by smallcold on 2017/9/4.
 */
public class MainController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MainController.class);

    @FXML
    @Getter
    private BorderPane root;
    @FXML
    private Label messageLabel;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private StyleClassedTextArea systemArea;
    @FXML
    private VirtualizedScrollPane<StyleClassedTextArea> hostsEditorVsPane;

    @FXML
    private TreeView<HostsOperatorProperty> hostsFileTreeView;
    @FXML
    private SearchBox searchBox;

    @FXML
    private SearchPopover searchPopover;

    private Map<HostsOperator, TreeItem<HostsOperatorProperty>> treeItemMap = Maps.newHashMap();

    @FXML
    private TreeItem<HostsOperatorProperty> sysHostsOperatorTreeItem;
    @FXML
    private TreeItem<HostsOperatorProperty> rootTreeItem;

    private void setHostsOperator(HostsOperator hostsOperator) {
        if (hostsOperator != null) {
            this.hostsOperator = hostsOperator;
        }
    }

    /**
     * 当前hosts操作类
     */
    private HostsOperator hostsOperator;

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
        // add line numbers to the left of area
        systemArea.setParagraphGraphicFactory(LineNumberFactory.get(systemArea));
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
            if (observable != null && observable.getValue() != null) {
                LOGGER.debug("点击文件节点");
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

    public void activeShowSysHosts() {
        hostsFileTreeView.getSelectionModel().select(sysHostsOperatorTreeItem);
        setHostsOperator(HostsOperatorFactory.getSystemHostsOperator());
        refreshData();
    }

    @FXML
    public void refreshHostsEditor(MouseEvent mouseEvent) {
        // 单击，刷新 Editor
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

    public void refreshData() {
        if (getHostsOperator() == null) {
            return;
        }
        getHostsOperator().init();
        systemArea.clear();
        if (CollectionUtils.isNotEmpty(getHostsOperator().getLineList())) {
            getHostsOperator().getLineList().forEach(line -> systemArea.appendText(line + "\n"));
        }
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
                String line = result.getHostsOperator().get(result.getLineNum());
                getHostsOperator().add(line);
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
        Function<Integer, Integer> clamp = i -> Math.max(0, Math.min(i, systemArea.getLength() - 1));
        systemArea.showParagraphAtTop(clamp.apply(result.getLineNum()));
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

    public void activeSearch() {
        searchBox.requestFocus();
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

    public void save() {
        try {
            getHostsOperator().flush();
        } catch (IOException e) {
            errorMessageLabel.setText("保存异常" + e.getMessage());
        }
    }
}
