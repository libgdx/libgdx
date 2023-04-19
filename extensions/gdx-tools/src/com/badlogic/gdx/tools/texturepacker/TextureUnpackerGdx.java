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

package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

/**
 * Unpacks a texture atlas into individual image files.
 *
 * @author Geert Konijnendijk
 * @author Nathan Sweet
 * @author Michael Bazos
 * @author Marcin Sciesiński
 */
public class TextureUnpackerGdx {
    private static final int NINEPATCH_PADDING = 1;
    private static final String OUTPUT_TYPE = "png";

    private boolean quiet;

    /**
     * Splits an atlas into seperate image and ninepatch files.
     */
    public void splitAtlas(TextureAtlasData atlas, String outputDir) {
        // create the output directory if it did not exist yet
        FileHandle outputDirFile = Gdx.files.local(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
            if (!quiet) System.out.println(String.format("Creating directory: %s", outputDirFile.path()));
        }

        for (Page page : atlas.getPages()) {
            // load the image file belonging to this page as a Buffered Image
            FileHandle file = page.textureFile;
            if (!file.exists())
                throw new RuntimeException("Unable to find atlas image: " + file.file().getAbsolutePath());
            Pixmap img = new Pixmap(file);
            try {
                for (Region region : atlas.getRegions()) {
                    if (!quiet)
                        System.out.println(String.format("Processing image for %s: x[%s] y[%s] w[%s] h[%s], rotate[%s]",
                                region.name, region.left, region.top, region.width, region.height, region.rotate));

                    // check if the page this region is in is currently loaded in a Buffered Image
                    if (region.page == page) {
                        Pixmap splitImage = null;
                        try {
                            String extension;
                            // check if the region is a ninepatch or a normal image and delegate accordingly
                            if (region.findValue("split") == null) {
                                splitImage = extractImage(img, region, outputDirFile, 0);
                                if (region.width != region.originalWidth || region.height != region.originalHeight) {
                                    Pixmap originalImg = new Pixmap(region.originalWidth, region.originalHeight, img.getFormat());
                                    originalImg.drawPixmap(splitImage, (int) region.offsetX, (int) (region.originalHeight - region.height - region.offsetY));
                                    splitImage.dispose();
                                    splitImage = originalImg;
                                }
                                extension = OUTPUT_TYPE;
                            } else {
                                splitImage = extractNinePatch(img, region, outputDirFile);
                                extension = String.format("9.%s", OUTPUT_TYPE);
                            }

                            // check if the parent directories of this image file exist and create them if not
                            FileHandle imgOutput = outputDirFile.child(
                                    String.format("%s.%s", region.index == -1 ? region.name : region.name + "_" + region.index, extension));
                            FileHandle imgDir = imgOutput.parent();
                            if (!imgDir.exists()) {
                                if (!quiet)
                                    System.out.println(String.format("Creating directory: %s", imgDir.path()));
                                imgDir.mkdirs();
                            }

                            // save the image
                            PixmapIO.writePNG(imgOutput, splitImage);
                        } finally {
                            splitImage.dispose();
                        }
                    }
                }
            } finally {
                img.dispose();
            }
        }
    }

    /**
     * Extract an image from a texture atlas.
     *
     * @param page          The image file related to the page the region is in
     * @param region        The region to extract
     * @param outputDirFile The output directory
     * @param padding       padding (in pixels) to apply to the image
     * @return The extracted image
     */
    private Pixmap extractImage(Pixmap page, Region region, FileHandle outputDirFile, int padding) {
        boolean rotate = region.rotate;
        int resultWidth = (rotate ? region.height : region.width);
        int resultHeight = (rotate ? region.width : region.height);
        Pixmap splitImage = new Pixmap(resultWidth + padding * 2, resultHeight + padding * 2, page.getFormat());
        for (int x = 0; x < resultWidth; x++) {
            for (int y = 0; y < resultHeight; y++) {
                int sourceX;
                int sourceY;
                if (region.rotate) {
                    sourceX = region.top + region.height - x;
                    sourceY = region.left + y;
                } else {
                    sourceX = region.left + x;
                    sourceY = region.top + y;
                }
                splitImage.drawPixel(padding + x, padding + y, page.getPixel(sourceX, sourceY));
            }
        }
        return splitImage;
    }

    /**
     * Extract a ninepatch from a texture atlas, according to the android specification.
     *
     * @param page   The image file related to the page the region is in
     * @param region The region to extract
     * @see <a href="http://developer.android.com/guide/topics/graphics/2d-graphics.html#nine-patch">ninepatch specification</a>
     */
    private Pixmap extractNinePatch(Pixmap page, Region region, FileHandle outputDirFile) {
        Pixmap splitImage = extractImage(page, region, outputDirFile, NINEPATCH_PADDING);
        splitImage.setColor(Color.BLACK);

        // Draw the four lines to save the ninepatch's padding and splits
        int[] splits = region.findValue("split");
        int startX = splits[0] + NINEPATCH_PADDING;
        int endX = region.width - splits[1] + NINEPATCH_PADDING - 1;
        int startY = splits[2] + NINEPATCH_PADDING;
        int endY = region.height - splits[3] + NINEPATCH_PADDING - 1;
        if (endX >= startX) splitImage.drawLine(startX, 0, endX, 0);
        if (endY >= startY) splitImage.drawLine(0, startY, 0, endY);
        int[] pads = region.findValue("pad");
        if (pads != null) {
            int padStartX = pads[0] + NINEPATCH_PADDING;
            int padEndX = region.width - pads[1] + NINEPATCH_PADDING - 1;
            int padStartY = pads[2] + NINEPATCH_PADDING;
            int padEndY = region.height - pads[3] + NINEPATCH_PADDING - 1;
            splitImage.drawLine(padStartX, splitImage.getHeight() - 1, padEndX, splitImage.getHeight() - 1);
            splitImage.drawLine(splitImage.getWidth() - 1, padStartY, splitImage.getWidth() - 1, padEndY);
        }

        return splitImage;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}
