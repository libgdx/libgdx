/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2007 Erwin Coumans  http://bulletphysics.com

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#include <MiniCL/cl_MiniCL_Defs.h>

#define MSTRINGIFY(A) A
#include "../OpenCLC10/ApplyForces.cl"
#include "../OpenCLC10/Integrate.cl"
#include "../OpenCLC10/PrepareLinks.cl"
#include "../OpenCLC10/SolvePositions.cl"
#include "../OpenCLC10/UpdateNodes.cl"
#include "../OpenCLC10/UpdateNormals.cl"
#include "../OpenCLC10/UpdatePositions.cl"
#include "../OpenCLC10/UpdatePositionsFromVelocities.cl"
#include "../OpenCLC10/VSolveLinks.cl"
#include "../OpenCLC10/UpdateFixedVertexPositions.cl"
//#include "../OpenCLC10/SolveCollisionsAndUpdateVelocities.cl"


MINICL_REGISTER(PrepareLinksKernel)
MINICL_REGISTER(VSolveLinksKernel)
MINICL_REGISTER(UpdatePositionsFromVelocitiesKernel)
MINICL_REGISTER(SolvePositionsFromLinksKernel)
MINICL_REGISTER(updateVelocitiesFromPositionsWithVelocitiesKernel)
MINICL_REGISTER(updateVelocitiesFromPositionsWithoutVelocitiesKernel)
MINICL_REGISTER(IntegrateKernel)
MINICL_REGISTER(ApplyForcesKernel)
MINICL_REGISTER(ResetNormalsAndAreasKernel)
MINICL_REGISTER(NormalizeNormalsAndAreasKernel)
MINICL_REGISTER(UpdateSoftBodiesKernel)
MINICL_REGISTER(UpdateFixedVertexPositions)

float mydot3a(float4 a, float4 b)
{
   return a.x*b.x + a.y*b.y + a.z*b.z;
}


typedef struct 
{
	int firstObject;
	int endObject;
} CollisionObjectIndices;

typedef struct 
{
	float4 shapeTransform[4]; // column major 4x4 matrix
	float4 linearVelocity;
	float4 angularVelocity;

	int softBodyIdentifier;
	int collisionShapeType;
	

	// Shape information
	// Compressed from the union
	float radius;
	float halfHeight;
	int upAxis;
		
	float margin;
	float friction;

	int padding0;
	
} CollisionShapeDescription;

// From btBroadphaseProxy.h
__constant int CAPSULE_SHAPE_PROXYTYPE = 10;

// Multiply column-major matrix against vector
float4 matrixVectorMul( float4 matrix[4], float4 vector )
{
	float4 returnVector;
	float4 row0 = float4(matrix[0].x, matrix[1].x, matrix[2].x, matrix[3].x);
	float4 row1 = float4(matrix[0].y, matrix[1].y, matrix[2].y, matrix[3].y);
	float4 row2 = float4(matrix[0].z, matrix[1].z, matrix[2].z, matrix[3].z);
	float4 row3 = float4(matrix[0].w, matrix[1].w, matrix[2].w, matrix[3].w);
	returnVector.x = dot(row0, vector);
	returnVector.y = dot(row1, vector);
	returnVector.z = dot(row2, vector);
	returnVector.w = dot(row3, vector);
	return returnVector;
}

__kernel void 
SolveCollisionsAndUpdateVelocitiesKernel( 
	const int numNodes,
	const float isolverdt,
	__global int *g_vertexClothIdentifier,
	__global float4 *g_vertexPreviousPositions,
	__global float * g_perClothFriction,
	__global float * g_clothDampingFactor,
	__global CollisionObjectIndices * g_perClothCollisionObjectIndices,
	__global CollisionShapeDescription * g_collisionObjectDetails,
	__global float4 * g_vertexForces,
	__global float4 *g_vertexVelocities,
	__global float4 *g_vertexPositions GUID_ARG)
{
	int nodeID = get_global_id(0);
	float4 forceOnVertex = (float4)(0.f, 0.f, 0.f, 0.f);
	
	if( get_global_id(0) < numNodes )
	{	
		int clothIdentifier = g_vertexClothIdentifier[nodeID];
		
		// Abort if this is not a valid cloth
		if( clothIdentifier < 0 )
			return;


		float4 position (g_vertexPositions[nodeID].xyz, 1.f);
		float4 previousPosition (g_vertexPreviousPositions[nodeID].xyz, 1.f);
			
		float clothFriction = g_perClothFriction[clothIdentifier];
		float dampingFactor = g_clothDampingFactor[clothIdentifier];
		float velocityCoefficient = (1.f - dampingFactor);		
		float4 difference = position - previousPosition;
		float4 velocity = difference*velocityCoefficient*isolverdt;
		
		CollisionObjectIndices collisionObjectIndices = g_perClothCollisionObjectIndices[clothIdentifier];
	
		int numObjects = collisionObjectIndices.endObject - collisionObjectIndices.firstObject;
		
		if( numObjects > 0 )
		{
			// We have some possible collisions to deal with
			for( int collision = collisionObjectIndices.firstObject; collision < collisionObjectIndices.endObject; ++collision )
			{
				CollisionShapeDescription shapeDescription = g_collisionObjectDetails[collision];
				float colliderFriction = shapeDescription.friction;

				if( shapeDescription.collisionShapeType == CAPSULE_SHAPE_PROXYTYPE )
				{
					// Colliding with a capsule

					float capsuleHalfHeight = shapeDescription.halfHeight;
					float capsuleRadius = shapeDescription.radius;
					float capsuleMargin = shapeDescription.margin;
					int capsuleupAxis = shapeDescription.upAxis;

					// Four columns of worldTransform matrix
					float4 worldTransform[4];
					worldTransform[0] = shapeDescription.shapeTransform[0];
					worldTransform[1] = shapeDescription.shapeTransform[1];
					worldTransform[2] = shapeDescription.shapeTransform[2];
					worldTransform[3] = shapeDescription.shapeTransform[3];

					// Correctly define capsule centerline vector 
					float4 c1 (0.f, 0.f, 0.f, 1.f); 
					float4 c2 (0.f, 0.f, 0.f, 1.f);
					c1.x = select( 0.f, -capsuleHalfHeight, capsuleupAxis == 0 );
					c1.y = select( 0.f, -capsuleHalfHeight, capsuleupAxis == 1 );
					c1.z = select( 0.f, -capsuleHalfHeight, capsuleupAxis == 2 );
					c2.x = -c1.x;
					c2.y = -c1.y;
					c2.z = -c1.z;


					float4 worldC1 = matrixVectorMul(worldTransform, c1);
					float4 worldC2 = matrixVectorMul(worldTransform, c2);
					float4 segment = (worldC2 - worldC1);

					// compute distance of tangent to vertex along line segment in capsule
					float distanceAlongSegment = -( mydot3a( (worldC1 - position), segment ) / mydot3a(segment, segment) );

					float4 closestPoint = (worldC1 + (segment * distanceAlongSegment));
					float distanceFromLine = length(position - closestPoint);
					float distanceFromC1 = length(worldC1 - position);
					float distanceFromC2 = length(worldC2 - position);
					
					// Final distance from collision, point to push from, direction to push in
					// for impulse force
					float dist;
					float4 normalVector;
					if( distanceAlongSegment < 0 )
					{
						dist = distanceFromC1;
						normalVector = float4(normalize(position - worldC1).xyz, 0.f);
					} else if( distanceAlongSegment > 1.f ) {
						dist = distanceFromC2;
						normalVector = float4(normalize(position - worldC2).xyz, 0.f);	
					} else {
						dist = distanceFromLine;
						normalVector = float4(normalize(position - closestPoint).xyz, 0.f);
					}
						
					float4 colliderLinearVelocity = shapeDescription.linearVelocity;
					float4 colliderAngularVelocity = shapeDescription.angularVelocity;
					float4 velocityOfSurfacePoint = colliderLinearVelocity + cross(colliderAngularVelocity, position - float4(worldTransform[0].w, worldTransform[1].w, worldTransform[2].w, 0.f));

					float minDistance = capsuleRadius + capsuleMargin;
					
					// In case of no collision, this is the value of velocity
					velocity = (position - previousPosition) * velocityCoefficient * isolverdt;
					
					
					// Check for a collision
					if( dist < minDistance )
					{
						// Project back to surface along normal
						position = position + float4(normalVector*(minDistance - dist)*0.9f);
						velocity = (position - previousPosition) * velocityCoefficient * isolverdt;
						float4 relativeVelocity = velocity - velocityOfSurfacePoint;

						float4 p1 = normalize(cross(normalVector, segment));
						float4 p2 = normalize(cross(p1, normalVector));
						// Full friction is sum of velocities in each direction of plane
						float4 frictionVector = p1*mydot3a(relativeVelocity, p1) + p2*mydot3a(relativeVelocity, p2);

						// Real friction is peak friction corrected by friction coefficients
						frictionVector = frictionVector * (colliderFriction*clothFriction);

						float approachSpeed = dot(relativeVelocity, normalVector);

						if( approachSpeed <= 0.0f )
							forceOnVertex -= frictionVector;
					}
				}
			}
		}

		g_vertexVelocities[nodeID] = float4(velocity.xyz, 0.f);	

		// Update external force
		g_vertexForces[nodeID] = float4(forceOnVertex.xyz, 0.f);

		g_vertexPositions[nodeID] = float4(position.xyz, 0.f);
	}
}


MINICL_REGISTER(SolveCollisionsAndUpdateVelocitiesKernel);




