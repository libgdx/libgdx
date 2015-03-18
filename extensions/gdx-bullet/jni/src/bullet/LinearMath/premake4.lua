	project "LinearMath"
		
	kind "StaticLib"
	targetdir "../../lib"
	includedirs {
		"..",
	}
	files {
		"**.cpp",
		"**.h"
	}