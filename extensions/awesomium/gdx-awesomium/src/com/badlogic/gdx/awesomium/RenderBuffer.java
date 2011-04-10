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
package com.badlogic.gdx.awesomium;

import java.nio.ByteBuffer;

import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class RenderBuffer {
	Pointer renderBuffer;
	
	RenderBuffer(Pointer ptr) {
		this.renderBuffer = ptr;
	}
	
	public RenderBuffer(int width, int height) {
		Awesomium.INSTANCE.awe_RenderBuffer_new(width, height);
	}
	
	public void dispose() {
		Awesomium.INSTANCE.awe_RenderBuffer_delete(renderBuffer);
	}
	
	public void copyTo(ByteBuffer destBuffer, int destRowSpan, int destDepth, boolean convertToRGBA) {
		Awesomium.INSTANCE.awe_RenderBuffer_copyTo(renderBuffer, destBuffer, destRowSpan, destDepth, convertToRGBA?-1:0);
	}
	
	public void saveToPNG(String filePath, boolean preserveTransparency) {
		Awesomium.INSTANCE.awe_RenderBuffer_saveToPNG(renderBuffer, new WString(filePath), preserveTransparency?-1:0);
	}
	
	public void saveToJPEG(String filePath, int quality) {
		Awesomium.INSTANCE.awe_RenderBuffer_saveToJPEG(renderBuffer, new WString(filePath), quality);
	}
	
	public void reserve(int width, int height) {
		Awesomium.INSTANCE.awe_RenderBuffer_reserve(renderBuffer, width, height);
	}
	
	public void copyFrom(ByteBuffer srcBuffer, int srcRowSpan) {
		Awesomium.INSTANCE.awe_RenderBuffer_copyFrom(renderBuffer, srcBuffer, srcRowSpan);
	}
	
	public void copyArea(ByteBuffer srcBuffer, int srcRowSpan, int srcX, int srcY, int srcWidth, int srcHeight, boolean forceOpaque) {
		Awesomium.INSTANCE.awe_RenderBuffer_copyArea(renderBuffer, srcBuffer, srcRowSpan, srcX, srcY, srcWidth, srcHeight, forceOpaque?-1:0);
	}
	
	public void copyArea(ByteBuffer srcBuffer, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight) {
		Awesomium.INSTANCE.awe_RenderBuffer_copyArea2(renderBuffer, srcBuffer, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
	}
	
	public void scrollArea(int dx, int dy, int clipX, int clipY, int clipWidth, int clipHeight) {
		Awesomium.INSTANCE.awe_RenderBuffer_scrollArea(renderBuffer, dx, dy, clipX, clipY, clipWidth, clipHeight);
	}
	
	public ByteBuffer getBuffer() {		
		int height = Awesomium.INSTANCE.awe_RenderBuffer_height(renderBuffer);
		int rowSpan = Awesomium.INSTANCE.awe_RenderBuffer_rowSpan(renderBuffer);
		return Awesomium.INSTANCE.awe_RenderBuffer_buffer(renderBuffer).getByteBuffer(0, height * rowSpan);
	}
	
	public int getWidth() {
		return Awesomium.INSTANCE.awe_RenderBuffer_width(renderBuffer);
	}
	
	public int getHeight() {
		return Awesomium.INSTANCE.awe_RenderBuffer_height(renderBuffer);
	}
	
	public int getRowSpan() {
		return Awesomium.INSTANCE.awe_RenderBuffer_rowSpan(renderBuffer);
	}
	
	public boolean ownsBuffer() {
		return Awesomium.INSTANCE.awe_RenderBuffer_ownsBuffer(renderBuffer)!=0?true:false;
	}
}
