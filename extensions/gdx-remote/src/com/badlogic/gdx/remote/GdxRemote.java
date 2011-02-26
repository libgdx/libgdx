
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
	
	@Override public void onCreate (Bundle savedInstanceState) {
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
			@Override public void onClick (View v) {
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
