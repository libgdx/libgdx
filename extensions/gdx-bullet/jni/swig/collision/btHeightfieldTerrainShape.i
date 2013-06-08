%{
#include <BulletCollision/CollisionShapes/btHeightfieldTerrainShape.h>
%}

%ignore btHeightfieldTerrainShape::btHeightfieldTerrainShape(int heightStickWidth,int heightStickLength, const void* heightfieldData, btScalar heightScale, btScalar minHeight, btScalar maxHeight, int upAxis, PHY_ScalarType heightDataType, bool flipQuadEdges);
%ignore btHeightfieldTerrainShape::btHeightfieldTerrainShape(int heightStickWidth,int heightStickLength,const void* heightfieldData, btScalar maxHeight,int upAxis,bool useFloatData,bool flipQuadEdges);

%extend btHeightfieldTerrainShape {
	btHeightfieldTerrainShape(int heightStickWidth, int heightStickLength, const float * heightfieldData, btScalar heightScale, btScalar minHeight, btScalar maxHeight, int upAxis, bool flipQuadEdges)
	{
		return new btHeightfieldTerrainShape(heightStickWidth, heightStickLength, (void *)heightfieldData, heightScale, minHeight, maxHeight, upAxis, PHY_FLOAT, flipQuadEdges);
	}
	
	btHeightfieldTerrainShape(int heightStickWidth, int heightStickLength, const short * heightfieldData, btScalar heightScale, btScalar minHeight, btScalar maxHeight, int upAxis, bool flipQuadEdges)
	{
		return new btHeightfieldTerrainShape(heightStickWidth, heightStickLength, (void *)heightfieldData, heightScale, minHeight, maxHeight, upAxis, PHY_SHORT, flipQuadEdges);
	}
};

%include "BulletCollision/CollisionShapes/btHeightfieldTerrainShape.h"