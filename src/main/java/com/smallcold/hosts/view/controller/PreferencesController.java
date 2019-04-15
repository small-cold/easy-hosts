package com.smallcold.hosts.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import lombok.Getter;

import java.net.URL;
import java.util.ResourceBundle;

/*
 * Created by smallcold on 2017/9/14.
 */
public class PreferencesController implements Initializable {

    @FXML
    public TableView remoteHostsTableView;
    @Getter
    @FXML
    private BorderPane root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
