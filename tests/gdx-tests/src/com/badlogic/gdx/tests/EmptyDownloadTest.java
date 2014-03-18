package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class EmptyDownloadTest extends GdxTest {

	@Override
	public void create () {
		HttpRequest request = new HttpRequest(HttpMethods.GET);
		request.setUrl("http://www.google.at");
		Gdx.net.sendHttpRequest(request, null);
	}
}