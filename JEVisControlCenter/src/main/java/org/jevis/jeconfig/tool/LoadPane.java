/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.tool;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

/**
 *
 * @author fs
 */
public class LoadPane {

    private final ProgressIndicator pi = new ProgressIndicator();
    Thread animation = null;
    final private StackPane _view = new StackPane();
    private Node content;

    public LoadPane(boolean load) {
        super();

        pi.setMaxWidth(50d);
        pi.setMinHeight(50d);

        if (load) {
            startLoading();
        } else {
            _view.getChildren().add(content);
        }
    }

    public void setContent(Node node) {
        content = node;
    }

    public Node getView() {
        return _view;
    }

    public void startLoading() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                _view.getChildren().remove(content);
                _view.getChildren().add(pi);

            }
        });

//        animation = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//
//                } catch (InterruptedException ex) {
//
//                }
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        pi.setProgress(-1);
//                    }
//                });
//            }
//        };
//        animation.start();
    }

    public void stopLoading() {
        try {

            if (animation != null) {
                animation.interrupt();
            }
//        _view.getChildren().removeAll();
//        System.out.println("view.content: " + _view.getChildren().size());
//        _view.getChildren().add(content);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
//                getChildren().removeAll();

//                if (animation != null) {
//                    animation.interrupt();
//                }
                    _view.getChildren().removeAll(content, pi);
                    System.out.println("view.content: " + _view.getChildren().size());
                    _view.getChildren().add(content);

                }
            });

        } catch (Exception ex) {

        }
    }
}
