import os
import time
import shutil

import bpy
import mathutils
import io_utils

def collect_faces(mesh):
	faces = []
	
	# go over all faces and collect their vertices
	for faceIndex in range(len(mesh.faces)):
		
		faceVertices = []
		face = mesh.faces[faceIndex]
		
		# go over all the vertices in this face and collect
		# its position, normal and uvs
		for vertexIndex in range(len(face.vertices)):
			
			v = face.vertices[vertexIndex]
			vertex = []
			vertex.append(mesh.vertices[v].co)
			vertex.append(mesh.vertices[v].normal)
			vertex.append(face.normal)
			
			for uv in mesh.uv_textures:
				vertex.append(uv.data[faceIndex].uv[vertexIndex])
		
			faceVertices.append(vertex)
		
		faces.append(faceVertices)
		
	return faces;
			
def write_faces_info(file, faces):
	file.write("%i\n" % len(faces))	
	numVertices = 0
	for face in faces:		
		file.write(str(len(face)))
		for vertex in face: 
			file.write(",%i" % numVertices)
			numVertices+=1
		file.write("\n")
	file.write("%i\n" % numVertices)

def write_vertex_attributes(file, faces, use_normals, use_uvs):
	attribCount = 1
	if(use_normals): attribCount += 1
	if(use_uvs):
		attribCount += len(faces[0][0])-3
	file.write("%i\n" % attribCount)
	file.write("position\n")
	if(use_normals): file.write("normal\n")
	for i in range(len(faces[0][0])-3):
		file.write("uv\n")
	
def write_vertices(file, faces, use_normals, use_face_normals, use_uvs, use_invert_uvs, use_y_up):
	for face in faces:	
		for vertex in face:		
			position = vertex[0]
			if(use_face_normals):
				normal = vertex[2]
			else:
				normal = vertex[1]
			if use_y_up:
				file.write("%f,%f,%f" % (position.y, position.z, position.x))
			else:
				file.write("%f,%f,%f" % (position.x, position.y, position.z))
			if use_normals: 
				if use_y_up:
					file.write(",%f,%f,%f" % (normal.x, normal.z, normal.y))
				else:
					file.write(",%f,%f,%f" % (normal.x, normal.y, normal.z))
			if use_uvs: 				
				for i in range(3,len(vertex)):	
					if(use_invert_uvs):
						file.write(",%f,%f" % (vertex[i][0], 1.0 - vertex[i][1]))
					else:
						file.write(",%f,%f" % (vertex[i][0], vertex[i][1]))
			file.write("\n")
		
	
def _save(filepath="c:\\gdx-workspace\\gdx\\extensions\\model-loaders\\model-loaders\\data\\test.g3dt",
		 use_selection=False,
		 use_apply_modifiers=True,
		 use_normals=True,
		 use_face_normals=True,						   
		 use_uvs=True, 
		 use_invert_uvs=False,
		 use_y_up=True,
         ):
	print("exporting G3DT Stillmodel")
	print('G3DT Stillmodel Export path: %r' % filepath)
	version = "g3dt-still-1.0"
	file = open(filepath, "w", encoding="utf8", newline="\n")
	file.write(version + "\n");
	
	# iterate through all objects
	if use_selection: 
		objects = bpy.context.selected_objects
	else:
		objects = bpy.data.objects
	
	tmp = []
	for obj in objects:
		if obj.type != 'MESH': continue
		tmp.append(obj)

	objects = tmp
	file.write(str(len(objects)) + "\n")
	
	for obj in objects:
		print("exporting mesh '" + obj.name + "'")
		mesh = obj.to_mesh(bpy.context.scene, use_apply_modifiers, 'PREVIEW')
		file.write(obj.name + "\n")
		faces = collect_faces(mesh)
		write_faces_info(file, faces)
		write_vertex_attributes(file, faces, use_normals, use_uvs)
		write_vertices(file, faces, use_normals, use_face_normals, use_uvs, use_invert_uvs, use_y_up)
		#writeVertices(file, mesh, use_normals, use_uvs, use_y_up)
	
	file.close()		
	return {'FINISHED'}
	
def save(self, context, filepath="c:\\gdx-workspace\\gdx\\extensions\\model-loaders\\model-loaders\\data\\test.g3dt",
		 use_selection=False,
		 use_apply_modifiers=True,
		 use_normals=True,
		 use_face_normals=True,						   
		 use_uvs=True, 
		 use_invert_uvs=False,
		 use_y_up=True,
         ):
	return _save(filepath, use_selection, use_apply_modifiers, use_normals, use_face_normals, use_uvs, use_invert_uvs, use_y_up)
	return {'FINISHED'}	