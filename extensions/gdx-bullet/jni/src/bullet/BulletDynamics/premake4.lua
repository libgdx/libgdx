	project "BulletDynamics"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"..",
	}
	files {
		"**.cpp",
		"**.h"
	}