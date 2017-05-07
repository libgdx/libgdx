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

package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

/** This viewport can be used to split up the screen into different regions which can be rendered each on their own. It actually
 * consists of several other viewports. It has one "root" viewport which is used to define the area that can be used by the "sub"
 * viewports. The "sub" viewports will split this area into several areas. <br />
 * To render in a certain "sub" viewport, this viewport needs to be activated first. This will result in a layouting of this
 * viewport and its {@link Viewport#update(int, int, boolean)} method being called to setup the camera and the OpenGL viewport
 * (glViewport) correctly.
 * @author Daniel Holderbaum */
public class SplitViewport extends Viewport {

	/** @author Daniel Holderbaum */
	public static class SizeInformation {
		/** Determines, how the size should be interpreted. */
		public SizeType sizeType;

		/** The size to be used. Is ignored in case {@link SizeType} REST is used. */
		public float size;

		public SizeInformation (SizeType sizeType, float size) {
			this.sizeType = sizeType;
			this.size = size;
		}
	}

	/** An enum which determines how a size should be interpreted.
	 * @author Daniel Holderbaum */
	public enum SizeType {
		/** The size will be fixed and will have exactly the given size all the time. */
		ABSOLUTE,

		/** The given size needs to be in [0, 1]. It is relative to the "root" viewport. */
		RELATIVE,

		/** If this type is chosen, the given size will be ignored. Instead all cells with this type will share the rest amount of
		 * the "root" viewport that is still left after all other parts have been subtracted. */
		REST
	}

	/** A sub view for one cell of the {@link SplitViewport}.
	 * @author Daniel Holderbaum */
	public static class SubView {
		/** The size information for this sub view. */
		public SizeInformation sizeInformation;

		/** The {@link Viewport} for this sub view. */
		public Viewport viewport;

		public SubView (SizeInformation sizeInformation, Viewport viewport) {
			this.sizeInformation = sizeInformation;
			this.viewport = viewport;
		}

	}

	private Viewport rootViewport;
	private Viewport activeViewport;

	private Array<SubView> rowSizeInformations = new Array<SubView>();
	private Array<Array<SubView>> subViews = new Array<Array<SubView>>();

	/** Initializes the split viewport.
	 * @param rootViewport The viewport to be used to determine the area which the sub viewports can use */
	public SplitViewport (Viewport rootViewport) {
		this.rootViewport = rootViewport;
	}

	/** Adds another row to the split viewport. This has to be called at least once prior to {@link #add(SubView)}.
	 * @param sizeInformation The size information for the row. */
	public void row (SizeInformation sizeInformation) {
		if (sizeInformation.sizeType == SizeType.RELATIVE) {
			validateRelativeSize(sizeInformation.size);
		}

		// for rows we don't need a SubView with a viewport, but to not duplicate some calculation methods, we just create a new
		// SubView
		rowSizeInformations.add(new SubView(sizeInformation, null));
		subViews.add(new Array<SubView>());
	}

	/** Adds another sub view to the last added row.
	 * @param subView The {@link SubView} with size and viewport. It can be changed externally. Those changes will be used as soon
	 *           as the viewport is activated next time. */
	public void add (SubView subView) {
		if (subViews.size == 0) {
			throw new IllegalStateException("A row has to be added first.");
		}
		if (subView.sizeInformation.sizeType == SizeType.RELATIVE) {
			validateRelativeSize(subView.sizeInformation.size);
		}

		Array<SubView> rowViewports = subViews.peek();
		rowViewports.add(subView);
	}

	private final Rectangle subViewportArea = new Rectangle();

	/** Updates the viewport at (row, column) and sets it as the currently active one. The top left sub viewport is (0, 0).
	 * @param row The index of the row with the viewport to be activated. Starts at 0.
	 * @param column The index of the column with the viewport to be activated. Starts at 0.
	 * @param centerCamera Whether the subView should center the camera or not. */
	public void activateSubViewport (int row, int column, boolean centerCamera) {
		validateCoordinates(row, column);

		Array<SubView> rowMap = subViews.get(row);
		Viewport viewport = rowMap.get(column).viewport;

		// update the viewport simulating a smaller sub view
		calculateSubViewportArea(row, column, subViewportArea);
		viewport.update((int)subViewportArea.width, (int)subViewportArea.height, centerCamera);

		// store the current world size so we can restore it in case it gets changed now
		float originalWorldWidth = viewport.worldWidth;
		float originalWorldHeight = viewport.worldHeight;

		// some scaling strategies will scale the viewport bigger than the allowed sub view, so we need to limit it
		if (viewport.viewportWidth > subViewportArea.width) {
			float offcutWidth = viewport.viewportWidth - subViewportArea.width;
			viewport.viewportWidth = (int)subViewportArea.width;
			viewport.worldWidth -= offcutWidth;
			viewport.viewportX += offcutWidth / 2;
		}
		if (viewport.viewportHeight > subViewportArea.height) {
			float offcutHeight = viewport.viewportHeight - subViewportArea.height;
			viewport.viewportHeight = (int)subViewportArea.height;
			viewport.worldHeight -= offcutHeight;
			viewport.viewportY += offcutHeight / 2;
		}

		// now shift it to the correct place
		viewport.viewportX += subViewportArea.x;
		viewport.viewportY += subViewportArea.y;

		// we changed the viewport parameters, now we need to update once more to correct the glViewport
		viewport.update();

		// restore the original world width after the glViewport has been set
		viewport.worldWidth = originalWorldWidth;
		viewport.worldHeight = originalWorldHeight;

		activeViewport = viewport;
	}

	public Viewport getRootViewport () {
		return rootViewport;
	}

	public void setRootViewport (Viewport rootViewport) {
		this.rootViewport = rootViewport;
	}

	// ############################################################
	// The following methods all just delegate to the root viewport
	// ############################################################

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		rootViewport.update(screenWidth, screenHeight, centerCamera);
	}

	@Override
	public Vector2 unproject (Vector2 screenCoords) {
		return rootViewport.unproject(screenCoords);
	}

	@Override
	public Vector2 project (Vector2 worldCoords) {
		return rootViewport.project(worldCoords);
	}

	@Override
	public Vector3 unproject (Vector3 screenCoords) {
		return rootViewport.unproject(screenCoords);
	}

	@Override
	public Vector3 project (Vector3 worldCoords) {
		return rootViewport.project(worldCoords);
	}

	@Override
	public Ray getPickRay (float screenX, float screenY) {
		return rootViewport.getPickRay(screenX, screenY);
	}

	@Override
	public void calculateScissors (Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
		rootViewport.calculateScissors(batchTransform, area, scissor);
	}

	@Override
	public Vector2 toScreenCoordinates (Vector2 worldCoords, Matrix4 transformMatrix) {
		return rootViewport.toScreenCoordinates(worldCoords, transformMatrix);
	}

	@Override
	public Camera getCamera () {
		return rootViewport.getCamera();
	}

	@Override
	public void setCamera (Camera camera) {
		rootViewport.setCamera(camera);
	}

	@Override
	public void setWorldSize (float worldWidth, float worldHeight) {
		rootViewport.setWorldSize(worldWidth, worldHeight);
	}

	@Override
	public float getWorldWidth () {
		return rootViewport.getWorldWidth();
	}

	@Override
	public void setWorldWidth (float worldWidth) {
		rootViewport.setWorldWidth(worldWidth);
	}

	@Override
	public float getWorldHeight () {
		return rootViewport.getWorldHeight();
	}

	@Override
	public void setWorldHeight (float worldHeight) {
		rootViewport.setWorldHeight(worldHeight);
	}

	@Override
	public int getViewportX () {
		return rootViewport.getViewportX();
	}

	@Override
	public int getViewportY () {
		return rootViewport.getViewportY();
	}

	@Override
	public int getViewportWidth () {
		return rootViewport.getViewportWidth();
	}

	@Override
	public int getViewportHeight () {
		return rootViewport.getViewportHeight();
	}

	@Override
	public int getLeftGutterWidth () {
		return rootViewport.getLeftGutterWidth();
	}

	@Override
	public int getRightGutterX () {
		return rootViewport.getRightGutterX();
	}

	@Override
	public int getRightGutterWidth () {
		return rootViewport.getRightGutterWidth();
	}

	@Override
	public int getBottomGutterHeight () {
		return rootViewport.getBottomGutterHeight();
	}

	@Override
	public int getTopGutterY () {
		return rootViewport.getTopGutterY();
	}

	@Override
	public int getTopGutterHeight () {
		return rootViewport.getTopGutterHeight();
	}

	// #################################################################
	// Private utility methods to help with calculations and validations
	// #################################################################

	private Rectangle calculateSubViewportArea (int row, int col, Rectangle subViewportArea) {
		subViewportArea.x = calculateWidthOffset(subViews.get(row), col);
		subViewportArea.y = calculateHeightOffset(rowSizeInformations, row);
		subViewportArea.width = calculateSize(subViews.get(row), col, getViewportWidth());
		subViewportArea.height = calculateSize(rowSizeInformations, row, getViewportHeight());

		return subViewportArea;
	}

	private float calculateHeightOffset (Array<SubView> subViews, int index) {
		// the glViewport offset is y-up, but the first row is the top most one
		// that's why we start at the top and subtract the row heights
		float heightOffset = getViewportHeight();
		for (int i = 0; i <= index; i++) {
			heightOffset -= calculateSize(subViews, i, getViewportHeight());
		}

		// add the root offset
		heightOffset += getViewportY();

		return heightOffset;
	}

	private float calculateWidthOffset (Array<SubView> sizeInformations, int index) {
		float widthOffset = 0;
		for (int i = 0; i < index; i++) {
			widthOffset += calculateSize(sizeInformations, i, getViewportWidth());
		}

		// add the root offset
		widthOffset += getViewportX();

		return widthOffset;
	}

	/** Used to calculate either the width or height.
	 * @param subViews The row informations or column informations of a certain row.
	 * @param index The index of the element to be calculated.
	 * @param totalSize The total size, either the viewport width or height. */
	private float calculateSize (Array<SubView> subViews, int index, float totalSize) {
		SubView subView = subViews.get(index);
		switch (subView.sizeInformation.sizeType) {
		case ABSOLUTE:
			return subView.sizeInformation.size;
		case RELATIVE:
			return subView.sizeInformation.size * totalSize;
		case REST:
			int rests = countRest(subViews);
			float usedSize = calculateUsedSize(subViews, totalSize);
			return (totalSize - usedSize) / rests;
		default:
			throw new IllegalArgumentException(subView.sizeInformation.sizeType + " could not be handled.");
		}
	}

	private float calculateUsedSize (Array<SubView> subViews, float totalSize) {
		float usedSize = 0;

		for (SubView subView : subViews) {
			switch (subView.sizeInformation.sizeType) {
			case ABSOLUTE:
				usedSize += subView.sizeInformation.size;
				break;
			case RELATIVE:
				usedSize += subView.sizeInformation.size * totalSize;
				break;
			}
		}

		return usedSize;
	}

	private int countRest (Array<SubView> subViews) {
		int rests = 0;

		for (SubView subView : subViews) {
			if (subView.sizeInformation.sizeType == SizeType.REST) {
				rests++;
			}
		}

		return rests;
	}

	private void validateCoordinates (int row, int col) {
		if (row >= subViews.size) {
			throw new IllegalArgumentException("There is no row with ID " + row);
		}

		Array<SubView> rowSubViews = subViews.get(row);
		if (col >= rowSubViews.size) {
			throw new IllegalArgumentException("There is no column with ID " + col);
		}
	}

	private void validateRelativeSize (float size) {
		if (size < 0 || size > 1) {
			throw new IllegalArgumentException(size + " does not fulfill the constraint of 0 <= size <= 1.");
		}
	}
}
