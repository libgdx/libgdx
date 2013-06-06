INCLUDE_DIRECTORIES(
	${BULLET_PHYSICS_SOURCE_DIR}/src
	${VECTOR_MATH_INCLUDE}
)

SET(BulletMultiThreaded_SRCS
	SpuFakeDma.cpp
	SpuLibspe2Support.cpp
	btThreadSupportInterface.cpp
	Win32ThreadSupport.cpp
	PosixThreadSupport.cpp
	SequentialThreadSupport.cpp
	SpuSampleTaskProcess.cpp
	SpuCollisionObjectWrapper.cpp 
	SpuCollisionTaskProcess.cpp
	SpuGatheringCollisionDispatcher.cpp
	SpuContactManifoldCollisionAlgorithm.cpp
	btParallelConstraintSolver.cpp
	
	#SPURS_PEGatherScatterTask/SpuPEGatherScatterTask.cpp
	#SpuPEGatherScatterTaskProcess.cpp

	SpuNarrowPhaseCollisionTask/boxBoxDistance.cpp
	SpuNarrowPhaseCollisionTask/SpuContactResult.cpp
	SpuNarrowPhaseCollisionTask/SpuMinkowskiPenetrationDepthSolver.cpp
	SpuNarrowPhaseCollisionTask/SpuGatheringCollisionTask.cpp
	SpuNarrowPhaseCollisionTask/SpuCollisionShapes.cpp

	#Some GPU related stuff, mainly CUDA and perhaps OpenCL
	btGpu3DGridBroadphase.cpp
)

SET(Root_HDRS
	PlatformDefinitions.h
	PpuAddressSpace.h
	SpuFakeDma.h
	SpuDoubleBuffer.h
	SpuLibspe2Support.h
	btThreadSupportInterface.h
	Win32ThreadSupport.h
	PosixThreadSupport.h
	SequentialThreadSupport.h
	SpuSampleTaskProcess.h
	SpuCollisionObjectWrapper.cpp 
	SpuCollisionObjectWrapper.h 
	SpuCollisionTaskProcess.h
	SpuGatheringCollisionDispatcher.h
	SpuContactManifoldCollisionAlgorithm.h
	btParallelConstraintSolver.h

	#SPURS_PEGatherScatterTask/SpuPEGatherScatterTask.h
	#SpuPEGatherScatterTaskProcess.h

	#Some GPU related stuff, mainly CUDA and perhaps OpenCL
	btGpu3DGridBroadphase.h
	btGpu3DGridBroadphaseSharedCode.h
	btGpu3DGridBroadphaseSharedDefs.h
	btGpu3DGridBroadphaseSharedTypes.h
	btGpuDefines.h
	btGpuUtilsSharedCode.h
	btGpuUtilsSharedDefs.h
)

SET(SpuNarrowPhaseCollisionTask_HDRS
	SpuNarrowPhaseCollisionTask/Box.h
	SpuNarrowPhaseCollisionTask/boxBoxDistance.h
	SpuNarrowPhaseCollisionTask/SpuContactResult.h
	SpuNarrowPhaseCollisionTask/SpuMinkowskiPenetrationDepthSolver.h
	SpuNarrowPhaseCollisionTask/SpuConvexPenetrationDepthSolver.h
	SpuNarrowPhaseCollisionTask/SpuPreferredPenetrationDirections.h
	SpuNarrowPhaseCollisionTask/SpuGatheringCollisionTask.h
	SpuNarrowPhaseCollisionTask/SpuCollisionShapes.h
)

SET(BulletMultiThreaded_HDRS
	${Root_HDRS}
	${SpuNarrowPhaseCollisionTask_HDRS}
)

ADD_LIBRARY(BulletMultiThreaded ${BulletMultiThreaded_SRCS} ${BulletMultiThreaded_HDRS})
SET_TARGET_PROPERTIES(BulletMultiThreaded PROPERTIES VERSION ${BULLET_VERSION})
SET_TARGET_PROPERTIES(BulletMultiThreaded PROPERTIES SOVERSION ${BULLET_VERSION})


SUBDIRS(GpuSoftBodySolvers)


IF (BUILD_SHARED_LIBS)
	IF (UNIX)
		TARGET_LINK_LIBRARIES(BulletMultiThreaded BulletDynamics BulletCollision pthread)
	ELSE()
		TARGET_LINK_LIBRARIES(BulletMultiThreaded BulletDynamics BulletCollision)
	ENDIF()
ENDIF (BUILD_SHARED_LIBS)


IF (INSTALL_LIBS)
	IF (NOT INTERNAL_CREATE_DISTRIBUTABLE_MSVC_PROJECTFILES)
		#INSTALL of other files requires CMake 2.6
		IF (${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION} GREATER 2.5)
#			IF(INSTALL_EXTRA_LIBS)
				IF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
					INSTALL(TARGETS BulletMultiThreaded DESTINATION .)
				ELSE (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
					INSTALL(TARGETS BulletMultiThreaded DESTINATION lib${LIB_SUFFIX})
					INSTALL(DIRECTORY
${CMAKE_CURRENT_SOURCE_DIR} DESTINATION ${INCLUDE_INSTALL_DIR} FILES_MATCHING
PATTERN "*.h"  PATTERN ".svn" EXCLUDE PATTERN "CMakeFiles" EXCLUDE)
				ENDIF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
#			ENDIF (INSTALL_EXTRA_LIBS)
		ENDIF (${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION} GREATER 2.5)

		IF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
			SET_TARGET_PROPERTIES(BulletMultiThreaded PROPERTIES FRAMEWORK true)
		
			SET_TARGET_PROPERTIES(BulletMultiThreaded PROPERTIES PUBLIC_HEADER "${Root_HDRS}")
			# Have to list out sub-directories manually:
			SET_PROPERTY(SOURCE ${SpuNarrowPhaseCollisionTask_HDRS} PROPERTY MACOSX_PACKAGE_LOCATION Headers/SpuNarrowPhaseCollisionTask)
		
		ENDIF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
	ENDIF (NOT INTERNAL_CREATE_DISTRIBUTABLE_MSVC_PROJECTFILES)
ENDIF (INSTALL_LIBS)

