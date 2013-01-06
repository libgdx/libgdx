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
package aurelienribon.utils;

import aurelienribon.utils.HttpUtils.DownloadListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * A special label used to display the current version of the project. The
 * label can check if any update is available, and update itself according
 * to the result of the check.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class VersionLabel extends JLabel {
	private String version;
	private String checkUrl;
	private String gotoUrl;
	private String blockName;

	/**
	 * Convenience method to call both {@link #init} and {@link #check} methods.
	 */
	public void initAndCheck(String version, String blockName, String checkUrl, String gotoUrl) {
		init(version, blockName, checkUrl, gotoUrl);
		check();
	}

	/**
	 * Initializes the required parameters.
	 */
	public void init(String version, String blockName, String checkUrl, String gotoUrl) {
		this.version = version;
		this.blockName = blockName;
		this.checkUrl = checkUrl;
		this.gotoUrl = gotoUrl;
		setText("v" + version);
	}

	/**
	 * Launches an asynchronous check for updates, using the configurated
	 * checkUrl. The label text and icon will be updated by the result.
	 * Possible results are:
	 * <ul>
	 * <li>No update found</li>
	 * <li>Update found</li>
	 * <li>Connection error</li>
	 * <li>Invalid version file</li>
	 * </ul>
	 */
	public void check() {
		setText("v" + version + " (checking for updates)");
		setIcon(Res.getImage("gfx/ic_loading.gif"));
		if (checkUrl == null) return;

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final DownloadListener listener = new HttpUtils.DownloadListener() {
			@Override
			public void onComplete() {
				String str;

				try {
					str = stream.toString("UTF-8");
					if (blockName != null) str = ParseUtils.parseBlock(str, blockName, version);
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				}

				List<String> versions = TextUtils.splitAndTrim(str);
				int idx = versions.indexOf(version);

				if (idx == 0) {
					setText("v" + version + " (latest version)");
					setIcon(Res.getImage("gfx/ic_ok.png"));
					firePropertyChange("latest", false, true);
				} else if (idx > 0) {
					setText("v" + version + " (new version available! v" + versions.get(0) + ")");
					setIcon(Res.getImage("gfx/ic_warning.png"));
					SwingUtils.addBrowseBehavior(VersionLabel.this, gotoUrl);
					firePropertyChange("newVersion", false, true);
				} else {
					setText("v" + version + " (update check error)");
					setIcon(Res.getImage("gfx/ic_error.png"));
					firePropertyChange("error", false, true);
				}
			}

			@Override
			public void onError(IOException ex) {
				setText("v" + version + " (connection error)");
				setIcon(Res.getImage("gfx/ic_error.png"));
				firePropertyChange("error", false, true);
			}
		};

		Timer timer = new Timer(2000, new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			HttpUtils.downloadAsync(checkUrl, stream, "Version number").addListener(listener);
		}});
		timer.setRepeats(false);
		timer.start();
	}
}