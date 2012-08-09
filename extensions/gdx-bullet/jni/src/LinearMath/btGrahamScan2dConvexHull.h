/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2011 Advanced Micro Devices, Inc.  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/


#ifndef GRAHAM_SCAN_2D_CONVEX_HULL_H
#define GRAHAM_SCAN_2D_CONVEX_HULL_H


#include "btVector3.h"
#include "btAlignedObjectArray.h"

struct GrahamVector2 : public btVector3
{
	GrahamVector2(const btVector3& org, int orgIndex)
		:btVector3(org),
			m_orgIndex(orgIndex)
	{
	}
	btScalar	m_angle;
	int m_orgIndex;
};


struct btAngleCompareFunc {
	btVector3 m_anchor;
	btAngleCompareFunc(const btVector3& anchor)
	: m_anchor(anchor) 
	{
	}
	bool operator()(const GrahamVector2& a, const GrahamVector2& b) const {
		if (a.m_angle != b.m_angle)
			return a.m_angle < b.m_angle;
		else
		{
			btScalar al = (a-m_anchor).length2();
			btScalar bl = (b-m_anchor).length2();
			if (al != bl)
				return  al < bl;
			else
			{
				return a.m_orgIndex < b.m_orgIndex;
			}
		}
	}
};

inline void GrahamScanConvexHull2D(btAlignedObjectArray<GrahamVector2>& originalPoints, btAlignedObjectArray<GrahamVector2>& hull)
{
	if (originalPoints.size()<=1)
	{
		for (int i=0;i<originalPoints.size();i++)
			hull.push_back(originalPoints[0]);
		return;
	}
	//step1 : find anchor point with smallest x/y and move it to first location
	//also precompute angles
	for (int i=0;i<originalPoints.size();i++)
	{
		const btVector3& left = originalPoints[i];
		const btVector3& right = originalPoints[0];
		if (left.x() < right.x() || 
            (!(right.x() < left.x()) && left.y() < right.y()))
		{
			originalPoints.swap(0,i);
		}
	}

	for (int i=0;i<originalPoints.size();i++)
	{
		btVector3 xvec(1,0,0);
		btVector3 ar = originalPoints[i]-originalPoints[0];
		originalPoints[i].m_angle = btCross(xvec, ar).dot(btVector3(0,0,1)) / ar.length();
	}

	//step 2: sort all points, based on 'angle' with this anchor
	btAngleCompareFunc comp(originalPoints[0]);
	originalPoints.quickSortInternal(comp,1,originalPoints.size()-1);

	int i;
	for (i = 0; i<2; i++) 
		hull.push_back(originalPoints[i]);

	//step 3: keep all 'convex' points and discard concave points (using back tracking)
	for (; i != originalPoints.size(); i++) 
	{
		bool isConvex = false;
		while (!isConvex&& hull.size()>1) {
			btVector3& a = hull[hull.size()-2];
			btVector3& b = hull[hull.size()-1];
			isConvex = btCross(a-b,a-originalPoints[i]).dot(btVector3(0,0,1))> 0;
			if (!isConvex)
				hull.pop_back();
			else 
				hull.push_back(originalPoints[i]);
		}
	}
}

#endif //GRAHAM_SCAN_2D_CONVEX_HULL_H
