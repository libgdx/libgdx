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

#pragma inline_recursion( on )
#pragma inline_depth( 250 )

#include "stdio.h"

/*
#ifdef _WIN32
#include <windows.h>
#endif
*/

#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "constraint.h"
#include "rigidbody.h"
#include "scenery.h"
#include "stack.h"
#include "simulator.h"
#include "message.h"


/****************************************************************************
*
*	neCollisionBody_::UpdateAABB
*
****************************************************************************/ 

void neCollisionBody_::UpdateAABB()
{
	if (col.convexCount == 0 && !isCustomCD)
		return;
/*
	neM3 c;
		
	c[0] = col.obb.as.box.boxSize[0] * col.obb.c2p.rot[0];
	c[1] = col.obb.as.box.boxSize[1] * col.obb.c2p.rot[1];
	c[2] = col.obb.as.box.boxSize[2] * col.obb.c2p.rot[2];
*/
	neT3 c2w = b2w * obb;   

	neV3 &pos = c2w.pos;

	int i;

	for (i = 0; i < 3; i++)
	{
		f32 a = neAbs(c2w.rot[0][i]) + neAbs(c2w.rot[1][i]) + neAbs(c2w.rot[2][i]);

		minBound[i] = pos[i] - a;
		maxBound[i] = pos[i] + a;

		if (minCoord[i])
			minCoord[i]->value = pos[i] - a;// - col.boundingRadius;

		if (maxCoord[i])
			maxCoord[i]->value = pos[i] + a;// + col.boundingRadius;
	}
};

void neCollisionBody_::Free()
{
	neRigidBodyBase::Free();

	RemoveConstraintHeader();
}
