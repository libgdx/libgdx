package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;

public class BigMeshTest extends GdxTest {

	/** copied from {@link ModelBatch} */
	protected static class RenderablePool extends FlushablePool<Renderable> {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.meshPart.set("", null, 0, 0, 0);
			renderable.shader = null;
			renderable.userData = null;
			return renderable;
		}
	}
	
	private Camera camera;
	private ModelBatch batch;
	private final Array<RenderableProvider> renderableProviders = new Array<RenderableProvider>();
	
	@Override
	public void create () {
		
		batch = new ModelBatch();
		
		camera = new PerspectiveCamera();
		camera.position.set(0, 1, 1).scl(3);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .1f;
		camera.far = 100f;
		
		final Material material = new Material();
		material.set(ColorAttribute.createDiffuse(Color.ORANGE));
		
		final VertexAttributes attributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
		final long attributesMask = attributes.getMask();
		
		ModelBuilder mb = new ModelBuilder();
		
		mb.begin();
		MeshPartBuilder mpb = mb.part("ellipse", GL20.GL_TRIANGLES, attributes, material);
		
		// create an ellipse with 64k vertices
		float width = 1;
		float height = 1;
		float angleFrom = 0;
		float angleTo = 360;
		int divisions = (1 << 16) - 2; // ellipse: vertices count = divisions + 2
		EllipseShapeBuilder.build(mpb, width, height, 0, 0, divisions, 
			0, 0, 0, 
			0, 1, 0, 
			1, 0, 0, 
			0, 0, 1, 
			angleFrom,
			angleTo);
		
		Model model = mb.end();
		
		// Test few method relying on short indices
		ModelInstance modelInstance = new ModelInstance(model);
		System.out.println(modelInstance.calculateBoundingBox(new BoundingBox()));
		Mesh mesh = model.nodes.first().parts.first().meshPart.mesh;
		System.out.println(mesh.calculateRadius(0f, 0f, 0f, 0, mesh.getNumIndices(), new Matrix4()));
		
		// model cache (simple)
		modelInstance.transform.setTranslation(-2, 0, 0);
		ModelCache modelCache = new ModelCache(new ModelCache.Sorter(), new ModelCache.SimpleMeshPool());
		modelCache.begin();
		modelCache.add(modelInstance);
		modelCache.end();
		
		// model cache (tight)
		modelInstance.transform.setTranslation(2, 0, 0);
		ModelCache modelCacheTight = new ModelCache(new ModelCache.Sorter(), new ModelCache.TightMeshPool());
		modelCacheTight.begin();
		modelCacheTight.add(modelInstance);
		modelCacheTight.end();
		
		modelInstance.transform.setTranslation(0, 0, 0);
		
		renderableProviders.add(modelInstance);
		renderableProviders.add(modelCache);
		renderableProviders.add(modelCacheTight);

		trace(modelInstance, "Model base");
		trace(modelCache, "Model cache (simple)");
		trace(modelCacheTight, "Model cache (tight)");
	}
	
	private void trace(RenderableProvider rp, String label){
		Array<Renderable> renderables = new Array<Renderable>();
		Pool<Renderable> pool = new RenderablePool();
		rp.getRenderables(renderables, pool);
		System.out.println(label + ":");
		System.out.println("- renderables: " + renderables.size);
		for(Renderable r : renderables){
			Mesh mesh = r.meshPart.mesh;
			System.out.println("-- renderable [" + String.valueOf(r.meshPart.id) + "]: ");
			System.out.println("-- mesh part offset: " + r.meshPart.offset);
			System.out.println("-- mesh part size: " + r.meshPart.size);
			System.out.println("-- mesh num vertices: " + mesh.getNumVertices());
			System.out.println("-- mesh num indices: " + mesh.getNumIndices());
		}
	}
	
	@Override
	public void render () {
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.update();
		
		batch.begin(camera);
		batch.render(renderableProviders);
		batch.end();
	}
	
}
