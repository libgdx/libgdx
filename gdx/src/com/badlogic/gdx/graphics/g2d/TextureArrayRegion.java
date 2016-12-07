package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.TextureArray;

/**
 * Defines a rectangular area of a page of a texture array. The coordinate system used has its origin in the upper left corner with the x-axis
 * pointing to the right and the y axis pointing downwards.
 */
public class TextureArrayRegion {
    TextureArray textureArray;
    int textureIndex;

    float u, v;
    float u2, v2;
    int regionWidth, regionHeight;

    public TextureArrayRegion() {
    }

    /**
     * @param width  The width of the texture region. May be negative to flip the sprite when drawn.
     * @param height The height of the texture region. May be negative to flip the sprite when drawn.
     */
    public TextureArrayRegion(final TextureArray textureArray, final int textureIndex, final int x, final int y, final int width, final int height) {
        this.textureArray = textureArray;
        this.textureIndex = textureIndex;
        setRegion(x, y, width, height);
    }

    public void setRegion(final TextureArrayRegion region) {
        textureArray = region.textureArray;
        textureIndex = region.textureIndex;
        setRegionTC(region.u, region.v, region.u2, region.v2);
    }

    /**
     * @param width  The width of the texture region. May be negative to flip the sprite when drawn.
     * @param height The height of the texture region. May be negative to flip the sprite when drawn.
     */
    public void setRegion(final int x, final int y, final int width, final int height) {
        final float invTexWidth = 1f / textureArray.getWidth();
        final float invTexHeight = 1f / textureArray.getHeight();
        setRegionTC(x * invTexWidth, y * invTexHeight, (x + width) * invTexWidth, (y + height) * invTexHeight);
        regionWidth = Math.abs(width);
        regionHeight = Math.abs(height);
    }

    public void setRegionTC(float u, float v, float u2, float v2) {
        final int texWidth = textureArray.getWidth();
        final int texHeight = textureArray.getHeight();
        regionWidth = Math.round(Math.abs(u2 - u) * texWidth);
        regionHeight = Math.round(Math.abs(v2 - v) * texHeight);

        // For a 1x1 region, adjust UVs toward pixel center to avoid filtering artifacts on AMD GPUs when drawing very stretched.
        if (regionWidth == 1 && regionHeight == 1) {
            final float adjustX = 0.25f / texWidth;
            u += adjustX;
            u2 -= adjustX;
            final float adjustY = 0.25f / texHeight;
            v += adjustY;
            v2 -= adjustY;
        }

        this.u = u;
        this.v = v;
        this.u2 = u2;
        this.v2 = v2;
    }

    public TextureArray getTextureArray() {
        return textureArray;
    }

    public float getU() {
        return u;
    }

    public void setU(final float u) {
        this.u = u;
        regionWidth = Math.round(Math.abs(u2 - u) * textureArray.getWidth());
    }

    public float getV() {
        return v;
    }

    public void setV(final float v) {
        this.v = v;
        regionHeight = Math.round(Math.abs(v2 - v) * textureArray.getHeight());
    }

    public float getU2() {
        return u2;
    }

    public void setU2(final float u2) {
        this.u2 = u2;
        regionWidth = Math.round(Math.abs(u2 - u) * textureArray.getWidth());
    }

    public float getV2() {
        return v2;
    }

    public void setV2(final float v2) {
        this.v2 = v2;
        regionHeight = Math.round(Math.abs(v2 - v) * textureArray.getHeight());
    }

    public int getRegionX() {
        return Math.round(u * textureArray.getWidth());
    }

    public void setRegionX(final int x) {
        setU(x / (float) textureArray.getWidth());
    }

    public int getRegionY() {
        return Math.round(v * textureArray.getHeight());
    }

    public void setRegionY(final int y) {
        setV(y / (float) textureArray.getHeight());
    }

    /**
     * Returns the region's width.
     */
    public int getRegionWidth() {
        return regionWidth;
    }

    public void setRegionWidth(final int width) {
        if (isFlipX()) {
            setU(u2 + width / (float) textureArray.getWidth());
        } else {
            setU2(u + width / (float) textureArray.getWidth());
        }
    }

    /**
     * Returns the region's height.
     */
    public int getRegionHeight() {
        return regionHeight;
    }

    public void setRegionHeight(final int height) {
        if (isFlipY()) {
            setV(v2 + height / (float) textureArray.getHeight());
        } else {
            setV2(v + height / (float) textureArray.getHeight());
        }
    }

    public void flip(final boolean x, final boolean y) {
        if (x) {
            final float temp = u;
            u = u2;
            u2 = temp;
        }
        if (y) {
            final float temp = v;
            v = v2;
            v2 = temp;
        }
    }

    public boolean isFlipX() {
        return u > u2;
    }

    public boolean isFlipY() {
        return v > v2;
    }

    /**
     * Offsets the region relative to the current region. Generally the region's size should be the entire size of the texture in
     * the direction(s) it is scrolled.
     *
     * @param xAmount The percentage to offset horizontally.
     * @param yAmount The percentage to offset vertically. This is done in texture space, so up is negative.
     */
    public void scroll(final float xAmount, final float yAmount) {
        if (xAmount != 0) {
            final float width = (u2 - u) * textureArray.getWidth();
            u = (u + xAmount) % 1;
            u2 = u + width / textureArray.getWidth();
        }
        if (yAmount != 0) {
            final float height = (v2 - v) * textureArray.getHeight();
            v = (v + yAmount) % 1;
            v2 = v + height / textureArray.getHeight();
        }
    }

    /**
     * Helper function to create tiles out of this TextureRegion starting from the top left corner going to the right and ending at
     * the bottom right corner. Only complete tiles will be returned so if the region's width or height are not a multiple of the
     * tile width and height not all of the region will be used. This will not work on texture regions returned form a TextureAtlas
     * that either have whitespace removed or where flipped before the region is split.
     *
     * @param tileWidth  a tile's width in pixels
     * @param tileHeight a tile's height in pixels
     * @return a 2D array of TextureRegions indexed by [row][column].
     */
    public TextureArrayRegion[][] split(final int tileWidth, final int tileHeight) {
        int x = getRegionX();
        int y = getRegionY();
        final int width = regionWidth;
        final int height = regionHeight;

        final int rows = height / tileHeight;
        final int cols = width / tileWidth;

        final int startX = x;
        final TextureArrayRegion[][] tiles = new TextureArrayRegion[rows][cols];
        for (int row = 0; row < rows; row++, y += tileHeight) {
            x = startX;
            for (int col = 0; col < cols; col++, x += tileWidth) {
                tiles[row][col] = new TextureArrayRegion(textureArray, textureIndex, x, y, tileWidth, tileHeight);
            }
        }

        return tiles;
    }
}
