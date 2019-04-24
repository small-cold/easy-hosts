package com.smallcold.hosts.view.controller;

import com.google.common.collect.Maps;
import com.smallcold.hosts.operate.HostsOperator;
import com.smallcold.hosts.operate.HostsOperatorCategory;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import com.smallcold.hosts.operate.SysHostsOperator;
import com.smallcold.hosts.view.DialogUtils;
import com.smallcold.hosts.view.SearchBox;
import com.smallcold.hosts.view.SearchPopover;
import com.smallcold.hosts.view.properties.HostsOperatorProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private StyleClassedTextArea textArea;
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
        // add line numbers to the left of textArea
        textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));
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
        hostsFileTreeView.setCellFactory(param -> new CheckBoxTreeCell<>());


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
                CheckBoxTreeItem<HostsOperatorProperty> treeItem = new CheckBoxTreeItem<>(new HostsOperatorProperty()
                        .setHostsOperator(hostsOperator));
                treeItem.setSelected(hostsOperator.isSelected());
                parentItem.getChildren().add(treeItem);
                treeItemMap.put(hostsOperator, treeItem);
            }
        }
        if (CollectionUtils.isNotEmpty(hostsOperatorCategory.getSubCategoryList())) {
            for (HostsOperatorCategory operatorCategory : hostsOperatorCategory.getSubCategoryList()) {
                CheckBoxTreeItem<HostsOperatorProperty> subItem = new CheckBoxTreeItem<>(new HostsOperatorProperty()
                        .setHostsOperatorCategory(operatorCategory));
                subItem.setSelected(operatorCategory.isSelected());
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
        textArea.clear();
        if (CollectionUtils.isNotEmpty(getHostsOperator().getLineList())) {
            getHostsOperator().getLineList().forEach(line -> textArea.appendText(line + "\n"));
        }
        textArea.getUndoManager().forgetHistory();
        textArea.setEditable(!(getHostsOperator() instanceof SysHostsOperator));
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
        String line = result.getLine();
        if (isSwitch) {
            line = result.getHostsOperator().switchLine(result.getLineNum());
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
        Function<Integer, Integer> clamp = i -> Math.max(0, Math.min(i, textArea.getLength() - 1));
        textArea.showParagraphAtTop(clamp.apply(result.getLineNum()));
        int anchor = textArea.getText().indexOf(line);
        textArea.selectRange(anchor, anchor + line.length());
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
                showMsg("当前使用【" + newHostsOperator.getName() + "】");
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
            getHostsOperator().load(textArea.getText());
            getHostsOperator().flush();
            showMsg("保存成功【" + getHostsOperator().getName() + "】");
            // 如果该文件是启用状态，切换系统hosts
            if (getHostsOperator().isSelected()) {
                HostsOperator hostsOperator = HostsOperatorFactory.getSystemHostsOperator();
                List<HostsOperator> selectedHostsOperatorList = HostsOperatorFactory.getSelectedHostsOperatorList();
                selectedHostsOperatorList.add(HostsOperatorFactory.getCommonHostsOperator());
                hostsOperator.switchTo(selectedHostsOperatorList);
                if (HostsOperatorFactory.getSystemHostsOperator().isChanged()) {
                    try {
                        HostsOperatorFactory.getSystemHostsOperator().flush();
                        showMsg("更新系统Hosts完成");
                        activeShowSysHosts();
                    } catch (IOException e) {
                        // FIXME 应该直接调用undo 方法
                        showMsg("更新系统Hosts失败【" + e.getMessage() + "】");
                        hostsOperator.init();
                        getCallbackObjectProperty().getValue().call(e);
                    }
                }
            }
        } catch (IOException e) {
            showErrorMsg("保存异常" + e.getMessage());
        }
    }

    public void showErrorMsg(String msg) {
        errorMessageLabel.setText(msg + " " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        messageLabel.setText("");
    }

    public void showMsg(String msg) {
        messageLabel.setText(msg + " " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorMessageLabel.setText("");
    }
}
