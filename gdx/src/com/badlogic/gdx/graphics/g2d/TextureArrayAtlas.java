package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

@SuppressWarnings("ClassWithTooManyConstructors")
public class TextureArrayAtlas implements Disposable {
    static final String[] tuple = new String[4];

    private TextureArray textureArray;
    private final Array<ArrayAtlasRegion> regions = new Array<>();

    public static class TextureArrayAtlasData {
        public static class Page {
            public final FileHandle textureFile;
            public int textureIndex;
            public final float width;
            public final float height;
            public final boolean useMipMaps;
            public final Pixmap.Format format;
            public final Texture.TextureFilter minFilter;
            public final Texture.TextureFilter magFilter;
            public final Texture.TextureWrap uWrap;
            public final Texture.TextureWrap vWrap;

            public Page(final FileHandle handle, final float width, final float height, final boolean useMipMaps, final Pixmap.Format format, final Texture.TextureFilter minFilter,
                        final Texture.TextureFilter magFilter, final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
                this.width = width;
                this.height = height;
                this.textureFile = handle;
                this.useMipMaps = useMipMaps;
                this.format = format;
                this.minFilter = minFilter;
                this.magFilter = magFilter;
                this.uWrap = uWrap;
                this.vWrap = vWrap;
            }
        }

        public static class Region {
            public TextureArrayAtlasData.Page page;
            public int index;
            public String name;
            public float offsetX;
            public float offsetY;
            public int originalWidth;
            public int originalHeight;
            public boolean rotate;
            public int left;
            public int top;
            public int width;
            public int height;
            public boolean flip;
            public int[] splits;
            public int[] pads;
        }

        final Array<TextureArrayAtlas.TextureArrayAtlasData.Page> pages = new Array<>();
        final Array<TextureArrayAtlas.TextureArrayAtlasData.Region> regions = new Array<>();

        public TextureArrayAtlasData(final FileHandle packFile, final FileHandle imagesDir, final boolean flip) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read(), StandardCharsets.UTF_8), 64);
            try {
                TextureArrayAtlas.TextureArrayAtlasData.Page pageImage = null;
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) break;
                    if (line.trim().length() == 0)
                        pageImage = null;
                    else if (pageImage == null) {
                        final FileHandle file = imagesDir.child(line);

                        float width = 0;
                        float height = 0;
                        if (readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
                            width = Integer.parseInt(tuple[0]);
                            height = Integer.parseInt(tuple[1]);
                            readTuple(reader);
                        }
                        final Pixmap.Format format = Pixmap.Format.valueOf(tuple[0]);

                        readTuple(reader);
                        final Texture.TextureFilter min = Texture.TextureFilter.valueOf(tuple[0]);
                        final Texture.TextureFilter max = Texture.TextureFilter.valueOf(tuple[1]);

                        final String direction = readValue(reader);
                        Texture.TextureWrap repeatX = ClampToEdge;
                        Texture.TextureWrap repeatY = ClampToEdge;
                        if ("x".equals(direction))
                            repeatX = Repeat;
                        else if ("y".equals(direction))
                            repeatY = Repeat;
                        else if ("xy".equals(direction)) {
                            repeatX = Repeat;
                            repeatY = Repeat;
                        }

                        pageImage = new TextureArrayAtlas.TextureArrayAtlasData.Page(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
                        pages.add(pageImage);
                    } else {
                        final boolean rotate = Boolean.valueOf(readValue(reader));

                        readTuple(reader);
                        final int left = Integer.parseInt(tuple[0]);
                        final int top = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        final int width = Integer.parseInt(tuple[0]);
                        final int height = Integer.parseInt(tuple[1]);

                        final TextureArrayAtlas.TextureArrayAtlasData.Region region = new TextureArrayAtlas.TextureArrayAtlasData.Region();
                        region.page = pageImage;
                        region.left = left;
                        region.top = top;
                        region.width = width;
                        region.height = height;
                        region.name = line;
                        region.rotate = rotate;

                        if (readTuple(reader) == 4) { // split is optional
                            region.splits = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                    Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                            if (readTuple(reader) == 4) { // pad is optional, but only present with splits
                                region.pads = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                        Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                                readTuple(reader);
                            }
                        }

                        region.originalWidth = Integer.parseInt(tuple[0]);
                        region.originalHeight = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        region.offsetX = Integer.parseInt(tuple[0]);
                        region.offsetY = Integer.parseInt(tuple[1]);

                        region.index = Integer.parseInt(readValue(reader));

                        if (flip) region.flip = true;

                        regions.add(region);
                    }
                }
            } catch (final Exception ex) {
                throw new GdxRuntimeException("Error reading pack file: " + packFile, ex);
            } finally {
                StreamUtils.closeQuietly(reader);
            }

            regions.sort(indexComparator);
        }

        public Array<TextureArrayAtlas.TextureArrayAtlasData.Page> getPages() {
            return pages;
        }

        public Array<TextureArrayAtlas.TextureArrayAtlasData.Region> getRegions() {
            return regions;
        }
    }

    /**
     * Creates an empty atlas to which regions can be added.
     */
    public TextureArrayAtlas() {
    }

    /**
     * Loads the specified pack file using {@link Files.FileType#Internal}, using the parent directory of the pack file to find the page
     * images.
     */
    public TextureArrayAtlas(final String internalPackFile) {
        this(Gdx.files.internal(internalPackFile));
    }

    /**
     * Loads the specified pack file, using the parent directory of the pack file to find the page images.
     */
    public TextureArrayAtlas(final FileHandle packFile) {
        this(packFile, packFile.parent());
    }

    /**
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     */
    public TextureArrayAtlas(final FileHandle packFile, final boolean flip) {
        this(packFile, packFile.parent(), flip);
    }

    public TextureArrayAtlas(final FileHandle packFile, final FileHandle imagesDir) {
        this(packFile, imagesDir, false);
    }

    /**
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     */
    public TextureArrayAtlas(final FileHandle packFile, final FileHandle imagesDir, final boolean flip) {
        this(new TextureArrayAtlas.TextureArrayAtlasData(packFile, imagesDir, flip));
    }

    /**
     * @param data May be null.
     */
    public TextureArrayAtlas(final TextureArrayAtlas.TextureArrayAtlasData data) {
        if (data != null) load(data);
    }

    private void load(final TextureArrayAtlasData data) {
        final FileHandle[] textureFileHandles = new FileHandle[data.pages.size];
        int i = 0;
        boolean createMipMaps = false;

        for (final TextureArrayAtlasData.Page page : data.pages) {
            page.textureIndex = i;
            textureFileHandles[i++] = page.textureFile;
            // TODO: uncomment when FileTextureArrayData starts supporting mipmap generation
//            createMipMaps |= page.useMipMaps;
        }
        final TextureArrayAtlasData.Page firstPage = data.pages.get(0);

        textureArray = new TextureArray(createMipMaps, textureFileHandles);
        textureArray.setWrap(firstPage.uWrap, firstPage.vWrap);
        textureArray.setFilter(Linear, Linear); // TODO: replace when FileTextureArrayData starts supporting mipmap generation
//        textureArray.setFilter(firstPage.minFilter, firstPage.magFilter);

        for (final TextureArrayAtlasData.Region region : data.regions) {
            final int width = region.width;
            final int height = region.height;
            final ArrayAtlasRegion atlasRegion = new ArrayAtlasRegion(textureArray, region.page.textureIndex,
                    region.left, region.top,
                    region.rotate ? height : width, region.rotate ? width : height);
            atlasRegion.index = region.index;
            atlasRegion.name = region.name;
            atlasRegion.offsetX = region.offsetX;
            atlasRegion.offsetY = region.offsetY;
            atlasRegion.originalHeight = region.originalHeight;
            atlasRegion.originalWidth = region.originalWidth;
            atlasRegion.rotate = region.rotate;
            atlasRegion.splits = region.splits;
            atlasRegion.pads = region.pads;
            if (region.flip) atlasRegion.flip(false, true);
            regions.add(atlasRegion);
        }
    }

    /**
     * Returns all regions in the atlas.
     */
    public Array<TextureArrayAtlas.ArrayAtlasRegion> getRegions() {
        return regions;
    }

    /**
     * Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
     * should be cached rather than calling this method multiple times.
     *
     * @return The region, or null.
     */
    public TextureArrayAtlas.ArrayAtlasRegion findRegion(final String name) {
        for (int i = 0, n = regions.size; i < n; i++)
            if (regions.get(i).name.equals(name)) return regions.get(i);
        return null;
    }

    /**
     * Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
     * the result should be cached rather than calling this method multiple times.
     *
     * @return The region, or null.
     */
    public TextureArrayAtlas.ArrayAtlasRegion findRegion(final String name, final int index) {
        for (int i = 0, n = regions.size; i < n; i++) {
            final TextureArrayAtlas.ArrayAtlasRegion region = regions.get(i);
            if (!region.name.equals(name)) continue;
            if (region.index != index) continue;
            return region;
        }
        return null;
    }

    /**
     * Returns all regions with the specified name, ordered by smallest to largest {@link TextureAtlas.AtlasRegion#index index}. This method
     * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times.
     */
    public Array<TextureArrayAtlas.ArrayAtlasRegion> findRegions(final String name) {
        final Array<TextureArrayAtlas.ArrayAtlasRegion> matched = new Array<>();
        for (int i = 0, n = regions.size; i < n; i++) {
            final TextureArrayAtlas.ArrayAtlasRegion region = regions.get(i);
            if (region.name.equals(name)) matched.add(new TextureArrayAtlas.ArrayAtlasRegion(region));
        }
        return matched;
    }

    @Override
    public void dispose() {
        final TextureArray t = this.textureArray;
        if (t != null) t.dispose();
    }

    static final Comparator<TextureArrayAtlas.TextureArrayAtlasData.Region> indexComparator = new Comparator<TextureArrayAtlas.TextureArrayAtlasData.Region>() {
        @Override
        public int compare(final TextureArrayAtlas.TextureArrayAtlasData.Region region1, final TextureArrayAtlas.TextureArrayAtlasData.Region region2) {
            int i1 = region1.index;
            if (i1 == -1) i1 = Integer.MAX_VALUE;
            int i2 = region2.index;
            if (i2 == -1) i2 = Integer.MAX_VALUE;
            return Integer.compare(i1, i2);
        }
    };

    static String readValue(final BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        final int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    /**
     * Returns the number of tuple values read (1, 2 or 4).
     */
    static int readTuple(final BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        final int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        int i = 0;
        int lastMatch = colon + 1;
        for (i = 0; i < 3; i++) {
            final int comma = line.indexOf(',', lastMatch);
            if (comma == -1) break;
            tuple[i] = line.substring(lastMatch, comma).trim();
            lastMatch = comma + 1;
        }
        tuple[i] = line.substring(lastMatch).trim();
        return i + 1;
    }

    /**
     * Describes the region of a packed image and provides information about the original image before it was packed.
     */
    @SuppressWarnings("FieldNotUsedInToString")
    public static class ArrayAtlasRegion extends TextureArrayRegion {
        /**
         * The number at the end of the original image file name, or -1 if none.<br>
         * <br>
         * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
         * part of the sprite's name. This is useful for keeping animation frames in order.
         *
         * @see TextureAtlas#findRegions(String)
         */
        public int index;

        /**
         * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
         * packer.
         */
        public String name;

        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.
         */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
         * packing.
         */
        public float offsetY;

        /**
         * The width of the image, after whitespace was removed for packing.
         */
        public int packedWidth;

        /**
         * The height of the image, after whitespace was removed for packing.
         */
        public int packedHeight;

        /**
         * The width of the image, before whitespace was removed and rotation was applied for packing.
         */
        public int originalWidth;

        /**
         * The height of the image, before whitespace was removed for packing.
         */
        public int originalHeight;

        /**
         * If true, the region has been rotated 90 degrees counter clockwise.
         */
        public boolean rotate;

        /**
         * The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom.
         */
        public int[] splits;

        /**
         * The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom.
         */
        public int[] pads;

        public ArrayAtlasRegion(final TextureArray textureArray, final int textureIndex, final int x, final int y, final int width, final int height) {
            super(textureArray, textureIndex, x, y, width, height);
            originalWidth = width;
            originalHeight = height;
            packedWidth = width;
            packedHeight = height;
        }

        public ArrayAtlasRegion(final TextureArrayAtlas.ArrayAtlasRegion region) {
            setRegion(region);
            index = region.index;
            name = region.name;
            offsetX = region.offsetX;
            offsetY = region.offsetY;
            packedWidth = region.packedWidth;
            packedHeight = region.packedHeight;
            originalWidth = region.originalWidth;
            originalHeight = region.originalHeight;
            rotate = region.rotate;
            splits = region.splits;
        }

        /**
         * Flips the region, adjusting the offset so the image appears to be flip as if no whitespace has been removed for packing.
         */
        @Override
        public void flip(final boolean x, final boolean y) {
            super.flip(x, y);
            if (x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
            if (y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
        }

        /**
         * Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
         * returns the packedWidth.
         */
        public float getRotatedPackedWidth() {
            return rotate ? packedHeight : packedWidth;
        }

        /**
         * Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
         * returns the packedHeight.
         */
        public float getRotatedPackedHeight() {
            return rotate ? packedWidth : packedHeight;
        }

        public String toString() {
            return name;
        }
    }
}
