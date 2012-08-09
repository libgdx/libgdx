	
hasDX11 = findDirectX11()
	
if (hasDX11) then
	
	project "BulletSoftBodyDX11Solvers"
		
  initDirectX11()
	
	kind "StaticLib"
	
	targetdir "../../../../lib"
	
	includedirs {
		".",
		"../../.."
	}
	files {
		"**.cpp",
		"**.h"
	}

end
