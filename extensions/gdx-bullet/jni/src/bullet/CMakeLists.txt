SUBDIRS(  BulletSoftBody BulletCollision BulletDynamics LinearMath )

IF(BUILD_MULTITHREADING)
	SUBDIRS(MiniCL BulletMultiThreaded)
ENDIF()

IF(INSTALL_LIBS)
	#INSTALL of other files requires CMake 2.6
	IF (${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION} GREATER 2.5)
		IF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
			# Don't actually need to install any common files, the frameworks include everything
		ELSE (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
			INSTALL(FILES btBulletCollisionCommon.h btBulletDynamicsCommon.h Bullet-C-Api.h DESTINATION ${INCLUDE_INSTALL_DIR})
			INSTALL(FILES vectormath/vmInclude.h DESTINATION ${INCLUDE_INSTALL_DIR}/vectormath)
			INSTALL(FILES vectormath/scalar/boolInVec.h 
					vectormath/scalar/floatInVec.h 
					vectormath/scalar/mat_aos.h 
					vectormath/scalar/quat_aos.h 
					vectormath/scalar/vec_aos.h 
					vectormath/scalar/vectormath_aos.h
				DESTINATION ${INCLUDE_INSTALL_DIR}/vectormath/scalar)
			INSTALL(FILES vectormath/sse/boolInVec.h 
					vectormath/sse/floatInVec.h 
					vectormath/sse/mat_aos.h 
					vectormath/sse/quat_aos.h 
					vectormath/sse/vec_aos.h 
					vectormath/sse/vecidx_aos.h
					vectormath/sse/vectormath_aos.h
				DESTINATION ${INCLUDE_INSTALL_DIR}/vectormath/sse)
		ENDIF (APPLE AND BUILD_SHARED_LIBS AND FRAMEWORK)
	ENDIF (${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION} GREATER 2.5)
ENDIF(INSTALL_LIBS)
