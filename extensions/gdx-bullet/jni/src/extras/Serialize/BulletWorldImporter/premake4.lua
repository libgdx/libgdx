	project "BulletWorldImporter"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"../BulletFileLoader",
		"../../../src"
	}
	 
	files {
		"**.cpp",
		"**.h"
	}