	project "BulletFileLoader"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"../../../src"
	}
	 
	files {
		"**.cpp",
		"**.h"
	}