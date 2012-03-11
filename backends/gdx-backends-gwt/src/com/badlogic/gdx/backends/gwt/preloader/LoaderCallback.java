package com.badlogic.gdx.backends.gwt.preloader;

public interface LoaderCallback<T> {
	public void success(T result);
	public void error();
}
