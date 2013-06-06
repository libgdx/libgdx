	
hasCL = findOpenCL_NVIDIA()
	
if (hasCL) then
	
	project "BulletSoftBodySolvers_OpenCL_NVIDIA"
		
 	defines { "USE_NVIDIA_OPENCL","CL_PLATFORM_NVIDIA"}

	initOpenCL_NVIDIA()
	
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
