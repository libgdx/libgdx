	project "BulletFileLoader"
		
	kind "StaticLib"
	
	includedirs {
		"../../../src"
	}
	 
	files {
		"**.cpp",
		"**.h"
	}