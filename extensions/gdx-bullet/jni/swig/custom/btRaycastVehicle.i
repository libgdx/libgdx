/*
 *	Interface module for a class with inner structs or classes.
 */
 
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
%}
%include "BulletDynamics/Vehicle/btRaycastVehicle.h"

%{
typedef btRaycastVehicle::btVehicleTuning btVehicleTuning;
%}
