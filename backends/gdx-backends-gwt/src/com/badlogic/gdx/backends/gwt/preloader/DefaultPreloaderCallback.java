/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt.preloader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

public class DefaultPreloaderCallback implements Preloader.PreloaderCallback {

    private static final String PRELOADER_ID = "gdx-preloader";

    private final Panel preloaderPanel;

    private final Style meterStyle;
    private final Panel meterPanel;

    public DefaultPreloaderCallback(Panel panel) {

        preloaderPanel = new VerticalPanel();
        preloaderPanel.getElement().setId(PRELOADER_ID);
        preloaderPanel.setStyleName("gdx-preloader");
        Image logo = new Image(GWT.getModuleBaseURL() + "logo.png");
        logo.setStyleName("logo");
        preloaderPanel.add(logo);
        meterPanel = new SimplePanel();
        meterPanel.setStyleName("gdx-meter");
        meterPanel.addStyleName("red");
        InlineHTML meter = new InlineHTML();
        meterStyle = meter.getElement().getStyle();
        meterStyle.setWidth(0, Style.Unit.PCT);
        meterPanel.add(meter);
        preloaderPanel.add(meterPanel);

        Element element = Document.get().getElementById("embed-" + GWT.getModuleName());
        if (element != null) {
            element.appendChild(preloaderPanel.getElement());
        } else {
            panel.add(preloaderPanel);
        }
    }

    @Override
    public void error (String file) {
        System.out.println("error: " + file);
    }

    @Override
    public void update (Preloader.PreloaderState state) {
        meterStyle.setWidth(100f * state.getProgress(), Style.Unit.PCT);
        if (state.hasEnded()) {
            Document.get().getElementById(PRELOADER_ID).removeFromParent();
        }
    }
}
