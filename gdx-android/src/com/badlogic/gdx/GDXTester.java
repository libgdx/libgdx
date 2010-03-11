package com.badlogic.gdx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GDXTester extends ListActivity 
{
	String[] items = new String[]{ "Fixed Point Test", "Float Test" };
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState); 
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));        
	}

	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
	
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();

		Intent intent = null;
		if( keyword.equals( items[0] ) )
			intent = new Intent( this, FixedPointTest.class );
		if( keyword.equals( items[1] ) )
			intent = new Intent( this, FloatTest.class );	
			
		startActivity( intent );
	}

}