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

package com.badlogic.gdx.remote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class GdxRemote extends Activity {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final SharedPreferences prefs = getSharedPreferences("ip-settings", 0);
		String ip = prefs.getString("ip", "192.168.1.66");
		String port = prefs.getString("port", "8190");

		final EditText ipText = (EditText)findViewById(R.id.ip);
		final EditText portText = (EditText)findViewById(R.id.port);
		ipText.setText(ip);
		portText.setText(port);

		Button connect = (Button)findViewById(R.id.connect);
		connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				Editor edit = prefs.edit();
				edit.putString("ip", ipText.getText().toString());
				edit.putString("port", portText.getText().toString());
				edit.commit();

				Bundle bundle = new Bundle();
				bundle.putString("ip", ipText.getText().toString());
				bundle.putString("port", portText.getText().toString()); //
				Intent intent = new Intent(GdxRemote.this, UxAndroid.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
}
