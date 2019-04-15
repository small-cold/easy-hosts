package com.smallcold.hosts.view;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/*
 * Created by smallcold on 2017/9/5.
 */
public class DialogUtils {

    private static Logger logger = Logger.getLogger(DialogUtils.class);

    public static Dialog<ButtonType> createExceptionDialog(String title, Throwable th) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        final DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContentText("详情:");
        dialogPane.getButtonTypes().addAll(ButtonType.OK);
        dialogPane.setContentText(th.getMessage());
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label("Exception stacktrace:");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        pw.close();

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane root = new GridPane();
        root.setVisible(false);
        root.setMaxWidth(Double.MAX_VALUE);
        root.add(label, 0, 0);
        root.add(textArea, 0, 1);
        dialogPane.setExpandableContent(root);
        dialog.showAndWait().filter(response -> response == ButtonType.OK)
                .ifPresent(response -> logger.warn("The alert was approved"));
        // dialog.setOnHidden(event -> {
        //     logger.info(title, th);
        // });
        return dialog;
    }

    public static Alert createAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setHeaderText(title);
        // alert.getDialogPane().setContentText(msg);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> System.out.println("The alert was approved"));
        return alert;
    }
}
