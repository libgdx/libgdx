
package com.badlogic.gdx.backends.iosrobovm;

/** Represents the bounds inside GL view to which libGDX draws. These bounds may be same as view's dimensions, but may differ in
 * some cases:
 * <ul>
 * <li>Status bar is visible - drawing area is not under the status bar</li>
 * <li>Screen is rotated - in some iOS versions the rotation reporting behavior is different and this needs to be handled</li>
 * </ul>
 *
 * <h3>IMPLEMENTATION & WARNING - Read carefully</h3> Accounting for status bar is not completely clean and relies on a
 * coincidence, related to coordinate system origins. When status bar is present, x and y grows (in practice only y does) to
 * offset the drawing area from the status bar and width and height (and their backBuffer values of course) shrink so the
 * remaining surface fits the rest of the screen.
 *
 * When touch events arrive, IOSInput subtracts x and y from their coordinates to account for the shift.
 *
 * The unclean part is in the actual rendering - since the offset is essentially faked, there is no way to supply it to the libGDX
 * application. But this does not become a problem, as long as Y and HEIGHT add up to the GL view's height (or X and WIDTH,
 * although that is not used in practice), because GL's coordinate system (as far as the glViewport is concerned) starts in the
 * LOWER left corner. So in practice, the rendering part of libGDX can be completely oblivious to any x/y offsets.
 *
 * This may become a problem when interfacing with UIKit, for example when placing banner ADs or using UIKit views over the
 * libGDX's GL view. In such case, overriding {@link IOSApplication#computeBounds()} and providing custom, correct values is
 * recommended. */
public final class IOSScreenBounds {
	/** Offset from top left corner in points */
	public final int x, y;
	/** Dimensions of drawing surface in points */
	public final int width, height;
	/** Dimensions of drawing surface in pixels */
	public final int backBufferWidth, backBufferHeight;

	public IOSScreenBounds (int x, int y, int width, int height, int backBufferWidth, int backBufferHeight) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.backBufferWidth = backBufferWidth;
		this.backBufferHeight = backBufferHeight;
	}
}
