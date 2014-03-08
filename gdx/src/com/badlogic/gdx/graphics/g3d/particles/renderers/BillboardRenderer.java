package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class BillboardRenderer extends Renderer<BillboardParticle> {
	//Attributes	
	protected static final int scaleAndRotationUsage = 1 << 9, directionUsage = 1 << 10;
	private static final VertexAttributes 
		GPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
															new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
															new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE), 
															new VertexAttribute(scaleAndRotationUsage, 4, "a_scaleAndRotation")),
		GPU_EXT_ATTRIBUTES = new VertexAttributes(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
									new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
									new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE), 
									new VertexAttribute(scaleAndRotationUsage, 4, "a_scaleAndRotation"),
									new VertexAttribute(directionUsage, 3, "a_direction")),
		CPU_ATTRIBUTES = new VertexAttributes(	new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), 
															new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"), 
															new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE) );
	
	//Offsets
	private static final int 	GPU_POSITION_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										GPU_UV_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										GPU_SCALE_ROTATION_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(scaleAndRotationUsage).offset/4),
										GPU_COLOR_OFFSET = (short)(GPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										GPU_VERTEX_SIZE = GPU_ATTRIBUTES.vertexSize/4,

										//Ext
										GPU_EXT_POSITION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										GPU_EXT_UV_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										GPU_EXT_SCALE_ROTATION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(scaleAndRotationUsage).offset/4),
										GPU_EXT_COLOR_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										GPU_EXT_DIRECTION_OFFSET = (short)(GPU_EXT_ATTRIBUTES.findByUsage(directionUsage).offset/4),
										GPU_EXT_VERTEX_SIZE = GPU_EXT_ATTRIBUTES.vertexSize/4,
										
										//Cpu
										CPU_POSITION_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Position).offset/4),
										CPU_UV_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.TextureCoordinates).offset/4),
										CPU_COLOR_OFFSET = (short)(CPU_ATTRIBUTES.findByUsage(Usage.Color).offset/4),
										CPU_VERTEX_SIZE= CPU_ATTRIBUTES.vertexSize/4;

	public static enum AlignMode{
		Screen, ViewPoint, ParticleDirection
	}

	protected boolean useGPU, isAdditive;
	protected AlignMode mode;
	protected Renderable renderable;
	private Camera camera; //used when gpu mode is off
	private float[] vertices;
	private short[] indices;
	private Texture texture;

	public BillboardRenderer(AlignMode mode, boolean useGPU){
		this.mode = mode;
		this.useGPU = useGPU;
	}
	
	public BillboardRenderer (BillboardRenderer billboardRenderer) {
		set(billboardRenderer);
	}

	public void setCamera(Camera camera){
		this.camera = camera;
	}
	
	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(pool.obtain().set(renderable));
	}

	@Override
	public void update () {
		BillboardParticle[] particles = controller.particles;
		short vo = 0; // the current vertex
		int fo = 0; // the current offset in the vertex array
		int io = 0; // the current offset in the indices array
		if(useGPU){
			if(mode != AlignMode.ParticleDirection){
				for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
					BillboardParticle particle = particles[i];
					float sx = particle.halfWidth * particlesRefScaleX*particle.scale, 
							sy = particle.halfHeight * particlesRefScaleY*particle.scale;

					//bottom left, bottom right, top right, top left
					putVertex(vertices, fo, particle, -sx, -sy, particle.u, particle.v2); fo+= GPU_VERTEX_SIZE;
					putVertex(vertices, fo, particle, sx, -sy, particle.u2, particle.v2); fo+= GPU_VERTEX_SIZE;
					putVertex(vertices, fo, particle, sx, sy, particle.u2, particle.v); fo+= GPU_VERTEX_SIZE;			
					putVertex(vertices, fo, particle, -sx, sy, particle.u, particle.v); fo+= GPU_VERTEX_SIZE;

					indices[io] = vo;
					indices[io+1] = (short)(vo+1);
					indices[io+2] = (short)(vo+2);
					indices[io+3] = (short)(vo+2);
					indices[io+4] = (short)(vo+3);
					indices[io+5] = vo;
					vo += 4;
					io += 6;
				}
			}
			else {
				for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
					BillboardParticle particle = particles[i];
					float sx = particle.scale * particle.halfWidth * particlesRefScaleX, 
							sy = particle.scale * particle.halfHeight * particlesRefScaleY;
					//bottom left, bottom right, top right, top left
					TMP_V1.set(particle.velocity).nor();
					putVertex(vertices, fo, particle, -sx, -sy, particle.u, particle.v2, TMP_V1); fo+= GPU_EXT_VERTEX_SIZE;
					putVertex(vertices, fo, particle, sx, -sy,  particle.u2, particle.v2, TMP_V1); fo+= GPU_EXT_VERTEX_SIZE;
					putVertex(vertices, fo, particle, sx, sy, particle.u2, particle.v, TMP_V1); fo+= GPU_EXT_VERTEX_SIZE;			
					putVertex(vertices, fo, particle, -sx, sy, particle.u, particle.v, TMP_V1); fo+= GPU_EXT_VERTEX_SIZE;

					indices[io] = vo;
					indices[io+1] = (short)(vo+1);
					indices[io+2] = (short)(vo+2);
					indices[io+3] = (short)(vo+2);
					indices[io+4] = (short)(vo+3);
					indices[io+5] = vo;
					vo += 4;
					io += 6;
				}
			}
		}
		else {
			if(mode == AlignMode.Screen){
				Vector3 look = TMP_V3.set(camera.direction).scl(-1),  //normal
						right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
						up = camera.up;
				
				for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
					BillboardParticle particle = particles[i];
					float sx = particle.halfWidth * particlesRefScaleX * particle.scale, 
						sy = particle.halfHeight * particlesRefScaleY * particle.scale;
					TMP_V1.set(right).scl(sx);
					TMP_V2.set(up).scl(sy);
					
					if(particle.cosRotation != 1){
						TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).sub(TMP_V2).mul(TMP_M3), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).sub(TMP_V2).mul(TMP_M3), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).add(TMP_V2).mul(TMP_M3), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).add(TMP_V2).mul(TMP_M3), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}
					else {
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).sub(TMP_V2), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).sub(TMP_V2), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(TMP_V1).add(TMP_V2), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(TMP_V1).add(TMP_V2), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}
					
					indices[io] = vo;
					indices[io+1] = (short)(vo+1);
					indices[io+2] = (short)(vo+2);
					indices[io+3] = (short)(vo+2);
					indices[io+4] = (short)(vo+3);
					indices[io+5] = vo;
					vo += 4;
					io += 6;
				}
			}
			else if(mode == AlignMode.ViewPoint){
				for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
					BillboardParticle particle = particles[i];
					Vector3 look = TMP_V3.set(camera.position).sub(particle.x, particle.y, particle.z).nor(), //normal
							right = TMP_V4.set(camera.up).crs(look).nor(), //tangent
							up = TMP_V5.set(look).crs(right);
					TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
					right.scl(particle.scale * particle.halfWidth * particlesRefScaleX);
					up.scl(particle.scale * particle.halfHeight * particlesRefScaleY);

					if(particle.cosRotation != 1){
						TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up).mul(TMP_M3), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up).mul(TMP_M3), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up).mul(TMP_M3), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up).mul(TMP_M3), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}
					else {
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}

					indices[io] = vo;
					indices[io+1] = (short)(vo+1);
					indices[io+2] = (short)(vo+2);
					indices[io+3] = (short)(vo+2);
					indices[io+4] = (short)(vo+3);
					indices[io+5] = vo;
					vo += 4;
					io += 6;
				}
			}
			else if(mode == AlignMode.ParticleDirection){
				for (int i = 0, count = controller.emitter.activeCount; i < count; ++i) {
					BillboardParticle particle = particles[i];
					Vector3 up = TMP_V1.set(particle.velocity).nor(),		
							look = TMP_V3.set(camera.position).sub(particle.x, particle.y, particle.z).nor(), //normal
							right = TMP_V4.set(up).crs(look).nor(); //tangent
					
					look.set(right).crs(up).nor();
					TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
					
					right.scl(particle.scale * particle.halfWidth * particlesRefScaleX);
					up.scl(particle.scale * particle.halfHeight * particlesRefScaleY);

					if(particle.cosRotation != 1){
						TMP_M3.setToRotation(look, particle.cosRotation, particle.sinRotation);
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up).mul(TMP_M3), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up).mul(TMP_M3), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up).mul(TMP_M3), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up).mul(TMP_M3), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}
					else {
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).sub(up), particle,  particle.u, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).sub(up), particle,  particle.u2, particle.v2); fo+= CPU_VERTEX_SIZE;
						putVertex(vertices, fo, TMP_V6.set(0,0,0).add(right).add(up), particle,  particle.u2, particle.v); fo+= CPU_VERTEX_SIZE;			
						putVertex(vertices, fo, TMP_V6.set(0,0,0).sub(right).add(up), particle,  particle.u, particle.v); fo+= CPU_VERTEX_SIZE;
					}

					indices[io] = vo;
					indices[io+1] = (short)(vo+1);
					indices[io+2] = (short)(vo+2);
					indices[io+3] = (short)(vo+2);
					indices[io+4] = (short)(vo+3);
					indices[io+5] = vo;
					vo += 4;
					io += 6;
				}
			}
		}
		renderable.meshPartSize = io;
		renderable.mesh.setVertices(vertices, 0, fo);
		renderable.mesh.setIndices(indices, 0, io);
	}
	
	//GPU
	//Required + Color + Rotation
	private static void putVertex(float[] vertices, int offset, BillboardParticle particle, float scaleX, float scaleY, float u, float v) {
		//Position
		vertices[offset + GPU_POSITION_OFFSET] = particle.x;
		vertices[offset + GPU_POSITION_OFFSET+1] = particle.y;
		vertices[offset + GPU_POSITION_OFFSET+2] = particle.z;
		//UV
		vertices[offset + GPU_UV_OFFSET] = u;
		vertices[offset + GPU_UV_OFFSET+1] = v;
		//Scale
		vertices[offset + GPU_SCALE_ROTATION_OFFSET] = scaleX;
		vertices[offset + GPU_SCALE_ROTATION_OFFSET+1] = scaleY;
		vertices[offset + GPU_SCALE_ROTATION_OFFSET+2] = particle.cosRotation;
		vertices[offset + GPU_SCALE_ROTATION_OFFSET+3] = particle.sinRotation;
		//Color
		vertices[offset + GPU_COLOR_OFFSET] = particle.r;
		vertices[offset + GPU_COLOR_OFFSET+1] = particle.g;
		vertices[offset + GPU_COLOR_OFFSET+2] = particle.b;
		vertices[offset + GPU_COLOR_OFFSET+3] = particle.a;
	}

	//Required + Color + Rotation + Direction
	private static void putVertex(float[] vertices, int offset, BillboardParticle particle, float scaleX, float scaleY, float u, float v, Vector3 direction) {
		//Position
		vertices[offset + GPU_EXT_POSITION_OFFSET] = particle.x;
		vertices[offset + GPU_EXT_POSITION_OFFSET+1] = particle.y;
		vertices[offset + GPU_EXT_POSITION_OFFSET+2] = particle.z;
		//UV
		vertices[offset + GPU_EXT_UV_OFFSET] = u;
		vertices[offset + GPU_EXT_UV_OFFSET+1] = v;
		//Scale
		vertices[offset + GPU_EXT_SCALE_ROTATION_OFFSET] = scaleX;
		vertices[offset + GPU_EXT_SCALE_ROTATION_OFFSET+1] = scaleY;
		vertices[offset + GPU_EXT_SCALE_ROTATION_OFFSET+2] = particle.cosRotation;
		vertices[offset + GPU_EXT_SCALE_ROTATION_OFFSET+3] = particle.sinRotation;
		//Color
		vertices[offset + GPU_EXT_COLOR_OFFSET] = particle.r;
		vertices[offset + GPU_EXT_COLOR_OFFSET+1] = particle.g;
		vertices[offset + GPU_EXT_COLOR_OFFSET+2] = particle.b;
		vertices[offset + GPU_EXT_COLOR_OFFSET+3] = particle.a;
		//Direction
		vertices[offset + GPU_EXT_DIRECTION_OFFSET] = direction.x;
		vertices[offset + GPU_EXT_DIRECTION_OFFSET +1] = direction.y;
		vertices[offset + GPU_EXT_DIRECTION_OFFSET +2] = direction.z;
	}
	
	
	//CPU
	//Required
	private static void putVertex(float[] vertices, int offset, Vector3 point, BillboardParticle particle, float u, float v) {
		//Position
		vertices[offset + CPU_POSITION_OFFSET] = point.x + particle.x;
		vertices[offset + CPU_POSITION_OFFSET+1] = point.y + particle.y;
		vertices[offset + CPU_POSITION_OFFSET+2] = point.z + particle.z;
		//UV
		vertices[offset + CPU_UV_OFFSET] = u;
		vertices[offset + CPU_UV_OFFSET+1] = v;
		//Color
		vertices[offset + CPU_COLOR_OFFSET] = particle.r;
		vertices[offset + CPU_COLOR_OFFSET+1] = particle.g;
		vertices[offset + CPU_COLOR_OFFSET+2] = particle.b;
		vertices[offset + CPU_COLOR_OFFSET+3] = particle.a;
	}

	@Override
	public void init () {
		//Vertex attributes and positions
		VertexAttributes attributes;
		int vertexSize;
		if(useGPU){
			if(mode == AlignMode.ParticleDirection){
				attributes = GPU_EXT_ATTRIBUTES;
				vertexSize = GPU_EXT_VERTEX_SIZE;
			}
			else{
				attributes = GPU_ATTRIBUTES;
				vertexSize = GPU_VERTEX_SIZE;
			}
		}
		else {
			attributes = CPU_ATTRIBUTES;
			vertexSize = CPU_VERTEX_SIZE;
		}

		int 	particleCount = controller.emitter.maxParticleCount,
				verticesCount = particleCount * 4,
				verticesFCount = verticesCount * vertexSize,
				indicesCount = particleCount * 6;
		boolean allocVertices = vertices == null || vertices.length != verticesFCount;
		if(allocVertices){
			vertices = new float[verticesFCount];
			indices = new short[indicesCount];
		}
		
		//Renderable allocation
		if(renderable == null){
			renderable = new Renderable();
			renderable.primitiveType = GL20.GL_TRIANGLES;
			renderable.meshPartOffset = 0;
			renderable.material = new Material(new BlendingAttribute(1f),
				new DepthTestAttribute(GL20.GL_LEQUAL, false), 
				TextureAttribute.createDiffuse(null));
		}
		
		//Mesh allocation
		if(allocVertices){
			if(renderable.mesh != null) 
				renderable.mesh.dispose();
			renderable.mesh = new Mesh(false, verticesCount, indicesCount, attributes);
		}


		//Select the right shader
		if(useGPU){
			/*
			if(renderable.shader == null || renderable.shader instanceof DefaultShader || 
				((ParticleShader)renderable.shader) ){
			 */
			renderable.shader = new ParticleShader(renderable, new ParticleShader.Config(mode));
			renderable.shader.init();

		}
		else {
			renderable.shader = new DefaultShader(renderable);
			renderable.shader.init();
		}
		
		setAdditive (isAdditive);
		setTexture(texture);
	}

	public void setTexture(Texture texture){
		if(renderable != null){
			TextureAttribute attribute = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
			attribute.textureDescription.texture = texture;
		}
		this.texture = texture;
	}

	public void setAdditive (boolean isAdditive) {
		if(renderable != null){
			BlendingAttribute blendingAttribute = (BlendingAttribute) renderable.material.get(BlendingAttribute.Type);
			if(isAdditive){
				blendingAttribute.sourceFunction =  GL20.GL_SRC_ALPHA; 
				blendingAttribute.destFunction = GL20.GL_ONE;
			}
			else {
				blendingAttribute.sourceFunction = GL20.GL_SRC_ALPHA; 
				blendingAttribute.destFunction = GL20.GL_ONE_MINUS_SRC_ALPHA;
			}
		}
		this.isAdditive = isAdditive;
	}
	
	public boolean isAdditive(){
		return isAdditive;
	}
	
	/** Sets the current align mode.
	 *  A call to init method must follow, undefined behavior otherwise. */
	public void setAlignMode(AlignMode mode){
		this.mode = mode;
	}
	
	public AlignMode getAlignMode(){
		return mode;
	}
	
	/** Sets the current align mode.
	 *  A call to init method must follow, undefined behavior otherwise. */
	public void setUseGpu(boolean useGPU){
		this.useGPU = useGPU;
	}
	
	public boolean isUseGPU(){
		return useGPU;
	}
	
	
	@Override
	public BillboardRenderer copy () {
		return new BillboardRenderer(this);
	}

	public void set (BillboardRenderer system) {
		useGPU = system.useGPU;
		isAdditive = system.isAdditive;
		mode = system.mode;
		camera = system.camera;
	}

}
