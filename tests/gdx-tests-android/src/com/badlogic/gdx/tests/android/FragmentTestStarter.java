package com.badlogic.gdx.tests.android;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;

public class FragmentTestStarter extends FragmentActivity implements AndroidFragmentApplication.Callbacks {

	FrameLayout list;
	FrameLayout view;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GdxTests.tests.add(MatrixTest.class);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		list = new FrameLayout(this);
		list.setId(1);
		list.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.addView(list);
		
		list.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));

		view = new FrameLayout(this);
		view.setId(2);
		view.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 2));
		layout.addView(view);

		setContentView(layout);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(1, new TestListFragment()).commit();
		}
	}
	
	public void onTestSelected (String testName) {
		if(view != null) {
			getSupportFragmentManager().beginTransaction().replace(2, TestViewFragment.newInstance(testName)).commit();
		} else {
			startActivity(new Intent(this, GdxTestActivity.class).putExtra("test", testName));
		}
	}
	
	@Override
	public void exit () {
	}
   
	public static class TestListFragment extends ListFragment {
		
		private SharedPreferences prefs;
		private FragmentTestStarter activity;
		
		@Override
		public void onCreate (Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			List<String> testNames = GdxTests.getNames();
			setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, testNames));			
			prefs = getActivity().getSharedPreferences("libgdx-tests", Context.MODE_PRIVATE);
		}		
		
		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			((ListView)view.findViewById(android.R.id.list)).setSelectionFromTop(prefs.getInt("index", 0), prefs.getInt("top", 0));
			return view;
		}

		@Override
		public void onListItemClick (ListView listView, View view, int position, long id) {
			super.onListItemClick(listView, view, position, id);

			Object o = this.getListAdapter().getItem(position);
			String testName = o.toString();
			if (activity != null) {
				activity.onTestSelected(testName);
			}
		}		
		
		
		@Override
		public void onAttach (Activity activity) {
			super.onAttach(activity);
			if (activity instanceof FragmentTestStarter) {
				this.activity = (FragmentTestStarter)activity;
			}
		}
		
	}

	public static class TestViewFragment extends AndroidFragmentApplication {

		public static TestViewFragment newInstance(String testName) {
			Bundle arguments = new Bundle();
			arguments.putString("test", testName);
			TestViewFragment fragment = new TestViewFragment();
			fragment.setArguments(arguments);
			return fragment;
		}
		
		GdxTest test;
		
		@Override
		public void onCreate (Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			test = GdxTests.newTest(getArguments().getString("test"));
		}

		@Override
		public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
			config.useImmersiveMode = true;
			return initializeForView(test, config);
		}
		
	}


}
