// NOTE: In BulletDynamics/Vehicle/btRaycastVehicle.h on line 22 the global class btVehicleTuning must be removed,
// It is never used by bullet (btVehicleTuning is a subclass of btRaycastVehicle),
// but it conflicts with swig because swig can't handle nested structs.

%module btRaycastVehicle

class btVehicleTuning
{
public:
	btVehicleTuning()
		:m_suspensionStiffness(btScalar(5.88)),
		m_suspensionCompression(btScalar(0.83)),
		m_suspensionDamping(btScalar(0.88)),
		m_maxSuspensionTravelCm(btScalar(500.)),
		m_frictionSlip(btScalar(10.5)),
		m_maxSuspensionForce(btScalar(6000.))
	{
	}
	
	btScalar	m_suspensionStiffness;
	btScalar	m_suspensionCompression;
	btScalar	m_suspensionDamping;
	btScalar	m_maxSuspensionTravelCm;
	btScalar	m_frictionSlip;
	btScalar	m_maxSuspensionForce;
};

%nestedworkaround btRaycastVehicle::btVehicleTuning;

%{
#include <BulletDynamics/Vehicle/btRaycastVehicle.h>
typedef btRaycastVehicle::btVehicleTuning btVehicleTuning; // FIXME This should be done by nestedworkaround
%}
%include "BulletDynamics/Vehicle/btRaycastVehicle.h"
