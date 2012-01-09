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

#include "tokamak.h"
#include "containers.h"
#include "scenery.h"
#include "collision.h"
#include "collision2.h"
#include "constraint.h"
#include "rigidbody.h"

#include <assert.h>
#include <stdio.h>

neBool BoxTestParam::CylinderFaceTest(ConvexTestResult & res, TConvex & cylinderB, neT3 & transB, s32 whichFace)
{
	neV3 diff = trans->pos - transB.pos;

	neV3 dir = trans->rot[whichFace];

	f32 dot = dir.Dot(diff);

	if (dot > 0.0f)
	{
		dot *= -1.0f;
	}
	else
	{
		dir *= -1.0f;
	}

	f32 depth = dot + convex->BoxSize(whichFace);

	neV3 contactPoint = transB.pos;

	neV3 tmp = transB.rot[1] * cylinderB.CylinderHalfHeight();

	dot = tmp.Dot(dir);

	if (dot > 0.0f)
	{
		depth += dot;

		contactPoint += tmp;
	}
	else
	{
		depth -= dot;
		
		contactPoint -= tmp;
	}
	depth += cylinderB.CylinderRadius();

	if (depth <= 0.0f)
		return false;

	if (depth >= res.depth)
		return true;

	contactPoint += dir * cylinderB.CylinderRadius();

	neV3 project = contactPoint - dir * depth;

	s32 otherAxis1 = neNextDim1[whichFace];
	
	s32 otherAxis2 = neNextDim2[whichFace];
	
	neV3 sub = project - trans->pos;

	dot = neAbs(sub.Dot(trans->rot[otherAxis1]));

	if (dot > (convex->BoxSize(otherAxis1) * 1.001f))
		return true;// not false ???? no it is true!!! 

	dot = neAbs(sub.Dot(trans->rot[otherAxis2]));

	if (dot > (convex->BoxSize(otherAxis2) * 1.001f))
		return true;// not false ???? no it is true!!! 
	
	res.contactA = project;

	res.contactB = contactPoint;

	res.contactNormal = dir;

	res.depth = depth;

	res.valid = true;
	
	return true;
}

neBool BoxTestParam::CylinderEdgeTest(ConvexTestResult & res, TConvex & cylinderB, neT3 & transB, s32 whichEdge)
{
	neV3 diff = trans->pos - transB.pos;

	neV3 dir = trans->rot[whichEdge].Cross(transB.rot[1]);

	f32 len = dir.Length();

	if (neIsConsiderZero(len))
		return true;

	dir *= (1.0f / len);

	f32 dot = dir.Dot(diff);

	if (dot > 0.0f)
	{
		dot *= -1.0f;
	}
	else
	{
		dir *= -1.0f;
	}

	f32 depth = dot + cylinderB.CylinderRadius();

	neV3 contactPoint = trans->pos;

	s32 i;

	for (i = 0; i < 3; i++)
	{
		if (i == whichEdge)
			continue;

		dot = dir.Dot(radii[i]);

		if (dot > 0.0f)
		{
			depth += dot;

			contactPoint -= radii[i];
		}
		else
		{
			depth -= dot;

			contactPoint += radii[i];
		}
	}
	if (depth <= 0.0f)
		return false;

	ConvexTestResult cr;

	cr.edgeA[0] = contactPoint + radii[whichEdge];
	cr.edgeA[1] = contactPoint - radii[whichEdge];
	cr.edgeB[0] = transB.pos + transB.rot[1] * cylinderB.CylinderHalfHeight();
	cr.edgeB[1] = transB.pos - transB.rot[1] * cylinderB.CylinderHalfHeight();

	f32 au, bu;

	// A is the box, B is the cylinder

	cr.ComputerEdgeContactPoint2(au, bu);

	if (cr.depth >= res.depth)
		return true;

	if (cr.valid)
	{
		depth = cylinderB.CylinderRadius() - cr.depth;

		if (depth <= 0.0f)
			return false;

		if (depth >= res.depth)
			return true;;

		res.valid = true;

		res.contactNormal = dir;

		res.contactA = cr.contactA;

		res.contactB = cr.contactB + res.contactNormal * cylinderB.CylinderRadius();

		res.depth = depth;
	}
	else
	{
		// A is the box, B is the cylinder

		if (au > 0.0 && au < 1.0f)
		{
			// box edge and cylinder end

			neV3 cylinderVert;

			if (bu <= 0.0f)
			{
				cylinderVert = cr.edgeB[0];
			}
			else
			{
				cylinderVert = cr.edgeB[1];
			}
			neV3 project;

			f32 dist = cylinderVert.GetDistanceFromLine2(project, cr.edgeA[0], cr.edgeA[1]);

			f32 depth = cylinderB.CylinderRadius() - dist;

			if (depth <= 0.0f)
				return true;

			if (depth >= res.depth)
				return true;

			res.depth = depth;
			res.valid = true;
			res.contactNormal = project - cylinderVert;
			res.contactNormal.Normalize();
			res.contactA = project;
			res.contactB = cylinderVert + res.contactNormal * cylinderB.CylinderRadius();
		}
		else
		{
			neV3 boxVert;

			if (au <= 0.0f)
			{
				boxVert = cr.edgeA[0];
			}
			else // au >= 1.0f
			{
				boxVert = cr.edgeA[1];
			}
			if (bu > 0.0f && bu < 1.0f)
			{
				// boxVert and cylinder edge

				neV3 project;

				f32 depth = boxVert.GetDistanceFromLine2(project, cr.edgeB[0], cr.edgeB[1]);

				depth = cylinderB.CylinderRadius() - depth;

				if (depth <= 0.0f)
					return true;

				if (depth >= res.depth)
					return true;

				res.depth = depth;
				res.valid = true;
				res.contactA = boxVert;
				res.contactNormal = boxVert - project;
				res.contactNormal.Normalize();
				res.contactB = project + res.contactNormal * cylinderB.CylinderRadius();
			}
			else
			{
				// box vert and cylinder end

				neV3 cylinderVert;

				if (bu <= 0.0f)
				{
					cylinderVert = cr.edgeB[0];
				}
				else
				{
					cylinderVert = cr.edgeB[1];
				}
				neV3 diff = boxVert - cylinderVert;

				f32 depth = diff.Dot(diff);

				if (depth >= cylinderB.CylinderRadiusSq())
					return true;

				depth = sqrtf(depth);

				depth = cylinderB.CylinderRadius() - depth;

				if (depth >= res.depth)
					return true;

				res.depth = depth;
				res.valid = true;
				res.contactNormal = diff;
				res.contactNormal.Normalize();
				res.contactA = boxVert;
				res.contactB = cylinderVert + res.contactNormal * cylinderB.CylinderRadius();
			}
		}
	}	

	return true;
}