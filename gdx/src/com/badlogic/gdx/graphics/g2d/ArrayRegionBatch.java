package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.NumberUtils;

@SuppressWarnings("UnnecessaryParentheses")
public class ArrayRegionBatch implements Disposable {

    private Mesh mesh;

    final float[] vertices;
    int idx = 0;
    TextureArray lastTextureArray = null;
    float invTexWidth = 0, invTexHeight = 0;

    boolean drawing = false;

    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();

    private boolean blendingDisabled = false;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;

    private final ShaderProgram shader;
    private ShaderProgram customShader = null;
    private boolean ownsShader;

    float color = Color.WHITE.toFloatBits();
    private Color tempColor = new Color(1, 1, 1, 1);

    /**
     * Number of render calls since the last {@link #begin()}.
     **/
    public int renderCalls = 0;

    /**
     * Number of rendering calls, ever. Will not be reset unless set manually.
     **/
    public int totalRenderCalls = 0;

    /**
     * The maximum number of sprites rendered in one batch so far.
     **/
    public int maxSpritesInBatch = 0;

    static {
        if (Gdx.gl30 == null) throw new RuntimeException("GL 3+ is required for ArrayRegionBatch");
    }

    public static final int vertexSize = 6;
    public static final int indicesPerSprite = 6;
    public static final int verticesPerSprite = 4;

    /**
     * Constructs a new SpriteBatch with a size of 1000, one buffer, and the default shader.
     *
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public ArrayRegionBatch() {
        this(1000, null);
    }

    /**
     * Constructs a SpriteBatch with one buffer and the default shader.
     *
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public ArrayRegionBatch(final int size) {
        this(size, null);
    }

    /**
     * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
     * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
     * respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
     *
     * @param size          The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public ArrayRegionBatch(final int size, final ShaderProgram defaultShader) {
        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        final Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;

        mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
                new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_textureIndex")
        );

        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        vertices = new float[size * 4 * vertexSize];

        final int len = size * vertexSize;
        final short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }
        mesh.setIndices(indices);

        if (defaultShader == null) {
            shader = createDefaultShader();
            ownsShader = true;
        } else
            shader = defaultShader;
    }

    /**
     * Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified.
     */
    static public ShaderProgram createDefaultShader() {
        final String vertexShader = ""
                + "#version 140\n"
                + "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
                + "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
                + "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
                + "in float a_textureIndex;\n"

                + "uniform mat4 u_projTrans;\n"

                + "out vec4 v_color;\n"
                + "out vec2 v_texCoords;\n"
                + "out float v_textureIndex;\n"

                + "void main() {\n"
                + "   v_textureIndex = a_textureIndex;"
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
                + "   v_color.a = v_color.a * (255.0/254.0);\n"
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
                + "   gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
                + "}\n";

        final String fragmentShader = ""
                + "#version 140\n"

                + "#ifdef GL_ES\n"
                + "#define LOWP lowp\n"
                + "precision mediump float;\n"
                + "#else\n"
                + "#define LOWP \n"
                + "#endif\n"

                + "in LOWP vec4 v_color;\n"
                + "in vec2 v_texCoords;\n"
                + "in float v_textureIndex;\n"

                + "uniform sampler2DArray u_textureArray;\n"

                + "out vec4 fragColor;"

                + "void main() {\n"
                + "  fragColor = v_color * texture(u_textureArray, vec3(v_texCoords, v_textureIndex));\n"
                + "}";

        final ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled())
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }

    public void begin() {
        if (drawing) throw new IllegalStateException("end must be called before begin.");
        renderCalls = 0;

        Gdx.gl.glDepthMask(false);

        (customShader != null ? customShader : shader).begin();
        setupUniforms();

        drawing = true;
    }

    public void end() {
        checkBeginCalled();
        if (idx > 0) flush();
        lastTextureArray = null;
        drawing = false;

        final GL20 gl = Gdx.gl;
        gl.glDepthMask(true);
        if (isBlendingEnabled()) gl.glDisable(GL20.GL_BLEND);

        (customShader != null ? customShader : shader).end();
    }

    public void setColor(final Color tint) {
        color = tint.toFloatBits();
    }

    public void setColor(final float r, final float g, final float b, final float a) {
        final int intBits = (int) (255 * a) << 24 | (int) (255 * b) << 16 | (int) (255 * g) << 8 | (int) (255 * r);
        color = NumberUtils.intToFloatColor(intBits);
    }

    public void setColor(final float color) {
        this.color = color;
    }

    public Color getColor() {
        final int intBits = NumberUtils.floatToIntColor(color);
        final Color color = tempColor;
        color.r = (intBits & 0xff) / 255f;
        color.g = ((intBits >>> 8) & 0xff) / 255f;
        color.b = ((intBits >>> 16) & 0xff) / 255f;
        color.a = ((intBits >>> 24) & 0xff) / 255f;
        return color;
    }

    public float getPackedColor() {
        return color;
    }


    public void draw(final TextureArrayRegion region, final float x, final float y, final float width, final float height) {
        checkBeginCalled();

        final float[] vertices = this.vertices;

        switchTextureAndFlushIfNecessary(region, vertices);

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.u;
        final float v = region.v2;
        final float u2 = region.u2;
        final float v2 = region.v;

        final float color = this.color;
        final float textureIndex = region.textureIndex;

        writeQuad(vertices, color, textureIndex,
                x, y, u, v,
                x, fy2, u, v2,
                fx2, fy2, u2, v2,
                fx2, y, u2, v);
    }

    private void switchTextureAndFlushIfNecessary(final TextureArrayRegion region, final float[] vertices) {
        final TextureArray textureArray = region.textureArray;
        if (textureArray != lastTextureArray) {
            switchTextureArray(textureArray);
        } else if (idx == vertices.length)
            flush();
    }

    private void writeQuad(
            final float[] vertices, final float color, final float textureIndex,
            final float x1, final float y1, final float u1, final float v1,
            final float x2, final float y2, final float u2, final float v2,
            final float x3, final float y3, final float u3, final float v3,
            final float x4, final float y4, final float u4, final float v4
    ) {
        int idx = this.idx;
        idx = writeVertex(idx, vertices, x1, y1, color, u1, v1, textureIndex);
        idx = writeVertex(idx, vertices, x2, y2, color, u2, v2, textureIndex);
        idx = writeVertex(idx, vertices, x3, y3, color, u3, v3, textureIndex);
        idx = writeVertex(idx, vertices, x4, y4, color, u4, v4, textureIndex);
        this.idx = idx;
    }

    private static int writeVertex(int idx, final float[] vertices,
                                   final float x, final float y, final float color, final float u, final float v, final float textureIndex) {
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = textureIndex;
        return idx;
    }

    private void checkBeginCalled() {
        if (!drawing) throw new IllegalStateException("begin must be called before");
    }

    public void flush() {
        if (idx == 0) return;

        renderCalls++;
        totalRenderCalls++;
        final int spritesInBatch = idx / vertexSize / verticesPerSprite;
        if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        final int count = spritesInBatch * indicesPerSprite;

        lastTextureArray.bind();
        final Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);

        if (blendingDisabled) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } else {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (blendSrcFunc != -1) Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
        }

        mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, count);

        idx = 0;
    }

    public void disableBlending() {
        if (blendingDisabled) return;
        flush();
        blendingDisabled = true;
    }

    public void enableBlending() {
        if (!blendingDisabled) return;
        flush();
        blendingDisabled = false;
    }

    public void setBlendFunction(final int srcFunc, final int dstFunc) {
        if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc) return;
        flush();
        blendSrcFunc = srcFunc;
        blendDstFunc = dstFunc;
    }

    public int getBlendSrcFunc() {
        return blendSrcFunc;
    }

    public int getBlendDstFunc() {
        return blendDstFunc;
    }

    @Override
    public void dispose() {
        mesh.dispose();
        if (ownsShader && shader != null) shader.dispose();
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4 getTransformMatrix() {
        return transformMatrix;
    }

    public void setProjectionMatrix(final Matrix4 projection) {
        if (drawing) flush();
        projectionMatrix.set(projection);
        if (drawing) setupUniforms();
    }

    public void setTransformMatrix(final Matrix4 transform) {
        if (drawing) flush();
        transformMatrix.set(transform);
        if (drawing) setupUniforms();
    }

    private void setupUniforms() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);

        final ShaderProgram sp = customShader != null ? customShader : shader;
        sp.setUniformMatrix("u_projTrans", combinedMatrix);
        sp.setUniformi("u_textureArray", 0);
    }

    protected void switchTextureArray(final TextureArray textureArray) {
        flush();
        lastTextureArray = textureArray;
        invTexWidth = 1.0f / textureArray.getWidth();
        invTexHeight = 1.0f / textureArray.getHeight();
    }

    public void setShader(final ShaderProgram shader) {
        if (drawing) {
            flush();
            if (customShader != null)
                customShader.end();
            else
                this.shader.end();
        }
        customShader = shader;
        if (drawing) {
            if (customShader != null)
                customShader.begin();
            else
                this.shader.begin();
            setupUniforms();
        }
    }

    public ShaderProgram getShader() {
        if (customShader == null) {
            return shader;
        }
        return customShader;
    }

    public boolean isBlendingEnabled() {
        return !blendingDisabled;
    }

    public boolean isDrawing() {
        return drawing;
    }
}
