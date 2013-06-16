	project "BulletXmlWorldImporter"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"../BulletWorldImporter",
		"../BulletFileLoader",
		"../../../src"
	}
	 
	files {
		"**.cpp",
		"**.h"
	}