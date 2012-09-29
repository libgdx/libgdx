/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/** A group that scrolls a child widget using scrollbars and/or mouse or touch dragging.
 * <p>
 * The widget is sized to its preferred size. If the widget's preferred width or height is less than the size of this scroll pane,
 * it is set to the size of this scroll pane. Scrollbars appear when the widget is larger than the scroll pane.
 * <p>
 * The scroll pane's preferred size is that of the child widget. At this size, the child widget will not need to scroll, so the
 * scroll pane is typically sized by ignoring the preferred size in one or both directions.
 * @author mzechner
 * @author Nathan Sweet */
public class ScrollPane extends WidgetGroup {
	private ScrollPaneStyle style;
	private Actor widget;

	final Rectangle hScrollBounds = new Rectangle();
	final Rectangle vScrollBounds = new Rectangle();
	final Rectangle hKnobBounds = new Rectangle();
	final Rectangle vKnobBounds = new Rectangle();
	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle widgetCullingArea = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();
	private ActorGestureListener gestureListener;

	boolean scrollX, scrollY;
	float amountX, amountY;
	float visualAmountX, visualAmountY;
	float maxX, maxY;
	boolean touchScrollH, touchScrollV;
	final Vector2 lastPoint = new Vector2();
	float areaWidth, areaHeight;
	private boolean fadeScrollBars = true, smoothScrolling = true;
	float fadeAlpha, fadeAlphaSeconds = 1, fadeDelay, fadeDelaySeconds = 1;

	boolean flickScroll = true;
	float velocityX, velocityY;
	float flingTimer;
	private boolean overscrollX = true, overscrollY = true;
	float flingTime = 1f;
	private float overscrollDistance = 50, overscrollSpeedMin = 30, overscrollSpeedMax = 200;
	private boolean forceOverscrollX, forceOverscrollY;
	private boolean disableX, disableY;
	private boolean clamp = true;
	private boolean scrollbarsOnTop;
	int draggingPointer = -1;

	/** @param widget May be null. */
	public ScrollPane (Actor widget) {
		this(widget, new ScrollPaneStyle());
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, Skin skin) {
		this(widget, skin.get(ScrollPaneStyle.class));
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, Skin skin, String styleName) {
		this(widget, skin.get(styleName, ScrollPaneStyle.class));
	}

	/** @param widget May be null. */
	public ScrollPane (Actor widget, ScrollPaneStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.widget = widget;
		this.style = style;
		if (widget != null) {
			setWidget(widget);
		}
		setWidth(150);
		setHeight(150);

		addCaptureListener(new InputListener() {
			private float handlePosition;

			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (draggingPointer != -1) return false;
				if (pointer == 0 && button != 0) return false;
				getStage().setScrollFocus(ScrollPane.this);

				if (!flickScroll) resetFade();

				if (fadeAlpha == 0) return false;

				if (scrollX && hScrollBounds.contains(x, y)) {
					event.stop();
					resetFade();
					if (hKnobBounds.contains(x, y)) {
						lastPoint.set(x, y);
						handlePosition = hKnobBounds.x;
						touchScrollH = true;
						draggingPointer = pointer;
						return true;
					}
					setScrollX(amountX + Math.max(areaWidth * 0.9f, maxX * 0.1f) * (x < hKnobBounds.x ? -1 : 1));
					return true;
				}
				if (scrollY && vScrollBounds.contains(x, y)) {
					event.stop();
					resetFade();
					if (vKnobBounds.contains(x, y)) {
						lastPoint.set(x, y);
						handlePosition = vKnobBounds.y;
						touchScrollV = true;
						draggingPointer = pointer;
						return true;
					}
					setScrollY(amountY + Math.max(areaHeight * 0.9f, maxY * 0.1f) * (y < vKnobBounds.y ? 1 : -1));
					return true;
				}
				return false;
			}

			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != draggingPointer) return;
				draggingPointer = -1;
				touchScrollH = false;
				touchScrollV = false;
			}

			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (pointer != draggingPointer) return;
				if (touchScrollH) {
					float delta = x - lastPoint.x;
					float scrollH = handlePosition + delta;
					handlePosition = scrollH;
					scrollH = Math.max(hScrollBounds.x, scrollH);
					scrollH = Math.min(hScrollBounds.x + hScrollBounds.width - hKnobBounds.width, scrollH);
					setScrollPercentX((scrollH - hScrollBounds.x) / (hScrollBounds.width - hKnobBounds.width));
					lastPoint.set(x, y);
				} else if (touchScrollV) {
					float delta = y - lastPoint.y;
					float scrollV = handlePosition + delta;
					handlePosition = scrollV;
					scrollV = Math.max(vScrollBounds.y, scrollV);
					scrollV = Math.min(vScrollBounds.y + vScrollBounds.height - vKnobBounds.height, scrollV);
					setScrollPercentY(1 - ((scrollV - vScrollBounds.y) / (vScrollBounds.height - vKnobBounds.height)));
					lastPoint.set(x, y);
				}
			}

			public boolean mouseMoved (InputEvent event, float x, float y) {
				if (!flickScroll) resetFade();
				return false;
			}
		});

		gestureListener = new ActorGestureListener() {
			public void pan (InputEvent event, float x, float y, float deltaX, float deltaY) {
				resetFade();
				amountX -= deltaX;
				amountY += deltaY;
				clamp();
				cancelTouchFocusedChild(event);
			}

			public void fling (InputEvent event, float x, float y, int button) {
				if (Math.abs(x) > 150) {
					flingTimer = flingTime;
					velocityX = x;
					cancelTouchFocusedChild(event);
				}
				if (Math.abs(y) > 150) {
					flingTimer = flingTime;
					velocityY = -y;
					cancelTouchFocusedChild(event);
				}
			}

			public boolean handle (Event event) {
				if (super.handle(event)) {
					if (((InputEvent)event).getType() == InputEvent.Type.touchDown) flingTimer = 0;
					return true;
				}
				return false;
			}
		};
		addListener(gestureListener);

		addListener(new InputListener() {
			public boolean scrolled (InputEvent event, float x, float y, int amount) {
				resetFade();
				if (scrollY)
					setScrollY(amountY + Math.max(areaHeight * 0.9f, maxY * 0.1f) / 4 * amount);
				else if (scrollX) //
					setScrollX(amountX + Math.max(areaWidth * 0.9f, maxX * 0.1f) / 4 * amount);
				return true;
			}
		});
	}

	void resetFade () {
		fadeAlpha = fadeAlphaSeconds;
		fadeDelay = fadeDelaySeconds;
	}

	void cancelTouchFocusedChild (InputEvent event) {
		Stage stage = getStage();
		if (stage != null) stage.cancelTouchFocus(gestureListener, this);
	}

	void clamp () {
		if (!clamp) return;
		scrollX(overscrollX ? MathUtils.clamp(amountX, -overscrollDistance, maxX + overscrollDistance) : MathUtils.clamp(amountX,
			0, maxX));
		scrollY(overscrollY ? MathUtils.clamp(amountY, -overscrollDistance, maxY + overscrollDistance) : MathUtils.clamp(amountY,
			0, maxY));
	}

	public void setStyle (ScrollPaneStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/** Returns the scroll pane's style. Modifying the returned style may not have an effect until
	 * {@link #setStyle(ScrollPaneStyle)} is called. */
	public ScrollPaneStyle getStyle () {
		return style;
	}

	public void act (float delta) {
		super.act(delta);

		boolean panning = gestureListener.getGestureDetector().isPanning();

		if (fadeAlpha > 0 && fadeScrollBars && !panning && !touchScrollH && !touchScrollV) {
			fadeDelay -= fadeDelaySeconds * delta;
			if (fadeDelay <= 0) fadeAlpha = Math.max(0, fadeAlpha - fadeAlphaSeconds * delta);
		}

		if (flingTimer > 0) {
			resetFade();

			float alpha = flingTimer / flingTime;
			amountX -= velocityX * alpha * delta;
			amountY -= velocityY * alpha * delta;
			clamp();

			// Stop fling if hit overscroll distance.
			if (amountX == -overscrollDistance) velocityX = 0;
			if (amountX >= maxX + overscrollDistance) velocityX = 0;
			if (amountY == -overscrollDistance) velocityY = 0;
			if (amountY >= maxY + overscrollDistance) velocityY = 0;

			flingTimer -= delta;
			if (flingTimer <= 0) {
				velocityX = 0;
				velocityY = 0;
			}
		}

		if (smoothScrolling && flingTimer <= 0 && !touchScrollH && !touchScrollV && !panning) {
			if (visualAmountX != amountX) {
				if (visualAmountX < amountX)
					visualScrollX(Math.min(amountX, visualAmountX + Math.max(150 * delta, (amountX - visualAmountX) * 5 * delta)));
				else
					visualScrollX(Math.max(amountX, visualAmountX - Math.max(150 * delta, (visualAmountX - amountX) * 5 * delta)));
			}
			if (visualAmountY != amountY) {
				if (visualAmountY < amountY)
					visualScrollY(Math.min(amountY, visualAmountY + Math.max(150 * delta, (amountY - visualAmountY) * 5 * delta)));
				else
					visualScrollY(Math.max(amountY, visualAmountY - Math.max(150 * delta, (visualAmountY - amountY) * 5 * delta)));
			}
		} else {
			if (visualAmountX != amountX) visualScrollX(amountX);
			if (visualAmountY != amountY) visualScrollY(amountY);
		}

		if (!panning) {
			if (overscrollX && scrollX) {
				if (amountX < 0) {
					resetFade();
					amountX += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountX / overscrollDistance)
						* delta;
					if (amountX > 0) scrollX(0);
				} else if (amountX > maxX) {
					resetFade();
					amountX -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -(maxX - amountX)
						/ overscrollDistance)
						* delta;
					if (amountX < maxX) scrollX(maxX);
				}
			}
			if (overscrollY && scrollY) {
				if (amountY < 0) {
					resetFade();
					amountY += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountY / overscrollDistance)
						* delta;
					if (amountY > 0) amountY = 0;
				} else if (amountY > maxY) {
					resetFade();
					amountY -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -(maxY - amountY)
						/ overscrollDistance)
						* delta;
					if (amountY < maxY) amountY = maxY;
				}
			}
		}
	}

	public void layout () {
		final Drawable bg = style.background;
		final Drawable hScrollKnob = style.hScrollKnob;
		final Drawable vScrollKnob = style.vScrollKnob;

		float bgLeftWidth = 0, bgRightWidth = 0, bgTopHeight = 0, bgBottomHeight = 0;
		if (bg != null) {
			bgLeftWidth = bg.getLeftWidth();
			bgRightWidth = bg.getRightWidth();
			bgTopHeight = bg.getTopHeight();
			bgBottomHeight = bg.getBottomHeight();
		}

		float width = getWidth();
		float height = getHeight();

		float scrollbarHeight = 0;
		if (hScrollKnob != null) scrollbarHeight = hScrollKnob.getMinHeight();
		if (style.hScroll != null) scrollbarHeight = Math.max(scrollbarHeight, style.hScroll.getMinHeight());
		float scrollbarWidth = 0;
		if (vScrollKnob != null) scrollbarWidth = vScrollKnob.getMinWidth();
		if (style.vScroll != null) scrollbarWidth = Math.max(scrollbarWidth, style.vScroll.getMinWidth());

		// Get available space size by subtracting background's padded area.
		areaWidth = width - bgLeftWidth - bgRightWidth;
		areaHeight = height - bgTopHeight - bgBottomHeight;

		if (widget == null) return;

		// Get widget's desired width.
		float widgetWidth, widgetHeight;
		if (widget instanceof Layout) {
			Layout layout = (Layout)widget;
			widgetWidth = layout.getPrefWidth();
			widgetHeight = layout.getPrefHeight();
		} else {
			widgetWidth = widget.getWidth();
			widgetHeight = widget.getHeight();
		}

		// Determine if horizontal/vertical scrollbars are needed.
		scrollX = forceOverscrollX || (widgetWidth > areaWidth && !disableX);
		scrollY = forceOverscrollY || (widgetHeight > areaHeight && !disableY);

		boolean fade = fadeScrollBars;
		if (!fade) {
			// Check again, now taking into account the area that's taken up by any enabled scrollbars.
			if (scrollY) {
				areaWidth -= scrollbarWidth;
				if (!scrollX && widgetWidth > areaWidth && !disableX) {
					scrollX = true;
				}
			}
			if (scrollX) {
				areaHeight -= scrollbarHeight;
				if (!scrollY && widgetHeight > areaHeight && !disableY) {
					scrollY = true;
					areaWidth -= scrollbarWidth;
				}
			}
		}

		// Set the widget area bounds.
		widgetAreaBounds.set(bgLeftWidth, bgBottomHeight, areaWidth, areaHeight);

		if (fade) {
			// Make sure widget is drawn under fading scrollbars.
			if (scrollX) areaHeight -= scrollbarHeight;
			if (scrollY) areaWidth -= scrollbarWidth;
		} else {
			if (scrollbarsOnTop) {
				// Make sure widget is drawn under non-fading scrollbars.
				if (scrollX) widgetAreaBounds.height += scrollbarHeight;
				if (scrollY) widgetAreaBounds.width += scrollbarWidth;
			} else {
				// Offset widget area y for horizontal scrollbar.
				if (scrollX) widgetAreaBounds.y += scrollbarHeight;
			}
		}

		// If the widget is smaller than the available space, make it take up the available space.
		widgetWidth = disableX ? width : Math.max(areaWidth, widgetWidth);
		widgetHeight = disableY ? height : Math.max(areaHeight, widgetHeight);

		maxX = widgetWidth - areaWidth;
		maxY = widgetHeight - areaHeight;
		if (fade) {
			// Make sure widget is drawn under fading scrollbars.
			if (scrollX) maxY -= scrollbarHeight;
			if (scrollY) maxX -= scrollbarWidth;
		}
		scrollX(MathUtils.clamp(amountX, 0, maxX));
		amountY = MathUtils.clamp(amountY, 0, maxY);

		// Set the bounds and scroll knob sizes if scrollbars are needed.
		if (scrollX) {
			if (hScrollKnob != null) {
				float hScrollHeight = style.hScroll != null ? style.hScroll.getMinHeight() : hScrollKnob.getMinHeight();
				hScrollBounds.set(bgLeftWidth, bgBottomHeight, areaWidth, hScrollHeight);
				hKnobBounds.width = Math.max(hScrollKnob.getMinWidth(), (int)(hScrollBounds.width * areaWidth / widget.getWidth()));
				hKnobBounds.height = hScrollKnob.getMinHeight();
				hKnobBounds.x = hScrollBounds.x + (int)((hScrollBounds.width - hKnobBounds.width) * getScrollPercentX());
				hKnobBounds.y = hScrollBounds.y;
			} else {
				hScrollBounds.set(0, 0, 0, 0);
				hKnobBounds.set(0, 0, 0, 0);
			}
		}
		if (scrollY) {
			if (vScrollKnob != null) {
				float vScrollWidth = style.vScroll != null ? style.vScroll.getMinWidth() : vScrollKnob.getMinWidth();
				vScrollBounds.set(width - bgRightWidth - vScrollWidth, height - bgTopHeight - areaHeight, vScrollWidth, areaHeight);
				vKnobBounds.width = vScrollKnob.getMinWidth();
				vKnobBounds.height = Math.max(vScrollKnob.getMinHeight(), (int)(vScrollBounds.height * areaHeight / widgetHeight));
				vKnobBounds.x = width - bgRightWidth - vScrollKnob.getMinWidth();
				vKnobBounds.y = vScrollBounds.y + (int)((vScrollBounds.height - vKnobBounds.height) * (1 - getScrollPercentY()));
			} else {
				vScrollBounds.set(0, 0, 0, 0);
				vKnobBounds.set(0, 0, 0, 0);
			}
		}

		if (widget.getWidth() != widgetWidth || widget.getHeight() != widgetHeight) {
			widget.setWidth(widgetWidth);
			widget.setHeight(widgetHeight);
			if (widget instanceof Layout) {
				Layout layout = (Layout)widget;
				layout.invalidate();
				layout.validate();
			}
		} else {
			if (widget instanceof Layout) ((Layout)widget).validate();
		}
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (widget == null) return;

		validate();

		// Setup transform for this group.
		applyTransform(batch, computeTransform());

		if (scrollX) hKnobBounds.x = hScrollBounds.x + (int)((hScrollBounds.width - hKnobBounds.width) * getScrollPercentX());
		if (scrollY)
			vKnobBounds.y = vScrollBounds.y + (int)((vScrollBounds.height - vKnobBounds.height) * (1 - getScrollPercentY()));

		// Calculate the widget's position depending on the scroll state and available widget area.
		float y = widgetAreaBounds.y;
		if (!scrollY)
			y -= (int)maxY;
		else
			y -= (int)(maxY - visualAmountY);

		if (scrollbarsOnTop && scrollX) {
			float scrollbarHeight = 0;
			if (style.hScrollKnob != null) scrollbarHeight = style.hScrollKnob.getMinHeight();
			if (style.hScroll != null) scrollbarHeight = Math.max(scrollbarHeight, style.hScroll.getMinHeight());
			y += scrollbarHeight;
		}

		float x = widgetAreaBounds.x;
		if (scrollX) x -= (int)visualAmountX;
		widget.setPosition(x, y);

		if (widget instanceof Cullable) {
			widgetCullingArea.x = -widget.getX() + widgetAreaBounds.x;
			widgetCullingArea.y = -widget.getY() + widgetAreaBounds.y;
			widgetCullingArea.width = widgetAreaBounds.width;
			widgetCullingArea.height = widgetAreaBounds.height;
			((Cullable)widget).setCullingArea(widgetCullingArea);
		}

		// Caculate the scissor bounds based on the batch transform, the available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), widgetAreaBounds, scissorBounds);

		// Draw the background ninepatch.
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (style.background != null) style.background.draw(batch, 0, 0, getWidth(), getHeight());
		batch.flush();

		// Enable scissors for widget area and draw the widget.
		if (ScissorStack.pushScissors(scissorBounds)) {
			drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		// Render scrollbars and knobs on top.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * Interpolation.fade.apply(fadeAlpha / fadeAlphaSeconds));
		if (scrollX && scrollY) {
			if (style.corner != null) {
				style.corner
					.draw(batch, hScrollBounds.x + hScrollBounds.width, hScrollBounds.y, vScrollBounds.width, vScrollBounds.y);
			}
		}
		if (scrollX) {
			if (style.hScroll != null)
				style.hScroll.draw(batch, hScrollBounds.x, hScrollBounds.y, hScrollBounds.width, hScrollBounds.height);
			if (style.hScrollKnob != null)
				style.hScrollKnob.draw(batch, hKnobBounds.x, hKnobBounds.y, hKnobBounds.width, hKnobBounds.height);
		}
		if (scrollY) {
			if (style.vScroll != null)
				style.vScroll.draw(batch, vScrollBounds.x, vScrollBounds.y, vScrollBounds.width, vScrollBounds.height);
			if (style.vScrollKnob != null)
				style.vScrollKnob.draw(batch, vKnobBounds.x, vKnobBounds.y, vKnobBounds.width, vKnobBounds.height);
		}

		resetTransform(batch);
	}

	public float getPrefWidth () {
		if (widget instanceof Layout) return ((Layout)widget).getPrefWidth();
		return 150;
	}

	public float getPrefHeight () {
		if (widget instanceof Layout) return ((Layout)widget).getPrefHeight();
		return 150;
	}

	public float getMinWidth () {
		return 0;
	}

	public float getMinHeight () {
		return 0;
	}

	/** Sets the {@link Actor} embedded in this scroll pane.
	 * @param widget May be null to remove any current actor. */
	public void setWidget (Actor widget) {
		if (this.widget != null) super.removeActor(this.widget);
		this.widget = widget;
		if (widget != null) super.addActor(widget);
	}

	public void addActor (Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public void addActorAt (int index, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public void addActorBefore (Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public boolean removeActor (Actor actor) {
		if (actor != widget) return false;
		setWidget(null);
		return true;
	}

	public Actor hit (float x, float y, boolean touchable) {
		if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return null;
		if (scrollX && hScrollBounds.contains(x, y)) return this;
		if (scrollY && vScrollBounds.contains(x, y)) return this;
		return super.hit(x, y, touchable);
	}

	/** Called whenever the x scroll amount is changed. */
	protected void scrollX (float pixelsX) {
		this.amountX = pixelsX;
	}

	/** Called whenever the y scroll amount is changed. */
	protected void scrollY (float pixelsY) {
		this.amountY = pixelsY;
	}

	/** Called whenever the visual x scroll amount is changed. */
	protected void visualScrollX (float pixelsX) {
		this.visualAmountX = pixelsX;
	}

	/** Called whenever the visual y scroll amount is changed. */
	protected void visualScrollY (float pixelsY) {
		this.visualAmountY = pixelsY;
	}

	public void setScrollX (float pixels) {
		scrollX(MathUtils.clamp(pixels, 0, maxX));
	}

	/** Returns the x scroll position in pixels. */
	public float getScrollX () {
		return amountX;
	}

	public void setScrollY (float pixels) {
		amountY = MathUtils.clamp(pixels, 0, maxY);
	}

	/** Returns the y scroll position in pixels. */
	public float getScrollY () {
		return amountY;
	}

	public float getVisualScrollX () {
		return !scrollX ? 0 : visualAmountX;
	}

	public float getVisualScrollY () {
		return !scrollY ? 0 : visualAmountY;
	}

	public float getScrollPercentX () {
		return MathUtils.clamp(amountX / maxX, 0, 1);
	}

	public void setScrollPercentX (float percentX) {
		scrollX(maxX * MathUtils.clamp(percentX, 0, 1));
	}

	public float getScrollPercentY () {
		return MathUtils.clamp(amountY / maxY, 0, 1);
	}

	public void setScrollPercentY (float percentY) {
		amountY = maxY * MathUtils.clamp(percentY, 0, 1);
	}

	public void setFlickScroll (boolean flickScroll) {
		if (this.flickScroll == flickScroll) return;
		this.flickScroll = flickScroll;
		if (flickScroll)
			addListener(gestureListener);
		else
			removeListener(gestureListener);
		invalidate();
	}

	/** Sets the scroll offset so the specified rectangle is fully in view, if possible. Coordinates are in the scroll pane widget's
	 * coordinate system. */
	public void scrollTo (float x, float y, float width, float height) {
		float amountX = this.amountX;
		if (x + width > amountX + areaWidth) amountX = x + width - areaWidth;
		if (x < amountX) amountX = x;
		scrollX(MathUtils.clamp(amountX, 0, maxX));

		float amountY = this.amountY;
		if (amountY > maxY - y - height + areaHeight) amountY = maxY - y - height + areaHeight;
		if (amountY < maxY - y) amountY = maxY - y;
		scrollY(MathUtils.clamp(amountY, 0, maxY));
	}

	/** Sets the scroll offset so the specified rectangle is fully in view and centered vertically in the scroll pane, if possible.
	 * Coordinates are in the scroll pane widget's coordinate system. */
	public void scrollToCenter (float x, float y, float width, float height) {
		float amountX = this.amountX;
		if (x + width > amountX + areaWidth) amountX = x + width - areaWidth;
		if (x < amountX) amountX = x;
		scrollX(MathUtils.clamp(amountX, 0, maxX));

		float amountY = this.amountY;
		float centerY = maxY - y + areaHeight / 2 - height / 2;
		if (amountY < centerY - areaHeight / 4 || amountY > centerY + areaHeight / 4) amountY = centerY;
		scrollY(MathUtils.clamp(amountY, 0, maxY));
	}

	/** Returns the maximum scroll value in the x direction. */
	public float getMaxX () {
		return maxX;
	}

	/** Returns the maximum scroll value in the y direction. */
	public float getMaxY () {
		return maxY;
	}

	public float getScrollBarHeight () {
		return style.hScrollKnob == null || !scrollX ? 0 : style.hScrollKnob.getMinHeight();
	}

	public float getScrollBarWidth () {
		return style.vScrollKnob == null || !scrollY ? 0 : style.vScrollKnob.getMinWidth();
	}

	public boolean isScrollX () {
		return scrollX;
	}

	public boolean isScrollY () {
		return scrollY;
	}

	/** Disables scrolling in a direction. The widget will be sized to the FlickScrollPane in the disabled direction. */
	public void setScrollingDisabled (boolean x, boolean y) {
		disableX = x;
		disableY = y;
	}

	public boolean isDragging () {
		return draggingPointer != -1;
	}

	public boolean isPanning () {
		return gestureListener.getGestureDetector().isPanning();
	}

	public boolean isFlinging () {
		return flingTimer > 0;
	}

	public void setVelocityX (float velocityX) {
		this.velocityX = velocityX;
	}

	/** Gets the flick scroll y velocity. */
	public float getVelocityX () {
		if (flingTimer <= 0) return 0;
		float alpha = flingTimer / flingTime;
		alpha = alpha * alpha * alpha;
		return velocityX * alpha * alpha * alpha;
	}

	public void setVelocityY (float velocityY) {
		this.velocityY = velocityY;
	}

	/** Gets the flick scroll y velocity. */
	public float getVelocityY () {
		return velocityY;
	}

	/** For flick scroll, if true the widget can be scrolled slightly past its bounds and will animate back to its bounds when
	 * scrolling is stopped. Default is true. */
	public void setOverscroll (boolean overscrollX, boolean overscrollY) {
		this.overscrollX = overscrollX;
		this.overscrollY = overscrollY;
	}

	/** For flick scroll, sets the overscroll distance in pixels and the speed it returns to the widget's bounds in seconds. Default
	 * is 50, 30, 200. */
	public void setupOverscroll (float distance, float speedMin, float speedMax) {
		overscrollDistance = distance;
		overscrollSpeedMin = speedMin;
		overscrollSpeedMax = speedMax;
	}

	/** For flick scroll, forces the enabling of overscrolling in a direction, even if the contents do not exceed the bounds in that
	 * direction. */
	public void setForceOverscroll (boolean x, boolean y) {
		forceOverscrollX = x;
		forceOverscrollY = y;
	}

	/** For flick scroll, sets the amount of time in seconds that a fling will continue to scroll. Default is 1. */
	public void setFlingTime (float flingTime) {
		this.flingTime = flingTime;
	}

	/** For flick scroll, prevents scrolling out of the widget's bounds. Default is true. */
	public void setClamp (boolean clamp) {
		this.clamp = clamp;
	}

	/** When true the scroll bars fade out after some time of not being used. */
	public void setFadeScrollBars (boolean fadeScrollBars) {
		if (this.fadeScrollBars == fadeScrollBars) return;
		this.fadeScrollBars = fadeScrollBars;
		if (!fadeScrollBars) fadeAlpha = fadeAlphaSeconds;
		invalidate();
	}

	public void setupFadeScrollBars (float fadeAlphaSeconds, float fadeDelaySeconds) {
		this.fadeAlphaSeconds = fadeAlphaSeconds;
		this.fadeDelaySeconds = fadeDelaySeconds;
	}

	public void setSmoothScrolling (boolean smoothScrolling) {
		this.smoothScrolling = smoothScrolling;
	}

	/** When false (the default), the widget is clipped so it is not drawn under the scrollbars. When true, the widget is clipped to
	 * the entire scroll pane bounds and the scrollbars are drawn on top of the widget. If {@link #setFadeScrollBars(boolean)} is
	 * true, the scroll bars are always drawn on top. */
	public void setScrollbarsOnTop (boolean scrollbarsOnTop) {
		this.scrollbarsOnTop = scrollbarsOnTop;
		invalidate();
	}

	/** The style for a scroll pane, see {@link ScrollPane}.
	 * @author mzechner
	 * @author Nathan Sweet */
	static public class ScrollPaneStyle {
		/** Optional. */
		public Drawable background, corner;
		/** Optional. */
		public Drawable hScroll, hScrollKnob;
		/** Optional. */
		public Drawable vScroll, vScrollKnob;

		public ScrollPaneStyle () {
		}

		public ScrollPaneStyle (Drawable background, Drawable hScroll, Drawable hScrollKnob, Drawable vScroll, Drawable vScrollKnob) {
			this.background = background;
			this.hScroll = hScroll;
			this.hScrollKnob = hScrollKnob;
			this.vScroll = vScroll;
			this.vScrollKnob = vScrollKnob;
		}

		public ScrollPaneStyle (ScrollPaneStyle style) {
			this.background = style.background;
			this.hScroll = style.hScroll;
			this.hScrollKnob = style.hScrollKnob;
			this.vScroll = style.vScroll;
			this.vScrollKnob = style.vScrollKnob;
		}
	}
}
