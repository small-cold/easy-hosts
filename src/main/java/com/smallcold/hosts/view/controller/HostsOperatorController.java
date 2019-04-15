package com.smallcold.hosts.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

/*
 * Created by smallcold on 2017/9/4.
 */
public class HostsOperatorController implements Initializable {

    @FXML
    TableView hostTableView = new TableView();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hostTableView = new TableView();
        hostTableView.setEditable(true);
        hostTableView.setMinHeight(400);
        hostTableView.setMinWidth(650);
        hostTableView.autosize();
    }
}
