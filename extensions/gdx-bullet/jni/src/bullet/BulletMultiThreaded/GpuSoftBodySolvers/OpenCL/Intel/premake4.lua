	
hasCL = findOpenCL_Intel()
	
if (hasCL) then
	
	project "BulletSoftBodySolvers_OpenCL_Intel"
		
 	defines { "USE_INTEL_OPENCL","CL_PLATFORM_INTEL"}

	initOpenCL_Intel()
	
	kind "StaticLib"
	
	targetdir "../../../../../lib"
	
	includedirs {
		".",
		"../../../..",
		"../../../../../Glut"
	}
	files {
		"../btSoftBodySolver_OpenCL.cpp",
		"../btSoftBodySolver_OpenCLSIMDAware.cpp",
		"../btSoftBodySolverOutputCLtoGL.cpp"
	}

end
