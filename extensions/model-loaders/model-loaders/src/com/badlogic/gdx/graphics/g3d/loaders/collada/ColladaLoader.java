package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Xml;
import com.badlogic.gdx.utils.Xml.Element;

public class ColladaLoader {
	public static StillModel loadStillModel(FileHandle handle) {
		return loadStillModel(handle.read());
	}
	
	public static StillModel loadStillModel(InputStream in) {
		Xml xml = new Xml();
		Element root = null;
		try {
			root = xml.parse(in);
		} catch(Exception e) {
			throw new GdxRuntimeException("Couldn't load Collada model", e);
		}
		
		// get geometries
		Array<Geometry> geos = readGeometries(root);
		
		// convert geometries to meshes
		StillSubMesh[] meshes = createMeshes(geos);
		
		// create StillModel
		StillModel model = new StillModel(meshes);		
		return model;
	}
	
	private static Array<Geometry> readGeometries(Element root) {		
		// check whether the library_geometries element is there
		Element colladaGeoLibrary = root.getChildByName("library_geometries");
		if(colladaGeoLibrary == null) throw new GdxRuntimeException("not <library_geometries> element in file");
		
		// check for geometries
		Array<Element> colladaGeos = colladaGeoLibrary.getChildrenByName("geometry");
		if(colladaGeos.size == 0) throw new GdxRuntimeException("no <geometry> elements in file");
		
		Array<Geometry> geometries = new Array<Geometry>();
		
		// read in all geometries
		for(int i = 0; i < colladaGeos.size; i++) {		
			try {
				geometries.add(new Geometry(colladaGeos.get(i)));
			} catch(GdxRuntimeException e) {
				System.out.println("warning: " + e.getMessage());
			}
		}
		
		return geometries;
	}		
	
	private static StillSubMesh[] createMeshes(Array<Geometry> geos) {
		StillSubMesh[] meshes = new StillSubMesh[geos.size];
		for(int i = 0; i < geos.size; i++) {			
			StillSubMesh subMesh = new StillSubMesh(geos.get(i).id, geos.get(i).getMesh(), GL10.GL_TRIANGLES);			
			subMesh.material = new Material("Null Material");			
			meshes[i] = subMesh;
		}
		return meshes;		
	}
	
	public static void main(String[] argv) throws FileNotFoundException, IOException {
//		loadStillModel(new FileInputStream("data/boy_plotted.dae"));
		loadStillModel(new FileInputStream("data/cubes.dae"));
		loadStillModel(new FileInputStream("data/cubes_poly.dae"));
	}
}
