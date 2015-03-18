	project "BulletCollision"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"..",
	}
	files {
		"**.cpp",
		"**.h"
	}