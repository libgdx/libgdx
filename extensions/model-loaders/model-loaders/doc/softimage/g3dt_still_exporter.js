var VERSION = "g3dt-still-1.0";

function WriteHeader(str) {
	return str + VERSION + "\n";
}

/**
	Returns an Array of PolygonMeshes based on
	the given String Array of mesh names.
*/
function GetMeshes() {
	var result = new Array();
	var l = new Enumerator(Selection);
	var i = 0;
	for(; !l.atEnd(); l.moveNext()) {
		var obj = l.item();
		if(obj.Type == "polymsh") result[i++] = obj;
	}
	return result;
}

/**
	Fetches the UVs from the provided Clusters. The returned
	result is an array containing the UVW elements of each
	cluster.
**/
function GetUVs(clusters) {
	var result = new ActiveXObject("XSI.Collection");
	
	for(var i = 0; i < clusters.Count; i++) {
		var cluster = clusters(i);
		var uvSpace = null;
		for(var j = 0; j < cluster.Properties.Count; j++) {
			if(cluster.Properties(j).type == "uvspace") {				
				uvSpace = cluster.Properties(j);
				break;
			} 
		}	  

		if(uvSpace != null) {			
			var uvs = uvSpace.Elements.Array.toArray();
			result.add(uvs);
		}
	}
	return result;
}

function ExportGeometryFaces(geo) {
	var vertexId = 0;
	var out = geo.Polygons.Count + "\n";	
	for(var poly = 0; poly < geo.Polygons.Count; poly++) {
		var face = geo.Polygons(poly);		
		out += face.Vertices.Count;
		LogMessage("nodes: " + face.Nodes.Count + ", verts:" + face.Vertices.Count);
		for(var vert = 0; vert < face.Vertices.Count; vert++) {
			LogMessage(face.Nodes(vert).Index + ", " + face.Vertices(vert).Index);
			out += ", " + vertexId;
			vertexId++;
		}
		out += "\n";
	}
	out += vertexId + "\n";
	return out;
}

function ExportGeometryDescriptor(geo, exportNormals) {
	var uvs = GetUVs(geo.Clusters);
	
	var out = "";
	out += uvs.Count + (exportNormals?2:1) + "\n";
	out += "position\n";	
	if(exportNormals) out += "normals\n";
	for(var i = 0; i < uvs.Count; i++) {
		out += "uv\n";
	}
	return out;
}

function ExportGeometry(geo, exportNormals) {	
	var uvs = GetUVs(geo.Clusters);
	var out = "";
	var verts = 0;
	for(var poly = 0; poly < geo.Polygons.Count; poly++) {
		var face = geo.Polygons(poly);				
		for(var vert = 0; vert < face.Vertices.Count; vert++) {
			var vertex = face.Vertices(vert);
			var position = vertex.Position;
			var normal = vertex.Normal;
			out += position.X + "," + position.Y + "," + position.Z;
			if(exportNormals) out += "," + normal.X + "," + normal.X + "," + normal.Z;
			var idx = face.Nodes(vert).Index;
			for(var i = 0; i < uvs.Count; i++) {	
				var uv = uvs(i);			
				out += "," + uv[idx*3] + "," + uv[idx*3+1];
			}
			out += "\n";
			verts++;
		}
	}	
	return out;
}

function ExportG3DTStillModel(fileName, exportNormals) {
	if (typeof exportNormals == 'undefined' ) exportNormals = false;	
	
	LogMessage("===========  Exporting G3DT Stillmodel =========");		
	// write the header
	var out = "";
	out = WriteHeader(out);

	// fetch all meshes and get their respective UVs	
	var meshes = GetMeshes();		

	out += meshes.length + "\n";
	for(var i = 0; i < meshes.length; i++) {
		var mesh = meshes[i];
		LogMessage("Exporting mesh " + mesh.Name);
		out += mesh.Name + "\n";
		out += ExportGeometryFaces(mesh.Primitives(0).Geometry);
		out += ExportGeometryDescriptor(mesh.Primitives(0).Geometry);
		out += ExportGeometry(mesh.Primitives(0).Geometry);
	}
	
	var fso  = new ActiveXObject("Scripting.FileSystemObject"); 
	var fh = fso.CreateTextFile(fileName, true); 
	fh.WriteLine(out); 
	fh.Close();
	
	LogMessage("Done");
}

ExportG3DTStillModel("C:\\gdx-workspace\\gdx\\extensions\\model-loaders\\model-loaders\\data\\qbob\\world_blobbie_brushes.g3dt");