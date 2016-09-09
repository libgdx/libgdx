	project "BulletWorldImporter"
		
	kind "StaticLib"
	
	includedirs {
		"../BulletFileLoader",
		"../../../src"
	}
	 
	files {
		"**.cpp",
		"**.h"
	}