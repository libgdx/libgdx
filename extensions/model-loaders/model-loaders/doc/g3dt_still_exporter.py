import bpy

bl_info = {
	"name": "G3DT Stillmodel Exporter",
	"description": "Exports Meshes to the G3DT format.",
	"author": "Mario Zechner",
	"version": (1, 0),
	"blender": (2, 5, 3),
	"api": 31236,
	"location": "File > Export > G3DT Stillmodel (.g3dt)",
	"warning": '',
	"category": "Import-Export"
}

def register():
	print("registered G3DT Stillmodel exporter");

def unregister():
	print("unregistered G3DT Stillmodel exporter");