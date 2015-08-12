package com.badlogic.gdx.graphics.g3d.utils.helpers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * Allow to visualize a camera
 * @author realitix
 */
public class CameraHelper implements RenderableProvider {
	private Camera camera;
	private Matrix4 projectionMatrix = new Matrix4();
	private Matrix4 tmpM = new Matrix4();

	private Vector3 tmpV = new Vector3();
	private Mesh mesh;
	private Material material = new Material();
	private ObjectMap<String, Short> pointMap = new ObjectMap<String, Short>();
	private Array<Vector3> verticesPosition = new Array<Vector3>();
	private Array<Color> verticesColor = new Array<Color>();
	private Array<Short> verticesIndice = new Array<Short>();


	public CameraHelper(Camera camera) {
		this.camera = camera;
		init();
	}

	private void init() {
		mesh = new Mesh(false, 22, 50,
			new VertexAttribute(Usage.Position, 3, "a_position"),
			new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));

		material.set(ColorAttribute.createDiffuse(Color.WHITE));
		material.set(new DepthTestAttribute());

		initPoints();
	}

	private void initPoints() {
		// colors
		Color colorFrustum = new Color(1, 0.66f, 0, 1);
		Color colorCone = new Color(1, 0, 0, 1);
		Color colorUp = new Color(0, 0.66f, 1, 1);
		Color colorTarget = new Color(1, 1, 1, 1);
		Color colorCross = new Color(0.2f, 0.2f, 0.2f, 1);

		// near
		addLine( "n1", "n2", colorFrustum );
		addLine( "n2", "n4", colorFrustum );
		addLine( "n4", "n3", colorFrustum );
		addLine( "n3", "n1", colorFrustum );

		// far
		addLine( "f1", "f2", colorFrustum );
		addLine( "f2", "f4", colorFrustum );
		addLine( "f4", "f3", colorFrustum );
		addLine( "f3", "f1", colorFrustum );

		// sides
		addLine( "n1", "f1", colorFrustum );
		addLine( "n2", "f2", colorFrustum );
		addLine( "n3", "f3", colorFrustum );
		addLine( "n4", "f4", colorFrustum );

		// cone
		addLine( "p", "n1", colorCone );
		addLine( "p", "n2", colorCone );
		addLine( "p", "n3", colorCone );
		addLine( "p", "n4", colorCone );

		// up
		addLine( "u1", "u2", colorUp );
		addLine( "u2", "u3", colorUp );
		addLine( "u3", "u1", colorUp );

		// target
		addLine( "c", "t", colorTarget );
		addLine( "p", "c", colorCross );

		// cross
		addLine( "cn1", "cn2", colorCross );
		addLine( "cn3", "cn4", colorCross );
		addLine( "cf1", "cf2", colorCross );
		addLine( "cf3", "cf4", colorCross );
	}

	private void addLine( String a, String b, Color color ) {
		addPoint(a, color);
		addPoint(b, color);
		addIndices(a, b);
	}

	private void addPoint(String id, Color color) {
		if( !pointMap.containsKey(id) ) {
			verticesPosition.add(new Vector3());
			verticesColor.add(color);
			pointMap.put(id, (short)(verticesPosition.size - 1));
		}
	}

	private void addIndices(String p1, String p2) {
		verticesIndice.add(pointMap.get(p1));
		verticesIndice.add(pointMap.get(p2));
	}

	private void setPoint( String id, float x, float y, float z) {
		unproject(tmpV.set( x, y, z ));
		verticesPosition.get(pointMap.get(id)).set(tmpV);
	};


	private void unproject(Vector3 v) {
		tmpM.set(projectionMatrix);
		tmpM.inv();

		// Projection
		float x = v.x, y = v.y, z = v.z;
		float e[] = tmpM.val;
		float d = 1 / ( e[ 3 ] * x + e[ 7 ] * y + e[ 11 ] * z + e[ 15 ] ); // perspective divide
		v.x = ( e[ 0 ] * x + e[ 4 ] * y + e[ 8 ] * z + e[ 12 ] ) * d;
		v.y = ( e[ 1 ] * x + e[ 5 ] * y + e[ 9 ] * z + e[ 13 ] ) * d;
		v.z = ( e[ 2 ] * x + e[ 6 ] * y + e[ 10 ] * z + e[ 14 ] ) * d;
	}

	public void update() {
		int w = 1, h = 1;
		projectionMatrix.set(camera.combined);

		// position
		verticesPosition.get(pointMap.get("p")).set(camera.position);

		// center / target
		setPoint( "c", 0, 0, - 1 );
		setPoint( "t", 0, 0, 1 );

		// near
		setPoint( "n1", - w, - h, - 1 );
		setPoint( "n2", w, - h, - 1 );
		setPoint( "n3", - w, h, - 1 );
		setPoint( "n4", w, h, - 1 );

		// far
		setPoint( "f1", - w, - h, 1 );
		setPoint( "f2", w, - h, 1 );
		setPoint( "f3", - w, h, 1 );
		setPoint( "f4", w, h, 1 );

		// up
		setPoint( "u1", w * 0.7f, h * 1.1f, - 1 );
		setPoint( "u2", - w * 0.7f, h * 1.1f, - 1 );
		setPoint( "u3", 0, h * 2, - 1 );

		// cross
		setPoint( "cf1", - w, 0, 1 );
		setPoint( "cf2", w, 0, 1 );
		setPoint( "cf3", 0, - h, 1 );
		setPoint( "cf4", 0, h, 1 );
		setPoint( "cn1", - w, 0, - 1 );
		setPoint( "cn2", w, 0, - 1 );
		setPoint( "cn3", 0, - h, - 1 );
		setPoint( "cn4", 0, h, - 1 );

		updateMesh();
	}

	private void updateMesh() {
		// Vertices
		float verticesBuffer[] = new float[verticesPosition.size*(mesh.getVertexSize()/4)];

		for(int i = 0; i < verticesPosition.size; i++) {
			// 3 vertices + 1 color packed
			int j = i * 7;
			verticesBuffer[j] = verticesPosition.get(i).x;
			verticesBuffer[j+1] = verticesPosition.get(i).y;
			verticesBuffer[j+2] = verticesPosition.get(i).z;
			verticesBuffer[j+3] = verticesColor.get(i).r;
			verticesBuffer[j+4] = verticesColor.get(i).g;
			verticesBuffer[j+5] = verticesColor.get(i).b;
			verticesBuffer[j+6] = verticesColor.get(i).a;
		}
		mesh.setVertices(verticesBuffer);

		// Indices
		short indicesBuffer[] = new short[verticesIndice.size];

		for(int i = 0; i < verticesIndice.size; i++) {
			indicesBuffer[i] = verticesIndice.get(i);
		}
		mesh.setIndices(indicesBuffer);
	}


	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		Renderable r = pool.obtain();
		r.worldTransform.idt();
		r.mesh = mesh;
		r.meshPartOffset = 0;
		r.meshPartSize = mesh.getNumIndices();
		r.primitiveType = GL20.GL_LINES;
		r.material = material;
		r.bones = null;
		r.userData = null;

		renderables.add(r);
	}
}
