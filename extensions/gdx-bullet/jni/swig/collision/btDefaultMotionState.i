/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btDefaultMotionState

%{
#include <LinearMath/btDefaultMotionState.h>
%}
%include "LinearMath/btDefaultMotionState.h"

%extend btDefaultMotionState {

	void getGraphicsWorldTrans(btTransform & out) {
		out = $self->m_graphicsWorldTrans;
	}
	
	void getCenterOfMassOffset(btTransform & out) {
		out = $self->m_centerOfMassOffset;
	}
	
	void getStartWorldTrans(btTransform & out) {
		out = $self->m_startWorldTrans;
	}
};