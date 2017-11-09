/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2006 Erwin Coumans  http://continuousphysics.com/Bullet/

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#ifndef BT_CONTACT_SOLVER_INFO
#define BT_CONTACT_SOLVER_INFO

#include "LinearMath/btScalar.h"

enum	btSolverMode
{
	SOLVER_RANDMIZE_ORDER = 1,
	SOLVER_FRICTION_SEPARATE = 2,
	SOLVER_USE_WARMSTARTING = 4,
	SOLVER_USE_2_FRICTION_DIRECTIONS = 16,
	SOLVER_ENABLE_FRICTION_DIRECTION_CACHING = 32,
	SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION = 64,
	SOLVER_CACHE_FRIENDLY = 128,
	SOLVER_SIMD = 256,
	SOLVER_INTERLEAVE_CONTACT_AND_FRICTION_CONSTRAINTS = 512,
	SOLVER_ALLOW_ZERO_LENGTH_FRICTION_DIRECTIONS = 1024
};

struct btContactSolverInfoData
{
	

	btScalar	m_tau;
	btScalar	m_damping;//global non-contact constraint damping, can be locally overridden by constraints during 'getInfo2'.
	btScalar	m_friction;
	btScalar	m_timeStep;
	btScalar	m_restitution;
	int		m_numIterations;
	btScalar	m_maxErrorReduction;
	btScalar	m_sor;
	btScalar	m_erp;//used as Baumgarte factor
	btScalar	m_erp2;//used in Split Impulse
	btScalar	m_globalCfm;//constraint force mixing
	int			m_splitImpulse;
	btScalar	m_splitImpulsePenetrationThreshold;
	btScalar	m_splitImpulseTurnErp;
	btScalar	m_linearSlop;
	btScalar	m_warmstartingFactor;

	int			m_solverMode;
	int	m_restingContactRestitutionThreshold;
	int			m_minimumSolverBatchSize;
	btScalar	m_maxGyroscopicForce;
	btScalar	m_singleAxisRollingFrictionThreshold;
	btScalar	m_leastSquaresResidualThreshold;

};

struct btContactSolverInfo : public btContactSolverInfoData
{

	

	inline btContactSolverInfo()
	{
		m_tau = btScalar(0.6);
		m_damping = btScalar(1.0);
		m_friction = btScalar(0.3);
		m_timeStep = btScalar(1.f/60.f);
		m_restitution = btScalar(0.);
		m_maxErrorReduction = btScalar(20.);
		m_numIterations = 10;
		m_erp = btScalar(0.2);
		m_erp2 = btScalar(0.2);
		m_globalCfm = btScalar(0.);
		m_sor = btScalar(1.);
		m_splitImpulse = true;
		m_splitImpulsePenetrationThreshold = -.04f;
		m_splitImpulseTurnErp = 0.1f;
		m_linearSlop = btScalar(0.0);
		m_warmstartingFactor=btScalar(0.85);
		//m_solverMode =  SOLVER_USE_WARMSTARTING |  SOLVER_SIMD | SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION|SOLVER_USE_2_FRICTION_DIRECTIONS|SOLVER_ENABLE_FRICTION_DIRECTION_CACHING;// | SOLVER_RANDMIZE_ORDER;
		m_solverMode = SOLVER_USE_WARMSTARTING | SOLVER_SIMD;// | SOLVER_RANDMIZE_ORDER;
		m_restingContactRestitutionThreshold = 2;//unused as of 2.81
		m_minimumSolverBatchSize = 128; //try to combine islands until the amount of constraints reaches this limit
		m_maxGyroscopicForce = 100.f; ///it is only used for 'explicit' version of gyroscopic force
		m_singleAxisRollingFrictionThreshold = 1e30f;///if the velocity is above this threshold, it will use a single constraint row (axis), otherwise 3 rows.
		m_leastSquaresResidualThreshold = 0.f;
	}
};

///do not change those serialization structures, it requires an updated sBulletDNAstr/sBulletDNAstr64
struct btContactSolverInfoDoubleData
{
	double		m_tau;
	double		m_damping;//global non-contact constraint damping, can be locally overridden by constraints during 'getInfo2'.
	double		m_friction;
	double		m_timeStep;
	double		m_restitution;
	double		m_maxErrorReduction;
	double		m_sor;
	double		m_erp;//used as Baumgarte factor
	double		m_erp2;//used in Split Impulse
	double		m_globalCfm;//constraint force mixing
	double		m_splitImpulsePenetrationThreshold;
	double		m_splitImpulseTurnErp;
	double		m_linearSlop;
	double		m_warmstartingFactor;
	double		m_maxGyroscopicForce;///it is only used for 'explicit' version of gyroscopic force
	double		m_singleAxisRollingFrictionThreshold;

	int			m_numIterations;
	int			m_solverMode;
	int			m_restingContactRestitutionThreshold;
	int			m_minimumSolverBatchSize;
	int			m_splitImpulse;
	char		m_padding[4];

};
///do not change those serialization structures, it requires an updated sBulletDNAstr/sBulletDNAstr64
struct btContactSolverInfoFloatData
{
	float		m_tau;
	float		m_damping;//global non-contact constraint damping, can be locally overridden by constraints during 'getInfo2'.
	float		m_friction;
	float		m_timeStep;

	float		m_restitution;
	float		m_maxErrorReduction;
	float		m_sor;
	float		m_erp;//used as Baumgarte factor

	float		m_erp2;//used in Split Impulse
	float		m_globalCfm;//constraint force mixing
	float		m_splitImpulsePenetrationThreshold;
	float		m_splitImpulseTurnErp;

	float		m_linearSlop;
	float		m_warmstartingFactor;
	float		m_maxGyroscopicForce;
	float		m_singleAxisRollingFrictionThreshold;

	int			m_numIterations;
	int			m_solverMode;
	int			m_restingContactRestitutionThreshold;
	int			m_minimumSolverBatchSize;

	int			m_splitImpulse;
	char		m_padding[4];
};



#endif //BT_CONTACT_SOLVER_INFO
