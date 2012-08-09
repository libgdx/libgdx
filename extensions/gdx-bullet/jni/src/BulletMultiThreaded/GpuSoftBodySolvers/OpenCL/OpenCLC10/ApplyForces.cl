MSTRINGIFY(


float adot3(float4 a, float4 b)
{
   return a.x*b.x + a.y*b.y + a.z*b.z;
}

float alength3(float4 a)
{
	a.w = 0;
	return length(a);
}

float4 anormalize3(float4 a)
{
	a.w = 0;
	return normalize(a);
}

float4 projectOnAxis( float4 v, float4 a )
{
	return (a*adot3(v, a));
}

__kernel void 
ApplyForcesKernel(
	const uint numNodes,
	const float solverdt,
	const float epsilon,
	__global int * g_vertexClothIdentifier,
	__global float4 * g_vertexNormal,
	__global float * g_vertexArea,
	__global float * g_vertexInverseMass,
	__global float * g_clothLiftFactor,
	__global float * g_clothDragFactor,
	__global float4 * g_clothWindVelocity,
	__global float4 * g_clothAcceleration,
	__global float * g_clothMediumDensity,
	__global float4 * g_vertexForceAccumulator,
	__global float4 * g_vertexVelocity GUID_ARG)
{
	unsigned int nodeID = get_global_id(0);
	if( nodeID < numNodes )
	{		
		int clothId  = g_vertexClothIdentifier[nodeID];
		float nodeIM = g_vertexInverseMass[nodeID];
		
		if( nodeIM > 0.0f )
		{
			float4 nodeV  = g_vertexVelocity[nodeID];
			float4 normal = g_vertexNormal[nodeID];
			float area    = g_vertexArea[nodeID];
			float4 nodeF  = g_vertexForceAccumulator[nodeID];
			
			// Read per-cloth values
			float4 clothAcceleration = g_clothAcceleration[clothId];
			float4 clothWindVelocity = g_clothWindVelocity[clothId];
			float liftFactor = g_clothLiftFactor[clothId];
			float dragFactor = g_clothDragFactor[clothId];
			float mediumDensity = g_clothMediumDensity[clothId];
		
			// Apply the acceleration to the cloth rather than do this via a force
			nodeV += (clothAcceleration*solverdt);

			g_vertexVelocity[nodeID] = nodeV;

			// Aerodynamics
			float4 rel_v = nodeV - clothWindVelocity;
			float rel_v_len = alength3(rel_v);
			float rel_v2 = dot(rel_v, rel_v);
			
			if( rel_v2 > epsilon )
			{
				float4 rel_v_nrm = anormalize3(rel_v);
				float4 nrm = normal;
									
				nrm = nrm * (dot(nrm, rel_v) < 0 ? -1.f : 1.f);

				float4 fDrag = (float4)(0.f, 0.f, 0.f, 0.f);
				float4 fLift = (float4)(0.f, 0.f, 0.f, 0.f);

				float n_dot_v = dot(nrm, rel_v_nrm);

				// drag force
				if ( dragFactor > 0.f )
					fDrag = 0.5f * dragFactor * mediumDensity * rel_v2 * area * n_dot_v * (-1.0f) * rel_v_nrm;

				// lift force
				// Check angle of attack
				// cos(10º) = 0.98480
				if ( 0 < n_dot_v && n_dot_v < 0.98480f)
					fLift = 0.5f * liftFactor * mediumDensity * rel_v_len * area * sqrt(1.0f-n_dot_v*n_dot_v) * (cross(cross(nrm, rel_v_nrm), rel_v_nrm));
				
				nodeF += fDrag + fLift;
					g_vertexForceAccumulator[nodeID] = nodeF;	
			}
		}
	}
}

);