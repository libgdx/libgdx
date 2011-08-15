if "bpy" in locals():
	import imp	
	if "export_still" in locals():
		imp.reload(export_still)
		
import bpy
from bpy.props import BoolProperty, FloatProperty, StringProperty, EnumProperty
import bpy_extras.io_utils
from bpy_extras.io_utils import ExportHelper, ImportHelper
from . import *


bl_info = {
	"name": "G3DT Exporter",
	"description": "Exports Meshes to the G3DT format.",
	"author": "Mario Zechner",
	"version": (1, 0),
	"blender": (2, 5, 3),
	"api": 31236,
	"location": "File > Export",
	"warning": '',
	"category": "Import-Export"
}

class ExportG3DTStill(bpy.types.Operator, ExportHelper):
	'''Save a G3DT Stillmodel File'''

	bl_idname = "export.g3dt"
	bl_label = 'Export G3DT Stillmodel'
	bl_options = {'PRESET'}

	filename_ext = ".g3dt"
	filter_glob = StringProperty(default="*.g3dt", options={'HIDDEN'})

	# List of operator properties, the attributes will be assigned
	# to the class instance from the operator settings before calling.

	# context group
	use_selection = BoolProperty(name="Selection Only", description="Export selected objects only", default=False)	   

	# extra data group
	use_apply_modifiers = BoolProperty(name="Apply Modifiers", description="", default=False)
	use_normals = BoolProperty(name="Export Normals", description="", default=True)
	use_face_normals = BoolProperty(name="Use Face Normals", description="", default=True)
	use_uvs = BoolProperty(name="Export UVs", description="", default=True)
	use_invert_uvs = BoolProperty(name="Invert V", description="", default=False)
	use_y_up = BoolProperty(name="OpenGL Coords", description="", default=True)

	def execute(self, context):	   
		import io_scene_g3dt
		import io_scene_g3dt.export_still
		import imp
		imp.reload(io_scene_g3dt)
		imp.reload(io_scene_g3dt.export_still)
		return io_scene_g3dt.export_still.save(self, context, **self.as_keywords(ignore=("check_existing", "filter_glob")))

def menu_func_export(self, context):
	self.layout.operator(ExportG3DTStill.bl_idname, text="G3DT Stillmodel (.g3dt)")


def register():
	bpy.utils.register_module(__name__)   
	bpy.types.INFO_MT_file_export.append(menu_func_export)


def unregister():
	bpy.utils.unregister_module(__name__)	
	bpy.types.INFO_MT_file_export.remove(menu_func_export)
	
if __name__ == "__main__":
	register()