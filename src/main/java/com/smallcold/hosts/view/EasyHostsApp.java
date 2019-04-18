package com.smallcold.hosts.view;
/*
 * Created by smallcold on 2017/9/4.
 */

import com.smallcold.hosts.conf.Config;
import com.smallcold.hosts.conf.ResourceBundleUtil;
import com.smallcold.hosts.enums.EnumOS;
import com.smallcold.hosts.exception.PermissionIOException;
import com.smallcold.hosts.operate.Downloader;
import com.smallcold.hosts.utils.SystemUtil;
import com.smallcold.hosts.view.controller.MainController;
import com.smallcold.hosts.view.controller.PreferencesController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class EasyHostsApp extends Application {

    private static final String MAIN_PAGE_FXML = "views/Main.fxml";
    private static final String PREFERENCES_PAGE_FXML = "views/Preferences.fxml";

    private Scene scene;
    private Pane root;

    private MenuBar menuBar;

    private MainController mainController;
    private PreferencesController preferencesController;
    private PasswordDialog passwordDialog;

    private ObjectProperty<Callback<Throwable, Integer>> callBack;

    public Callback<Throwable, Integer> getCallBack() {
        return callBack.get();
    }

    public ObjectProperty<Callback<Throwable, Integer>> callBackProperty() {
        if (callBack == null) {
            callBack = new SimpleObjectProperty<>();
            callBack.setValue(th -> {
                if (th == null || th instanceof PermissionIOException) {
                    passwordDialog.show();
                    return 1;
                } else {
                    DialogUtils.createExceptionDialog("发生错误", th);
                    return 0;
                }
            });
        }
        return callBack;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        root = new Pane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                final double w = getWidth();
                final double h = getHeight();
                final double menuHeight = menuBar.prefHeight(w);
                // final double toolBarHeight = toolBar.prefHeight(w);
                if (menuBar != null) {
                    menuBar.resize(w, menuHeight);
                }
                // toolBar.resizeRelocate(0, menuHeight, w, toolBarHeight);
                // homePane.setLayoutY(toolBarHeight + menuHeight + 5);
                // homePane.resize(w, h - toolBarHeight);
                // homePane.resize(w, h - toolBarHeight - menuHeight);
                // homePane.resizeRelocate(0, toolBarHeight + menuHeight + 5, w, h - toolBarHeight - menuHeight);
                if (preferencesController != null) {
                    preferencesController.getRoot().setLayoutX(0);
                    preferencesController.getRoot().setLayoutY(0);
                    preferencesController.getRoot().setPrefWidth(w);
                    preferencesController.getRoot().setPrefHeight(h);
                }
                if (mainController != null) {
                    mainController.getRoot().setLayoutX(0);
                    mainController.getRoot().setLayoutY(0);
                    mainController.getRoot().setPrefWidth(w);
                    mainController.getRoot().setPrefHeight(h);
                }
            }
        };
        root.setMinHeight(720);
        root.setMinHeight(480);
        initSysMenu();
        gotoMainPage();
        // initToolBar();

        root.setOnKeyReleased(event -> {
            // 搜索
            if (event.isShortcutDown() && event.getCode() == KeyCode.F) {
                mainController.activeSearch();
            }
            if (event.isShortcutDown() && event.isShiftDown() && event.getCode() == KeyCode.H) {
                mainController.activeShowSysHosts();
            }
            // 保存
            if (event.isShortcutDown() && event.getCode() == KeyCode.S) {
                mainController.save();
            }
        });
    }

    private void initAdminPasswordDialog() {
        passwordDialog = new PasswordDialog();
        passwordDialog.setTitle("请输入管理员密码");
        passwordDialog.setHeaderText("用户：" + System.getProperty("user.name"));
        passwordDialog.getDialogPane().setContentText("密码:");
        passwordDialog.show();
        passwordDialog.setOnHidden(event -> saveAdminPassword());
    }

    private void saveAdminPassword() {

        if (StringUtils.isBlank(passwordDialog.getResult())) {
            DialogUtils.createAlert("管理员密码为空",
                    "系统Hosts为只读状态，双击不能快速切换系统hosts", AlertType.WARNING);
        } else {
            boolean result = Config.setAdminPassword(passwordDialog.getResult());
            if (!result) {
                passwordDialog.setTitle("密码错误（" + System.getProperty("user.name") + ")");
                passwordDialog.show();
            } else if (mainController != null) {
                mainController.refreshData();
            }
        }
    }

    // private void initToolBar() {
    //     // CREATE TOOLBAR
    //     toolBar = new TitledToolBar();
    //     root.getChildren().add(toolBar);
    //     Button backButton = new Button();
    //     backButton.setId("back");
    //     backButton.getStyleClass().add("left-pill");
    //     backButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
    //     Button forwardButton = new Button();
    //     forwardButton.setId("forward");
    //     forwardButton.getStyleClass().add("center-pill");
    //     forwardButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
    //     homeButton = new Button();
    //     homeButton.setId("home");
    //     homeButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
    //     homeButton.getStyleClass().add("right-pill");
    //     homeButton.setOnAction(event -> {
    //         mainController.activeShowSysHosts();
    //         mainController.requestFocus();
    //     });
    //     homeButton.setOnKeyPressed(event -> {
    //         if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE){
    //             mainController.activeShowSysHosts();
    //             mainController.requestFocus();
    //         }
    //     });
    //     HBox navButtons = new HBox(0, backButton, forwardButton, homeButton);
    //     ToggleButton listButton = new ToggleButton();
    //     listButton.setId("list");
    //     listButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
    //     HBox.setMargin(listButton, new Insets(0, 0, 0, 7));
    //     ToggleButton searchButton = new ToggleButton();
    //     searchButton.setId("search");
    //     searchButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
    //     backButton.setGraphic(new Region());
    //     forwardButton.setGraphic(new Region());
    //     homeButton.setGraphic(new Region());
    //     listButton.setGraphic(new Region());
    //     searchButton.setGraphic(new Region());
    //     toolBar.addLeftItems(navButtons, listButton);
    // }

    private void initSysMenu() {
        menuBar = new MenuBar();
        if (SystemUtil.CURRENT_OS == EnumOS.MacOS) {
            menuBar.setUseSystemMenuBar(true);
        }
        Menu fileMenu = new Menu(ResourceBundleUtil.getString("key.menu-file"));
        // 刷新可选文件
        MenuItem refreshFileTree = new MenuItem(ResourceBundleUtil.getString("key.menu-file-refresh"));
        refreshFileTree.setAccelerator(
                KeyCombination.keyCombination(KeyCode.SHORTCUT.getName() + "+" + KeyCode.R.getName()));
        refreshFileTree.setOnAction(event -> mainController.refreshFileTree());
        // 下载文件
        MenuItem downloadRemoteFile = new MenuItem(ResourceBundleUtil.getString("key.menu-download-remote"));
        downloadRemoteFile.setAccelerator(
                KeyCombination.keyCombination(KeyCode.SHORTCUT.getName() + "+" + KeyCode.D.getName()));
        downloadRemoteFile.setOnAction(event -> {
            try {
                Downloader.downloadAndWriteAll();
            } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
                DialogUtils.createExceptionDialog("下载文件异常", e);
            }
            refreshFileTree.fire();
        });
        // 清空远程文件，并下载
        MenuItem clearRemoteFile = new MenuItem(ResourceBundleUtil.getString("key.menu-clear-remote-file"));
        clearRemoteFile.setAccelerator(
                KeyCombination.keyCombination(
                        KeyCode.SHORTCUT.getName() + "+" + KeyCode.SHIFT.getName() + "+" + KeyCode.D.getName()));
        clearRemoteFile.setOnAction(event -> {
            try {
                Downloader.clearDownloadFolder();
                downloadRemoteFile.fire();
            } catch (IOException e) {
                DialogUtils.createExceptionDialog("清空远程文件发生异常", e);
            }
        });
        // 打开用户目录
        MenuItem openConfigFolderMenu = new MenuItem(ResourceBundleUtil.getString("key.menu-file-open-config"));
        openConfigFolderMenu.setAccelerator(
                KeyCombination.keyCombination(
                        KeyCode.SHORTCUT.getName() + "+" + KeyCode.SEMICOLON.getName()));
        openConfigFolderMenu.setOnAction(event -> openFile(Config.getWorkFolder()));

        fileMenu.getItems().addAll(
                refreshFileTree,
                new SeparatorMenuItem(),
                downloadRemoteFile,
                clearRemoteFile,
                new SeparatorMenuItem(),
                openConfigFolderMenu,
                new SeparatorMenuItem()
        );
        menuBar.getMenus().add(fileMenu);
        Menu optionsMenu = new Menu(ResourceBundleUtil.getString("key.menu-edit"));

        MenuItem preferencesMenuItem = new MenuItem(ResourceBundleUtil.getString("key.menu-preferences"));
        preferencesMenuItem.setAccelerator(
                KeyCombination.keyCombination(KeyCode.SHORTCUT.getName() + "+" + KeyCode.COMMA.getName()));
        preferencesMenuItem.setOnAction(event -> {
            // gotoPreferencesPage();
            // 打开配置目录，自己修改
            openFile(Config.getUserSettingFile());
        });
        optionsMenu.getItems().addAll(
                // new SeparatorMenuItem(),
                preferencesMenuItem
        );
        menuBar.getMenus().add(optionsMenu);
        root.getChildren().add(menuBar);
    }

    private void openFile(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                DialogUtils.createExceptionDialog("打开文件错误", e);
            }
        } else {
            DialogUtils.createAlert("", "当前系统不支持打开系统资源管理器,请手动打开路径"
                    + file.toString(), AlertType.INFORMATION);
        }
    }

    /**
     * 打开主页面
     */
    private void gotoMainPage() {
        if (mainController == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setLocation(EasyHostsApp.class.getClassLoader().getResource(MAIN_PAGE_FXML));
            loader.setResources(ResourceBundleUtil.getBundle());
            try {
                root.getChildren().add(0, loader.load());
                mainController = loader.getController();
                mainController.setCallbackObjectProperty(callBackProperty());
            } catch (Exception e) {
                Platform.runLater(() -> DialogUtils.createExceptionDialog("加载主页异常", e));
            }
        } else {
            mainController.getRoot().setVisible(true);
        }
        if (preferencesController != null) {
            preferencesController.getRoot().setVisible(false);
        }
    }

    /**
     * 打开配置页
     */
    private void gotoPreferencesPage() {
        if (preferencesController == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setResources(ResourceBundleUtil.getBundle());
            loader.setLocation(EasyHostsApp.class.getClassLoader().getResource(PREFERENCES_PAGE_FXML));
            try {
                root.getChildren().add(1, loader.load());
                preferencesController = loader.getController();
            } catch (Exception e) {
                Platform.runLater(() -> DialogUtils.createExceptionDialog("加载配置页异常", e));
            }
        } else {
            preferencesController.getRoot().setVisible(true);
        }
        mainController.getRoot().setVisible(false);
    }

    @Override
    public void start(Stage stage) {
        scene = new Scene(root, 720, 480, Color.BLACK);
        scene.getStylesheets().setAll("/css/HostsHelper.css");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setTitle(ResourceBundleUtil.getString("key.app-title"));
        stage.show();
        initAdminPasswordDialog();
    }
}
