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

package com.badlogic.gdx.utils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public class ScreenCapturer {
	 private static int screenshotNumber = 1;
	 private static boolean grayScale = false;
	 
	 
	 /**
	  * Saves a screenshot of a portion of a screen in the .png format 
	  * @param x Starting xCoord
	  * @param y Starting yCoord
	  * @param width Width of the square zoom
	  * @param height Height of the square zoom
	  */
	 public static void saveScreenshotZoom(int x, int y, int width, int height){
		 
		 /* Creates a new file with the next index number in function of the existing files of the same name('screenshot[number]') */
       try{
           FileHandle fh;
           do{
               fh = new FileHandle("Screenshots/screenshot" + screenshotNumber++ + ".png");
           }while (fh.exists());
           
           /* Throw exceptions for when the zoom sqaure exceeds screen dimensions */
           if(x < 0){
         	  throw new Exception("Screenshot.class: the x coordinate given is negative");
       	  }else if(y < 0){
       		 throw new Exception("Screenshot.class: the y coordinate given is negative");
       	  }else if(width <= 0){
       		 throw new Exception("Screenshot.class: the width given is negative.");
       	  }else if(height <= 0){
       		 throw new Exception("Screenshot.class: the height given is negative.");
           }else if( (x + width) > Gdx.graphics.getWidth()){
         	  throw new Exception("Screenshot.class: the size of the zoom square exceeds screen width.");
           }else if( (y + height) > Gdx.graphics.getHeight()){
         	  throw new Exception("Screenshot.class: the size of the zoom square exceeds screen height.");
           }
           
           Pixmap pixmap = getScreenshot(x, y, width, height, true);
           PixmapIO.writePNG(fh, pixmap);
           pixmap.dispose();
       }catch (Exception e){
      	 System.err.println(e.getMessage());
       }
	 }
	 
	 /**
	  * Saves a screenshot of the whole screen in the .png format 
	  */
    public static void saveScreenshot(){
   	 
   	 /* Creates a new file with the next index number in function of the existing files of the same name('screenshot[number]') */
        try{
            FileHandle fh;
            do{
                fh = new FileHandle("Screenshots/screenshot" + screenshotNumber++ + ".png");
            }while (fh.exists());
            Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            PixmapIO.writePNG(fh, pixmap);
            pixmap.dispose();
        }catch (Exception e){
      	  System.err.println(e.getMessage());
        }
    }
    
    /**
     * Gets a pixmap from the frame buffer.
     * @param x XCoord
     * @param y YCoord
     * @param w Width
     * @param h Heigth 
     * @param yDown True if we want the screenshot flipped vertically, false if we don't
     * @return The resulting pixmap 
     */
    private static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown){
        final Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(x, y, w, h);
        
        manipulate(pixmap);
        
        if (yDown) {
            // Flip the pixmap upside down
      	   ByteBuffer pixels = pixmap.getPixels();
            int numBytes = w * h * 4;
            byte[] lines = new byte[numBytes];
            int numBytesPerLine = w * 4;
            for (int i = 0; i < h; i++) {
                pixels.position((h - i - 1) * numBytesPerLine);
                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
            }
            pixels.clear();
            pixels.put(lines);
            pixels.clear();
        }

        return pixmap;
    }
    
    /**
     * Manipulates a pixmap with the conversions specified in this class
     * @param pm The pixmap to be manipulated
     */
    private static void manipulate(Pixmap pm){
   		 Color temp = new Color();
          float avg;
          for (int i = 0; i < pm.getWidth(); i++) {
   			for (int j = 0; j < pm.getHeight(); j++) {
   		      Color.rgba8888ToColor(temp, pm.getPixel(i, j));
   		      if(grayScale){
   		      	avg = (temp.r + temp.g + temp.b)/3;
	   		      temp.r = avg;
	   		      temp.g = avg;
	   		      temp.b = avg;
   		      }
   		      pm.setColor(temp);
   		      pm.drawPixel(i, j);
   			}
          }
    }
    
    
    /**
     * Sets the gray scale flag to true or false 
     */
    public static void setGrayScale(){
   	 grayScale = true;
    }
    
    /**
     * Checks if the capturer is set to take grayscaled screenshots
     * @return the value of the grayScale member
     */
    public static boolean checkGrayScale(){
   	 return grayScale;
    }
    
    /**
     * Disables grayscale mode
     */
    public static void disableGrayScale(){
   	 grayScale = false;
    }

}
