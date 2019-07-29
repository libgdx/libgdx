package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.PointerIcon;

import com.badlogic.gdx.graphics.Cursor;

public class AndroidCursor implements Cursor {

	Bitmap bmCursor;
	PointerIcon pointerIcon;

	@TargetApi(24)
	AndroidCursor (String drawableName, float xHotspot, float yHotspot, Context c) {
		Resources resources = c.getResources();
		final int resourceId = resources.getIdentifier(drawableName, "drawable", c.getPackageName());
		if (resourceId != 0) {
			bmCursor = BitmapFactory.decodeResource(resources, resourceId);
			pointerIcon = PointerIcon.create(bmCursor,
					Math.min(Math.max(xHotspot,0),1) * (bmCursor.getWidth()-1),
					Math.min(Math.max(yHotspot,0),1) * (bmCursor.getHeight()-1));
		}
	}

	@Override
	public void dispose () {
		bmCursor.recycle();
		bmCursor = null;
		pointerIcon = null;
	}
}
