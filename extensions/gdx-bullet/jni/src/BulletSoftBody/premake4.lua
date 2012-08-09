	project "BulletSoftBody"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"..",
	}
	files {
		"**.cpp",
		"**.h"
	}