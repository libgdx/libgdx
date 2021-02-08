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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSViewApplication;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.annotation.CustomClass;

@CustomClass("MainViewController")
public class MainViewController extends UIViewController {

  private final IOSViewApplication gdxApp;

  public MainViewController() {
    this.gdxApp = new IOSViewApplication(new IosTestWrapper(),
        new IOSApplicationConfiguration());
  }

  @Override
  public void viewDidLoad() {
    super.viewDidLoad();
    gdxApp.init(UIApplication.getSharedApplication(), this);
    getView().addSubview(gdxApp.getUIViewController().getView());
    addChildViewController(gdxApp.getUIViewController());
  }

  @Override
  public void viewDidAppear(boolean b) {
    super.viewDidAppear(b);
    gdxApp.resume();
  }

  @Override
  public void viewWillDisappear(boolean b) {
    super.viewWillDisappear(b);
    gdxApp.pause();
  }

  @Override
  protected void doDispose() {
    super.doDispose();
    gdxApp.dispose();
  }
}
