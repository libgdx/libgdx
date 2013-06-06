	
hasCL = findOpenCL_AMD()
	
if (hasCL) then
	
	project "BulletSoftBodySolvers_OpenCL_AMD"
		
 	defines { "USE_AMD_OPENCL","CL_PLATFORM_AMD"}

	initOpenCL_AMD()
	
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
