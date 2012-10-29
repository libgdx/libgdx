/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Modified by Elijah Cornell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.android;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.utils.Pool;

/**
 * An implementation of the {@link Input} interface for Android.
 * 
 * @author mzechner
 * 
 */
public final class AndroidInputLW extends AndroidInputBase implements Input, SensorEventListener {

	private final AndroidTouchHandlerLW touchHandler;

	public AndroidInputLW(AndroidApplicationLW activity,
			AndroidApplicationConfiguration config) {
	    this.app = activity;
		this.config = config;

		for (int i = 0; i < realId.length; i++)
			realId[i] = -1;
		this.app = activity;
		this.sleepTime = config.touchSleepTime;

		touchHandler = new AndroidSingleTouchHandlerLW();
		hasMultitouch = false;

		vibrator = (Vibrator) activity.getService().getSystemService(
				Context.VIBRATOR_SERVICE);

		int rotation = getRotation();
        DisplayMode mode = ((AndroidApplicationLW)app).graphics.getDesktopDisplayMode();
        if (((rotation == 0 || rotation == 180) && (mode.width >= mode.height))
            || ((rotation == 90 || rotation == 270) && (mode.width <= mode.height))) {
            nativeOrientation = Orientation.Landscape;
        } else {
            nativeOrientation = Orientation.Portrait;
        }
	}

	@Override
	public float getAccelerometerX() {
		return accelerometerValues[0];
	}

	@Override
	public float getAccelerometerY() {
		return accelerometerValues[1];
	}

	@Override
	public float getAccelerometerZ() {
		return accelerometerValues[2];
	}

	protected void processEvents() {
		synchronized (this) {
			justTouched = false;

			if (processor != null) {

				if (this.processor instanceof InputProcessorLW) {
					final InputProcessorLW processor = (InputProcessorLW)this.processor;

					int len = touchEvents.size();
	                for (int i = 0; i < len; i++) {
	                    TouchEvent e = touchEvents.get(i);
	                    currentEventTimeStamp = e.timeStamp;
	                    switch (e.type) {
	                    case TouchEvent.TOUCH_DOWN:
	                        processor.touchDown(e.x, e.y, e.pointer, Buttons.LEFT);
	                        justTouched = true;
	                        break;
	                    case TouchEvent.TOUCH_UP:
	                        processor.touchUp(e.x, e.y, e.pointer, Buttons.LEFT);
	                        break;
	                    case TouchEvent.TOUCH_DRAGGED:
	                        processor.touchDragged(e.x, e.y, e.pointer);
	                    }
	                    usedTouchEvents.free(e);
	                }
				}
			} else {
                int len = touchEvents.size();
                for (int i = 0; i < len; i++) {
                    TouchEvent e = touchEvents.get(i);
                    if (e.type == TouchEvent.TOUCH_DOWN) justTouched = true;
                    usedTouchEvents.free(e);
                }
			}
            if (touchEvents.size() == 0)
            {
                for (int i = 0; i < deltaX.length; i++)
                {
                    deltaX[0] = 0;
                    deltaY[0] = 0;
                }
            }

            touchEvents.clear();
		}
	}

	boolean requestFocus = true;

	public boolean onTap(int pX, int pY) {

		// synchronized in handler.postTouchEvent()
		touchHandler.onTap(pX, pY, this);

		if (sleepTime != 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		return true;
	}

	public boolean onDrop(int pX, int pY) {

		// synchronized in handler.postTouchEvent()
		touchHandler.onDrop(pX, pY, this);

		if (sleepTime != 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			System.arraycopy(event.values, 0, accelerometerValues, 0,
					accelerometerValues.length);
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.arraycopy(event.values, 0, magneticFieldValues, 0,
					magneticFieldValues.length);
		}
	}

	protected void registerSensorListeners() {
		if (config.useAccelerometer) {
			manager = (SensorManager) ((AndroidApplicationLW)app).getService().getSystemService(
					Context.SENSOR_SERVICE);
			if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
				accelerometerAvailable = false;
			} else {
				Sensor accelerometer = manager.getSensorList(
						Sensor.TYPE_ACCELEROMETER).get(0);
				accelerometerAvailable = manager.registerListener(this,
						accelerometer, SensorManager.SENSOR_DELAY_GAME);
			}
		} else
			accelerometerAvailable = false;

		if (config.useCompass) {
			if (manager == null)
				manager = (SensorManager) ((AndroidApplicationLW)app).getService().getSystemService(
						Context.SENSOR_SERVICE);
			Sensor sensor = manager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensor != null) {
				compassAvailable = accelerometerAvailable;
				if (compassAvailable) {
					compassAvailable = manager.registerListener(this, sensor,
							SensorManager.SENSOR_DELAY_GAME);
				}
			} else {
				compassAvailable = false;
			}
		} else
			compassAvailable = false;
		Gdx.app.log("AndroidInput", "sensor listener setup");
	}

	protected void unregisterSensorListeners() {
		if (manager != null) {
			manager.unregisterListener(this);
			manager = null;
		}
		Gdx.app.log("AndroidInput", "sensor listener tear down");
	}

	@Override
	public void getTextInput(TextInputListener arg0, String arg1, String arg2) {
	}

	@Override
	public boolean isKeyPressed(int arg0) {
		return false;
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean arg0) {
	}

    @Override
    public void getPlaceholderTextInput(TextInputListener listener, String title, String placeholder)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCatchMenuKey(boolean catchMenu)
    {
    }

    @Override
    public int getRotation()
    {
        int orientation = ((WindowManager) 
                ((AndroidApplicationLW)app).service.getApplication().getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (orientation) {
        case Surface.ROTATION_0:
            return 0;
        case Surface.ROTATION_90:
            return 90;
        case Surface.ROTATION_180:
            return 180;
        case Surface.ROTATION_270:
            return 270;
        default:
            return 0;
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
