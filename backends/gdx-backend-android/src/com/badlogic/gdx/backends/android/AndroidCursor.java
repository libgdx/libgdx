
package com.badlogic.gdx.backends.android;

import android.os.Build;
import android.view.PointerIcon;
import android.view.View;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AndroidCursor implements Cursor {

	static void setSystemCursor (View view, SystemCursor systemCursor) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			int type;
			switch (systemCursor) { //@off
				case Arrow: type = PointerIcon.TYPE_DEFAULT; break;
				case Ibeam: type = PointerIcon.TYPE_TEXT; break;
				case Crosshair: type = PointerIcon.TYPE_CROSSHAIR; break;
				case Hand: type = PointerIcon.TYPE_HAND; break;
				case HorizontalResize: type = PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW; break;
				case VerticalResize: type = PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW; break;
				case NWSEResize: type = PointerIcon.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW; break;
				case NESWResize: type = PointerIcon.TYPE_TOP_RIGHT_DIAGONAL_DOUBLE_ARROW; break;
				case AllResize: type = PointerIcon.TYPE_ALL_SCROLL; break;
				case NotAllowed: type = PointerIcon.TYPE_NO_DROP; break; // Closest match
				case None: type = PointerIcon.TYPE_NULL; break;
				default: throw new GdxRuntimeException("Unknown system cursor " + systemCursor); //@on
			}
			view.setPointerIcon(PointerIcon.getSystemIcon(view.getContext(), type));
		}
	}

	@Override
	public void dispose () {

	}

}
