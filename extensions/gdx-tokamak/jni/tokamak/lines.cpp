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

void CollisionTestSensor(TConvex * obbA, neSensor_ * sensorsA, neT3 & transA, neCollision & colB, neT3 & transB, neRigidBodyBase * body)
{
	neT3 convex2WorldB;

	convex2WorldB = transB * colB.obb.c2p;
	
	neT3 world2convexB;

	world2convexB = convex2WorldB.FastInverse();

	neT3 AtoB;

	AtoB = world2convexB * transA;

	if (colB.convexCount == 1)
	{
		neSensorItem * si = (neSensorItem *)sensorsA;

		while (si)
		{
			neSensor_ * s = (neSensor_ *) si;

			si = si->next;

			neSensor_ tmp = *s;

			tmp.depth = 0.0f;
			
			tmp.pos = AtoB * s->pos;

			tmp.dir = AtoB.rot * s->dir;

			tmp.length = s->length;

			SensorTest(tmp, colB.obb, convex2WorldB);

			if (tmp.depth > 0.0f && tmp.depth > s->depth)
			{
				s->depth = tmp.depth;
				s->body = tmp.body;
				s->materialID = tmp.materialID;
				s->normal = tmp.normal;
				s->contactPoint = convex2WorldB * tmp.contactPoint;
				s->body = body;
/*
				char ss[256];

				sprintf(ss, "normal = %f, %f, %f \n", s->normal[0], s->normal[1], s->normal[2]);
				OutputDebugString(ss);
*/			}
		}
	}
	else
	{
		neSensorItem * si = (neSensorItem *)sensorsA;

		while (si)
		{
			neSensor_ * s = (neSensor_ *) si;

			si = si->next;

			neSensor_ tmp = *s;

			tmp.depth = 0.0f;

			TConvexItem * ti = (TConvexItem *)colB.convex;

			while (ti)
			{
				TConvex * t = (TConvex *)ti;

				ti = ti->next;

				convex2WorldB = transB * t->c2p;

				world2convexB = convex2WorldB.FastInverse();

				AtoB = world2convexB * transA;

				tmp.pos = AtoB * s->pos;

				tmp.dir = AtoB.rot * s->dir;

				tmp.length = s->length;

				//SensorTest(tmp, colB.obb, convex2WorldB);
				SensorTest(tmp, *t, convex2WorldB);

				if (tmp.depth > 0.0f && tmp.depth > s->depth)
				{
					s->depth = tmp.depth;
					s->body = tmp.body;
					s->materialID = tmp.materialID;
					s->normal = tmp.normal;
					s->contactPoint = convex2WorldB * tmp.contactPoint;
					s->body = body;
				}
			}
		}
	}
}

NEINLINE neBool SameSide(const neV3 & p1, const neV3 & p2, const neV3 & a, const neV3 & edge)
{
	neV3 cp1 = edge.Cross(p1 - a);
	
	neV3 cp2 = edge.Cross(p2 - a);

	f32 dot = cp1.Dot(cp2);

	return (dot >= 0.0f);
}

/*
 *	
		neBool found = false;

		f32 dist, ratio, factor, depth;

		neV3 contact;

		s32 i;

		for (i = 0; i < 3; i++)
		{
			if (!neIsConsiderZero(sensorA.dir[i]))
			{
				if (sensorA.dir[i] > 0.0f)
				{
					if (sensorA.pos[i] > -convexB.as.box.boxSize[i])
						continue;
		
					factor = 1.0f;
				}
				else
				{
					if (sensorA.pos[i] < convexB.as.box.boxSize[i])
						continue;

					factor = -1.0f;
				}
				dist = factor * (convexB.as.box.boxSize[i] - sensorA.pos[i]);

				ASSERT(dist > 0.0f);

				if (dist > neAbs(sensorA.dir[i]))
					return;

				ratio = dist / neAbs(sensorA.dir[i]);

				contact = sensorA.pos + sensorA.dir * ratio;

				s32 other1, other2;

				other1 = (i + 1)%3;
				
				other2 = (i + 2)%3;

				if (contact[other1] >= convexB.as.box.boxSize[other1] || contact[other1] <= -convexB.as.box.boxSize[other1])
					continue;

				if (contact[other2] >= convexB.as.box.boxSize[other2] || contact[other2] <= -convexB.as.box.boxSize[other2])
					continue;

				found = true;

				depth = (1.0f - ratio) * sensorA.length;

				break;
			}
			else if (sensorA.pos[i] >= convexB.as.box.boxSize[i] || sensorA.pos[i] <= -convexB.as.box.boxSize[i])
			{
				return;
			}
		}
		if (found)
		{
			sensorA.depth = depth;

			sensorA.normal = transB.rot[i] * factor * -1.0f;

			sensorA.contactPoint = contact;

			sensorA.materialID = convexB.matIndex;
		}

 */
void SensorTest(neSensor_ & sensorA, TConvex & convexB, neT3 & transB)
{
	if (convexB.type == TConvex::BOX)
	{
		int nearDim = -1;
		int farDim = -1;

//	set Tnear = - infinity, Tfar = infinity
//	For each pair of planes P associated with X, Y, and Z do:
//	(example using X planes)
//	if direction Xd = 0 then the ray is parallel to the X planes, so
//	if origin Xo is not between the slabs ( Xo < Xl or Xo > Xh) then return false
//	else, if the ray is not parallel to the plane then
//	begin
//	compute the intersection distance of the planes
//	T1 = (Xl - Xo) / Xd
//	T2 = (Xh - Xo) / Xd
//	If T1 > T2 swap (T1, T2) /* since T1 intersection with near plane */
//	If T1 > Tnear set Tnear =T1 /* want largest Tnear */
//	If T2 < Tfar set Tfar="T2" /* want smallest Tfar */
//	If Tnear > Tfar box is missed so return false
//	If Tfar < 0 box is behind ray return false end


		float tNear = -1.0e6;
		float tFar = 1.0e6;

		for (int i = 0; i < 3; i++)
		{
			if (neIsConsiderZero(sensorA.dir[i]))
			{
				if (sensorA.pos[i] < -convexB.as.box.boxSize[i] ||
					sensorA.pos[i] > convexB.as.box.boxSize[i])
				{
					return;
				}
			}
			float t1 = (-convexB.as.box.boxSize[i] - sensorA.pos[i]) / sensorA.dir[i];
			
			float t2 = (convexB.as.box.boxSize[i] - sensorA.pos[i]) / sensorA.dir[i];

			float tt;

			if (t1 > t2)
			{
				tt = t1;
				t1 = t2;
				t2 = tt;
			}

			if (t1 > tNear)
			{
				tNear = t1;
				nearDim = i;
			}

			if (t2 < tFar)
			{
				tFar = t2;
				farDim = i;
			}

			if (tNear > tFar)
				return;

			if (tFar < 0)
				return;

		}
		//assert(nearDim != -1);
		//assert(farDim != -1);

		if (tNear > 1.0f)
			return;

		neV3 contact = sensorA.pos + tNear * sensorA.dir;

		neV3 sensorEnd = sensorA.pos + sensorA.dir;

		f32 depth = (sensorEnd - contact).Length();

		sensorA.depth = depth;

		f32 factor = (sensorA.dir[nearDim] >= 0) ? -1.0f : 1.0f;
		sensorA.normal = transB.rot[nearDim] * factor;

		sensorA.contactPoint = contact;

		sensorA.materialID = convexB.matIndex;
	}
	else if (convexB.type == TConvex::TERRAIN)
	{
		neSimpleArray<s32> & _triIndex = *convexB.as.terrain.triIndex;

		s32 triangleCount = _triIndex.GetUsedCount();

		neArray<neTriangle_> & triangleArray = *convexB.as.terrain.triangles;
		
		for (s32 i = 0; i < triangleCount; i++)
		{
			s32 test = _triIndex[i];

			neTriangle_ * t = &triangleArray[_triIndex[i]];

			neV3 * vert[3];

			neV3 edges[3];

			neV3 normal;

			f32 d;

			vert[0] = &convexB.vertices[t->indices[0]];
			vert[1] = &convexB.vertices[t->indices[1]];
			vert[2] = &convexB.vertices[t->indices[2]];

			edges[0] = *vert[1] - *vert[0];
			edges[1] = *vert[2] - *vert[1];
			edges[2] = *vert[0] - *vert[2];
			normal = edges[0].Cross(edges[1]);

			normal.Normalize();

			d = normal.Dot(*vert[0]);

			f32 nd = normal.Dot(sensorA.dir);

			f32 np = normal.Dot(sensorA.pos);

			f32 t1;

			t1 = (d - np) / nd;

			if (t1 > 1.0f || t1 < 0.0f)
				continue;

			neV3 contactPoint = sensorA.pos + sensorA.dir * t1;

			if (!SameSide(contactPoint, *vert[2], *vert[0], edges[0]))
				continue;

			if (!SameSide(contactPoint, *vert[0], *vert[1], edges[1]))
				continue;

			if (!SameSide(contactPoint, *vert[1], *vert[2], edges[2]))
				continue;

			sensorA.depth = (1.0f - t1) * sensorA.length;

			if (nd > 0.0)
				sensorA.normal = normal * -1.0f;
			else
				sensorA.normal = normal;

			sensorA.contactPoint = contactPoint;

			sensorA.materialID = t->materialID;
		}
	}
	else
	{
		// other primitives to do
	}
}




















