
package com.badlogic.gdx.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GdxRemote extends Activity {
	
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button connect = (Button)findViewById(R.id.connect);
		connect.setOnClickListener(new OnClickListener() {			
			@Override public void onClick (View v) {
				Bundle bundle = new Bundle();
				bundle.putString("ip", "192.168.1.66");
				bundle.putInt("port", 8190); //
				Intent intent = new Intent(GdxRemote.this, UxAndroid.class);
				intent.putExtras(bundle);
				startActivity(intent);				
			}
		});
	}
}
