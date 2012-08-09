MSTRINGIFY(
	
__kernel void 
UpdateFixedVertexPositions(
	const uint numNodes,
	__global int * g_anchorIndex,
	__global float4 * g_vertexPositions,
	__global float4 * g_anchorPositions GUID_ARG)
{
	unsigned int nodeID = get_global_id(0);

	if( nodeID < numNodes )
	{		
		int anchorIndex  = g_anchorIndex[nodeID];
		float4 position = g_vertexPositions[nodeID];

		if ( anchorIndex >= 0 )
		{
			float4 anchorPosition = g_anchorPositions[anchorIndex];
			g_vertexPositions[nodeID] = anchorPosition;
		}
	}		
}

);
