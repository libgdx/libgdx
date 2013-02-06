/*************************************************************************
 *                                                                       *
 * Tokamak Physics Engine, Copyright (C) 2002-2007 David Lam.            *
 * All rights reserved.  Email: david@tokamakphysics.com                 *
 *                       Web: www.tokamakphysics.com                     *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT for more details.                                         *
 *                                                                       *
 *************************************************************************/

#ifndef NE_MESSAGE_H
#define NE_MESSAGE_H

#define MSG_MEMORY_ALLOC_FAILED		"Failed to allocate memory.\n"
#define MSG_RUN_OUT_GEOMETRY		"Run out of Geometries. Increase 'geometriesCount'.\n"
#define MSG_RUN_OUT_SENSOR			"Run out of Sensors. Increase 'sensorsCount'.\n"
#define MSG_RUN_OUT_RIDIGBODY		"Run out of RigidBodies. Increase 'rigidBodiesCount'.\n"
#define MSG_RUN_OUT_RIDIGPARTICLE	"Run out of RigidBodies. Increase 'rigidParticleCount'.\n"
#define MSG_RUN_OUT_ANIMATEDBODY	"Run out of AnimatedBodies. Increase 'animatedBodiesCount'.\n"

#define MSG_CONTROLLER_FULL			"Run out of Controllers. Increase 'constraintSetsCount'.\n"
#define MSG_CONSTRAINT_FULL			"Run out of Constraints. Increase 'constraintsCount'.\n"
#define MSG_CONSTRAINT_HEADER_FULL	"Run out of Constraint Sets. Increase 'constraintSetsCount'.\n"
#define MSG_CONSTRAINT_BUFFER_FULL	"Run out of Constraint Buffer. Cannot solve all constraints. Increase constraintBufferSize.\n"
#define MSG_TOO_MANY_CONSTRAINT		"Rigid Body contain too many constraints.\n"
#define MSG_STACK_BUFFER_FULL		"Stacking Buffer full Error. Please contact techincal support\n"

#define MSG_TRYING_TO_FREE_INVALID_RB	"Trying to Free invalid RigidBody.\n"
#define MSG_TRYING_TO_FREE_INVALID_RP	"Trying to Free invalid RigidParticle.\n"
#define MSG_TRYING_TO_FREE_INVALID_CB	"Trying to Free invalid AnimatedBody.\n"

#endif
