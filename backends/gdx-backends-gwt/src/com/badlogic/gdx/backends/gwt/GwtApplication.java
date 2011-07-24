package com.badlogic.gdx.backends.gwt;

import gwt.g2d.client.util.FpsTimer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public abstract class GwtApplication implements EntryPoint {
	private ApplicationListener listener;
	private GwtApplicationConfiguration config;
	private GwtGraphics graphics;
	
	@Override
	public void onModuleLoad() {
		this.listener = getApplicationListener();
		this.config = getConfig();

		graphics = new GwtGraphics(config);
		Gdx.graphics = graphics;
		Gdx.gl20 = graphics.getGL20();
		Gdx.gl = graphics.getGLCommon();
		
		setupLoop();
	}
	
	private void setupLoop() {
		// tell listener about app creation
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());
		
		// add resize handler
		graphics.surface.addHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				GwtApplication.this.listener.resize(event.getWidth(), event.getHeight());
			}
		}, ResizeEvent.getType());
		
		// setup rendering timer
		FpsTimer timer = new FpsTimer(config.fps) {
			@Override
			public void update() {
				listener.render();
			}
		};
		timer.start();
	}

	public abstract GwtApplicationConfiguration getConfig();
	public abstract ApplicationListener getApplicationListener();
}
