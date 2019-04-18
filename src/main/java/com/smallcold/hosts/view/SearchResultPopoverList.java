/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.smallcold.hosts.view;

import com.smallcold.hosts.conf.ResourceBundleUtil;
import com.smallcold.hosts.operate.HostsOperatorFactory;
import com.smallcold.hosts.view.controller.HostsSearchResult;
import com.smallcold.hosts.view.controller.MainController;
import com.smallcold.hosts.view.controller.Popover;
import com.smallcold.hosts.view.controller.PopoverTreeList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Popover page that displays a list of search results.
 *
 * @author smallcold
 */
public class SearchResultPopoverList extends PopoverTreeList<HostsSearchResult> implements Popover.Page {
    private Popover popover;
    private MainController pageBrowser;
    private final Pane backgroundRectangle = new Pane();

    public SearchResultPopoverList(MainController pageBrowser) {
        this.pageBrowser = pageBrowser;
        setFocusTraversable(false);
        backgroundRectangle.setId("PopoverBackground");
        setPlaceholder(backgroundRectangle);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        backgroundRectangle.resize(getWidth(), getHeight());
    }

    @Override
    public void itemClicked(HostsSearchResult result, boolean isSwitch) {
        popover.hide();
        pageBrowser.getToItem(result, isSwitch);
    }

    @Override
    public void setPopover(Popover popover) {
        this.popover = popover;
    }

    @Override
    public Popover getPopover() {
        return popover;
    }

    @Override
    public Node getPageNode() {
        return this;
    }

    @Override
    public String getPageTitle() {
        return ResourceBundleUtil.getString("key.search-title");
    }

    @Override
    public String leftButtonText() {
        return null;
    }

    @Override
    public void handleLeftButton() {
    }

    @Override
    public String rightButtonText() {
        return null;
    }

    @Override
    public void handleRightButton() {
    }

    @Override
    public void handleShown() {
    }

    @Override
    public void handleHidden() {
    }

    @Override
    public ListCell<HostsSearchResult> call(ListView<HostsSearchResult> p) {
        return new SearchResultListCell();
    }

    private class SearchResultListCell extends ListCell<HostsSearchResult>
            implements Skin<SearchResultListCell>, EventHandler {
        private static final int TEXT_GAP = 6;
        private ImageView arrow = new ImageView(RIGHT_ARROW);
        private Label title = new Label();
        private Label icon = new Label();
        private Label details = new Label();
        private int cellIndex;
        private Rectangle topLine = new Rectangle(0, 0, 1, 1);

        private SearchResultListCell() {
            super();
            //System.out.println("CREATED TimeSlot CELL " + (cellIndex));
            // we don't need any of the labeled functionality of the default cell skin, so we replace skin with our own
            // in this case using this same class as it saves memory. This skin is very simple its just a HBox container
            setSkin(this);
            getStyleClass().setAll("search-result-cell");
            title.getStyleClass().setAll("title");
            icon.getStyleClass().setAll("icon");
            details.getStyleClass().setAll("details");
            topLine.setFill(Color.web("#dfdfdf"));
            getChildren().addAll(arrow, title, icon, details, topLine);
            setOnMouseClicked(this);

            // listen to changes of this cell being added and removed from list
            // and when it or its parent is moved. If any of those things change
            // then update the iconPane's layout. requestLayout() will be called
            // many times for any change of cell layout in the list but that
            // dosn't matter as they will all be batched up by layout machanisim
            // and iconPane.layoutChildren() will only be called once per frame.
        }

        @Override
        protected double computeMinWidth(double height) {
            final Insets insets = getInsets();
            final double h = height = insets.getBottom() - insets.getTop();
            return (int) ((insets.getLeft() + title.minWidth(h) + TEXT_GAP + details.minWidth(h) + insets.getRight())
                    + 0.5d);
        }

        @Override
        protected double computePrefWidth(double height) {
            final Insets insets = getInsets();
            final double h = height = insets.getBottom() - insets.getTop();
            return (int) ((insets.getLeft() + title.prefWidth(h) + TEXT_GAP + details.prefWidth(h) + insets.getRight())
                    + 0.5d);
        }

        @Override
        protected double computeMaxWidth(double height) {
            final Insets insets = getInsets();
            final double h = height = insets.getBottom() - insets.getTop();
            return (int) ((insets.getLeft() + title.maxWidth(h) + TEXT_GAP + details.maxWidth(h) + insets.getRight())
                    + 0.5d);
        }

        @Override
        protected double computeMinHeight(double width) {
            final Insets insets = getInsets();
            final double w = width - insets.getLeft() - insets.getRight();
            return (int) ((insets.getTop() + title.minHeight(w) + TEXT_GAP + details.minHeight(w) + insets.getBottom())
                    + 0.5d);
        }

        @Override
        protected double computePrefHeight(double width) {
            final Insets insets = getInsets();
            final double w = width - insets.getLeft() - insets.getRight();
            return (int) (
                    (insets.getTop() + title.prefHeight(w) + TEXT_GAP + details.prefHeight(w) + insets.getBottom())
                            + 0.5d);
        }

        @Override
        protected double computeMaxHeight(double width) {
            final Insets insets = getInsets();
            final double w = width - insets.getLeft() - insets.getRight();
            return (int) ((insets.getTop() + title.maxHeight(w) + TEXT_GAP + details.maxHeight(w) + insets.getBottom())
                    + 0.5d);
        }

        @Override
        protected void layoutChildren() {
            final Insets insets = getInsets();
            final double left = insets.getLeft();
            final double top = insets.getTop();
            final double w = getWidth() - left - insets.getRight();
            final double h = getHeight() - top - insets.getBottom();
            final double titleHeight = title.prefHeight(w);
            icon.setLayoutX(left);
            icon.setLayoutY(top);
            icon.resize(50, titleHeight);
            title.setLayoutX(left + icon.getWidth() + TEXT_GAP);
            title.setLayoutY(top);
            title.resize(w - icon.getWidth() - TEXT_GAP, titleHeight);
            final double detailsHeight = details.prefHeight(w);
            details.setLayoutX(left);
            details.setLayoutY(top + titleHeight + TEXT_GAP);
            details.resize(w, detailsHeight);
            final Bounds arrowBounds = arrow.getLayoutBounds();
            arrow.setLayoutX(getWidth() - arrowBounds.getWidth() - 24);
            arrow.setLayoutY((int) ((getHeight() - arrowBounds.getHeight()) / 2d));
            topLine.setLayoutX(left - 5);
            topLine.setWidth(getWidth() - left + 5);
        }

        // CELL METHODS
        @Override
        protected void updateItem(HostsSearchResult result, boolean empty) {
            super.updateItem(result, empty);
            if (result == null) { // empty item
                arrow.setVisible(false);
                icon.setVisible(false);
                title.setVisible(false);
                details.setVisible(false);
            } else {
                arrow.setVisible(true);
                title.setVisible(true);
                icon.setVisible(true);
                details.setVisible(true);
                title.setText(result.getLine());
                if (result.getHostsOperator() == HostsOperatorFactory.getSystemHostsOperator()) {
                    icon.getStyleClass().setAll("icon-system");
                } else if (result.getHostsOperator() == HostsOperatorFactory.getCommonHostsOperator()) {
                    icon.getStyleClass().setAll("icon-common");
                } else {
                    icon.getStyleClass().setAll("icon");
                }
                icon.setText(result.getHostsOperator().getName());
                details.setText(result.getLine());
            }
        }

        // SKIN METHODS
        @Override
        public SearchResultListCell getSkinnable() {
            return this;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void handle(Event t) {
            itemClicked(getItem(), false);
        }
    }
}