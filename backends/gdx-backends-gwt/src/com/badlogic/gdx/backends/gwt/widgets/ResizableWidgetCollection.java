/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.badlogic.gdx.backends.gwt.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;

/** A collection of {@link ResizableWidget} that periodically checks the outer dimensions of a widget and redraws it as necessary.
 * Every {@link ResizableWidgetCollection} uses a timer, so consider the cost when adding one.
 * 
 * Typically, a {@link ResizableWidgetCollection} is only needed if you expect your widgets to resize based on window resizing or
 * other events. Fixed sized Widgets do not need to be added to a {@link ResizableWidgetCollection} as they cannot be resized. */
public class ResizableWidgetCollection implements ResizeHandler, Iterable<ResizableWidget> {
	/** Information about a widgets size. */
	static class ResizableWidgetInfo {

		private ResizableWidget widget;
		private int curOffsetHeight = 0;
		private int curOffsetWidth = 0;
		private int curClientHeight = 0;
		private int curClientWidth = 0;

		/** Constructor.
		 * 
		 * @param widget the widget that will be monitored */
		public ResizableWidgetInfo (ResizableWidget widget) {
			this.widget = widget;
			updateSizes();
		}

		public int getClientHeight () {
			return curClientHeight;
		}

		public int getClientWidth () {
			return curClientWidth;
		}

		public int getOffsetHeight () {
			return curOffsetHeight;
		}

		public int getOffsetWidth () {
			return curOffsetWidth;
		}

		/** Update the current sizes.
		 * 
		 * @return true if the sizes changed, false if not. */
		public boolean updateSizes () {
			int offsetWidth = widget.getElement().getOffsetWidth();
			int offsetHeight = widget.getElement().getOffsetHeight();
			int clientWidth = widget.getElement().getClientWidth();
			int clientHeight = widget.getElement().getClientHeight();
			if (offsetWidth != curOffsetWidth || offsetHeight != curOffsetHeight || clientWidth != curClientWidth
				|| clientHeight != curClientHeight) {
				this.curOffsetWidth = offsetWidth;
				this.curOffsetHeight = offsetHeight;
				this.curClientWidth = clientWidth;
				this.curClientHeight = clientHeight;
				return true;
			}

			return false;
		}
	}

	/** The default delay between resize checks in milliseconds. */
	private static final int DEFAULT_RESIZE_CHECK_DELAY = 400;

	/** A static {@link ResizableWidgetCollection} that can be used in most cases. */
	private static ResizableWidgetCollection staticCollection = null;

	/** Get the globally accessible {@link ResizableWidgetCollection}. In most cases, the global collection can be used for all
	 * {@link ResizableWidget}s.
	 * 
	 * @return the global {@link ResizableWidgetCollection} */
	public static ResizableWidgetCollection get () {
		if (staticCollection == null) {
			staticCollection = new ResizableWidgetCollection();
		}
		return staticCollection;
	}

	/** The timer used to periodically compare the dimensions of elements to their old dimensions. */
	private Timer resizeCheckTimer = new Timer() {
		@Override
		public void run () {
			// Ignore changes that result from window resize events
			if (windowHeight != Window.getClientHeight() || windowWidth != Window.getClientWidth()) {
				windowHeight = Window.getClientHeight();
				windowWidth = Window.getClientWidth();
				schedule(resizeCheckDelay);
				return;
			}

			// Look for elements that have new dimensions
			checkWidgetSize();

			// Start checking again
			if (resizeCheckingEnabled) {
				schedule(resizeCheckDelay);
			}
		}
	};

	/** A hash map of the resizable widgets this collection is checking. */
	private Map<ResizableWidget, ResizableWidgetInfo> widgets = new HashMap<ResizableWidget, ResizableWidgetInfo>();

	/** The current window height. */
	int windowHeight = 0;

	/** The current window width. */
	int windowWidth = 0;

	/** The hook used to remove the window handler. */
	private HandlerRegistration windowHandler;

	/** The delay between resize checks. */
	int resizeCheckDelay = DEFAULT_RESIZE_CHECK_DELAY;

	/** A boolean indicating that resize checking should run. */
	boolean resizeCheckingEnabled;

	/** Create a ResizableWidget. */
	public ResizableWidgetCollection () {
		this(DEFAULT_RESIZE_CHECK_DELAY);
	}

	/** Constructor.
	 * 
	 * @param resizeCheckingEnabled false to disable resize checking */
	public ResizableWidgetCollection (boolean resizeCheckingEnabled) {
		this(DEFAULT_RESIZE_CHECK_DELAY, resizeCheckingEnabled);
	}

	/** Constructor.
	 * 
	 * @param resizeCheckDelay the delay between checks in milliseconds */
	public ResizableWidgetCollection (int resizeCheckDelay) {
		this(resizeCheckDelay, true);
	}

	/** Constructor. */
	protected ResizableWidgetCollection (int resizeCheckDelay, boolean resizeCheckingEnabled) {
		setResizeCheckDelay(resizeCheckDelay);
		setResizeCheckingEnabled(resizeCheckingEnabled);
	}

	/** Add a resizable widget to the collection.
	 * 
	 * @param widget the resizable widget to add */
	public void add (ResizableWidget widget) {
		widgets.put(widget, new ResizableWidgetInfo(widget));
	}

	/** Check to see if any Widgets have been resized and call their handlers appropriately. */
	public void checkWidgetSize () {
		for (Map.Entry<ResizableWidget, ResizableWidgetInfo> entry : widgets.entrySet()) {
			ResizableWidget widget = entry.getKey();
			ResizableWidgetInfo info = entry.getValue();

			// Call the onResize method only if the widget is attached
			if (info.updateSizes()) {
				// Check that the offset width and height are greater than 0.
				if (info.getOffsetWidth() > 0 && info.getOffsetHeight() > 0 && widget.isAttached()) {
					// Send the client dimensions, which is the space available for
					// rendering.
					widget.onResize(info.getOffsetWidth(), info.getOffsetHeight());
				}
			}
		}
	}

	/** Get the delay between resize checks in milliseconds.
	 * 
	 * @return the resize check delay */
	public int getResizeCheckDelay () {
		return resizeCheckDelay;
	}

	/** Check whether or not resize checking is enabled.
	 * 
	 * @return true is resize checking is enabled */
	public boolean isResizeCheckingEnabled () {
		return resizeCheckingEnabled;
	}

	public Iterator<ResizableWidget> iterator () {
		return widgets.keySet().iterator();
	}

	/** Remove a {@link ResizableWidget} from the collection.
	 * 
	 * @param widget the widget to remove */
	public void remove (ResizableWidget widget) {
		widgets.remove(widget);
	}

	/** Set the delay between resize checks in milliseconds.
	 * 
	 * @param resizeCheckDelay the new delay */
	public void setResizeCheckDelay (int resizeCheckDelay) {
		this.resizeCheckDelay = resizeCheckDelay;
	}

	/** Set whether or not resize checking is enabled. If disabled, elements will still be resized on window events, but the timer
	 * will not check their dimensions periodically.
	 * 
	 * @param enabled true to enable the resize checking timer */
	public void setResizeCheckingEnabled (boolean enabled) {
		if (enabled && !resizeCheckingEnabled) {
			resizeCheckingEnabled = true;
			if (windowHandler == null) {
				windowHandler = Window.addResizeHandler(this);
			}
			resizeCheckTimer.schedule(resizeCheckDelay);
		} else if (!enabled && resizeCheckingEnabled) {
			resizeCheckingEnabled = false;
			if (windowHandler != null) {
				windowHandler.removeHandler();
				windowHandler = null;
			}
			resizeCheckTimer.cancel();
		}
	}

	/** Inform the {@link ResizableWidgetCollection} that the size of a widget has changed and already been redrawn. This will
	 * prevent the widget from being redrawn on the next loop.
	 * 
	 * @param widget the widget's size that changed */
	public void updateWidgetSize (ResizableWidget widget) {
		if (!widget.isAttached()) {
			return;
		}

		ResizableWidgetInfo info = widgets.get(widget);
		if (info != null) {
			info.updateSizes();
		}
	}

	/** Called when the browser window is resized.
	 *
	 */
	@Override
	public void onResize (ResizeEvent event) {
		checkWidgetSize();
	}

}
