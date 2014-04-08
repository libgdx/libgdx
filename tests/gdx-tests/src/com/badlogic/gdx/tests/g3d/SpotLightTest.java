package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class SpotLightTest extends ModelTest{
	SpotLight spotLight;
	Model lightModel;
	Renderable pLight;
	Vector3 center = new Vector3(), transformedCenter = new Vector3(), tmpV = new Vector3();
	float radius = 1f;

	float planSize = 20;
	float planDivision = 50;
	
	@Override
	public void create () {
		super.create();
		environment.clear();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.0f, 0.0f, 1.0f));
		environment.add(spotLight = new SpotLight().set(0.2f, 0.8f, 0.2f, new Vector3(), 1, new Vector3(), 1,0.0f,0.0f, 20,3f));

		
		//Make my own texture plane with a lot of vertices
				
		ModelBuilder mb = new ModelBuilder();

		mb.begin();
		MeshPartBuilder mpbuilder = mb.part("grid", GL20.GL_TRIANGLES,  Usage.Position |  Usage.Normal |  Usage.TextureCoordinates, new Material());
		VertexInfo vi00 = new VertexInfo();
		VertexInfo vi10 = new VertexInfo();
		VertexInfo vi11 = new VertexInfo();
		VertexInfo vi01 = new VertexInfo();
		
		Vector3 normal = Vector3.Y.cpy();
		float length = planSize / planDivision;
		float uvLength = 1/planDivision;
				
		for(int iz = 0 ; iz < planDivision ; ++iz){
			
			for(int ix = 0 ; ix < planDivision ; ++ix){
				
				vi00 = new VertexInfo(); vi00.hasPosition = vi00.hasNormal = vi00.hasUV = true;
				vi10 = new VertexInfo(); vi10.hasPosition = vi10.hasNormal = vi10.hasUV = true;
				vi11 = new VertexInfo(); vi11.hasPosition = vi11.hasNormal = vi11.hasUV = true;
				vi01 = new VertexInfo(); vi01.hasPosition = vi01.hasNormal = vi01.hasUV = true;

				vi00.setNor(normal);
				vi10.setNor(normal);
				vi11.setNor(normal);
				vi01.setNor(normal);
				
				vi00.setPos(-planSize/2 + length*ix, 0, -planSize/2 + length*iz);
				vi10.setPos(-planSize/2 + length*ix, 0,  -planSize/2 + length*(iz+1));
				vi11.setPos(-planSize/2 + length*(ix+1), 0, -planSize/2 + length*(iz+1));
				vi01.setPos(-planSize/2 + length*(ix+1), 0, -planSize/2 + length*iz);
				
				vi00.setUV(uvLength*ix, uvLength*iz);
				vi10.setUV(uvLength*ix, uvLength*(iz+1));
				vi11.setUV(uvLength*(ix+1), uvLength*(iz+1));
				vi01.setUV(uvLength*(ix+1), uvLength*iz);
				
				mpbuilder.rect(vi00, vi10, vi11, vi01);
				
			}			
			
		}
		
		instances.add(new ModelInstance(mb.end()));
		
		lightModel = mb.createSphere(1, 1, 1, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
		lightModel.nodes.get(0).parts.get(0).setRenderable(pLight = new Renderable());
	}

	@Override
	protected void onLoaded () {
		//no need to load a specified model
		//super.onLoaded();
		
		BoundingBox bounds = instances.get(0).calculateBoundingBox(new BoundingBox());
		center.set(bounds.getCenter());
		radius = bounds.getDimensions().len() * .5f;
		spotLight.position.set(0, radius, 0).add(transformedCenter.set(center).mul(transform));
		//spotLight.intensity = radius * radius;
		((ColorAttribute)pLight.material.get(ColorAttribute.Diffuse)).color.set(spotLight.color);
		final float s = 0.2f * radius;
		pLight.worldTransform.setToScaling(s, s, s);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		final float delta = Gdx.graphics.getDeltaTime();
		spotLight.position.sub(transformedCenter);
		spotLight.position.rotate(Vector3.X, delta * 50f);
		spotLight.position.rotate(Vector3.Y, delta * 13f);
		spotLight.position.rotate(Vector3.Z, delta * 3f);
		spotLight.position.add(transformedCenter.set(center).mul(transform));
		spotLight.direction.set(spotLight.position.cpy().scl(-1).nor());

		pLight.worldTransform.setTranslation(spotLight.position);		
		batch.render(pLight);

		super.render(batch, instances);
	}

	@Override
	public void dispose () {
		lightModel.dispose();
		super.dispose();
	}

}
