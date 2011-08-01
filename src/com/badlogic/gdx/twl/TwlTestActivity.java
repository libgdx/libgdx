/**
 * 
 */
package com.badlogic.gdx.twl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author Kurtis Kopf
 */
public class TwlTestActivity extends Activity
{

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button twlButton = (Button)findViewById(R.id.button1);
		twlButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(v.getContext(),ButtonTestAndroid.class);
				startActivityForResult(myIntent, 0);
			}
		});
		
		Button twlTextArea = (Button)findViewById(R.id.button2);
		twlTextArea.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(v.getContext(),TextAreaTestAndroid.class);
				startActivityForResult(myIntent, 0);
			}
		});
		
		Button twlNode = (Button)findViewById(R.id.button3);
		twlNode.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(v.getContext(),NodeTestAndroid.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}
}