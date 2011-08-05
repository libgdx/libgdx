package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SortedIntList;

import java.util.Iterator;

/**
 * <p>
 * Renderer for {@link Decal} objects.
 * </p>
 * <p>
 * New objects are added using {@link DecalBatch#add(Decal)}, there is no limit on how many decals can be added.<br/>
 * Once all the decals have been submitted a call to {@link DecalBatch#flush()} will
 * batch them together and send big chunks of geometry to the GL.
 * </p>
 * <p>
 * The size of the batch specifies the maximum number of decals that can be batched together before they have to be
 * submitted to the graphics pipeline. The default size is {@link DecalBatch#DEFAULT_SIZE}. If it is known before hand
 * that not as many will be needed on average the batch can be downsized to save memory. If the game is basically 3d
 * based and decals will only be needed for an orthogonal HUD it makes sense to tune the size down.
 * </p>
 * <p>
 * The way the batch handles things depends on the {@link GroupStrategy}. Different strategies can be used to customize
 * shaders, states, culling etc. for more details see the {@link GroupStrategy} java doc.<br/>
 * While it shouldn't be necessary to change strategies, if you have to do so, do it before calling {@link #add(Decal)},
 * and if you already did, call {@link #flush()} first.
 * </p>
 */
public class DecalBatch implements Disposable {
	private static final int DEFAULT_SIZE = 1000;
	private float[] vertices;
	private Mesh mesh;

	private SortedIntList<ObjectMap<DecalMaterial, Array<Decal>>> groupList = new SortedIntList<ObjectMap<DecalMaterial, Array<Decal>>>();

	private GroupStrategy groupStrategy;

	/**
	 * Creates a new batch using the {@link DefaultGroupStrategy}
	 */
	public DecalBatch() {
		this(DEFAULT_SIZE, new DefaultGroupStrategy());
	}

	public DecalBatch(GroupStrategy groupStrategy) {
		this(DEFAULT_SIZE, groupStrategy);
	}

	public DecalBatch(int size, GroupStrategy groupStrategy) {
		initialize(size);
		setGroupStrategy(groupStrategy);
	}

	/**
	 * Sets the {@link GroupStrategy} used
	 * @param groupStrategy Group strategy to use
	 */
	public void setGroupStrategy(GroupStrategy groupStrategy) {
		this.groupStrategy = groupStrategy;
	}

	/**
	 * Initializes the batch with the given amount of decal objects the buffer is able to hold when full.
	 *
	 * @param size Maximum size of decal objects to hold in memory
	 */
	public void initialize(int size) {
		vertices = new float[size * Decal.SIZE];
		mesh = new Mesh(
				Mesh.VertexDataType.VertexArray, false, size * 4, size * 6,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));

		short[] indices = new short[size * 6];
		int v = 0;
		for(int i = 0; i < indices.length; i += 6, v += 4) {
			indices[i] = (short) (v);
			indices[i + 1] = (short) (v + 2);
			indices[i + 2] = (short) (v + 1);
			indices[i + 3] = (short) (v + 1);
			indices[i + 4] = (short) (v + 2);
			indices[i + 5] = (short) (v + 3);
		}
		mesh.setIndices(indices);
	}

	/**
	 * @return maximum amount of decal objects this buffer can hold in memory
	 */
	public int getSize() {
		return vertices.length / Decal.SIZE;
	}

	/**
	 * Add a decal to the batch, marking it for later rendering
	 *
	 * @param decal Decal to add for rendering
	 */
	public void add(Decal decal) {
		DecalMaterial material = decal.getMaterial();
		int groupIndex = groupStrategy.decideGroup(decal);
		ObjectMap<DecalMaterial, Array<Decal>> targetGroup = groupList.get(groupIndex);
		if(targetGroup == null) {
			targetGroup = new ObjectMap<DecalMaterial, Array<Decal>>();
			groupList.insert(groupIndex, targetGroup);
		}
		Array<Decal> targetList = targetGroup.get(material);
		if(targetList == null) {
			//create unordered arrays for all lists
			targetList = new Array<Decal>(false, 16);
			targetGroup.put(material, targetList);
		}
		targetList.add(decal);
	}

	/**
	 * Flush this batch sending all contained decals to GL. After flushing the batch is empty once again.
	 */
	public void flush() {
		render();
		clear();
	}

	/**
	 * Renders all decals to the buffer and flushes the buffer to the GL when full/done
	 */
	protected void render() {
		groupStrategy.beforeGroups();
		for(SortedIntList.Node<ObjectMap<DecalMaterial, Array<Decal>>> group : groupList) {
			groupStrategy.beforeGroup(group.index, group.value.values());
			for(ObjectMap.Entry<DecalMaterial, Array<Decal>> groupEntry : group.value.entries()) {
				render(groupEntry.key, groupEntry.value);
			}
			groupStrategy.afterGroup(group.index);
		}
		groupStrategy.afterGroups();
	}

	/**
	 * Renders a group of vertices to the buffer, flushing them to GL when done/full
	 *
	 * @param material Material of that group to set
	 * @param decals   Decals to render
	 */
	private void render(DecalMaterial material, Array<Decal> decals) {
		//apply the groups material
		material.set();

		//batch vertices
		int idx = 0;
		for(Decal decal : decals) {
			decal.update();
			System.arraycopy(decal.vertices, 0, vertices, idx, decal.vertices.length);
			idx += decal.vertices.length;
			//if our batch is full we have to flush it
			if(idx == vertices.length) {
				flush(idx);
				idx = 0;
			}
		}
		//at the end if there is stuff left in the batch we render that
		if(idx > 0) {
			flush(idx);
		}
	}

	/**
	 * Flushes vertices[0,verticesPosition[ to GL
	 * verticesPosition % Decal.SIZE must equal 0
	 *
	 * @param verticesPosition Amount of elements from the vertices array to flush
	 */
	protected void flush(int verticesPosition) {
		mesh.setVertices(vertices, 0, verticesPosition);
		mesh.render(GL10.GL_TRIANGLES, 0, verticesPosition / 4);
	}

	/**
	 * Remove all decals from batch
	 */
	protected void clear() {
		groupList.clear();
	}

	/**
	 * Frees up memory by dropping the buffer and underlying resources.
	 * If the batch is needed again after disposing it can be {@link #initialize(int) initialized} again.
	 */
	public void dispose() {
		clear();
		vertices = null;
		mesh.dispose();
	}
}
