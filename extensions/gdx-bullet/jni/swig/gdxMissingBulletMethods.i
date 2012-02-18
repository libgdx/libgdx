/*
 * Bullet declares some methods that are not implemented (but never called).
 * The Android NDK linker will error unless it can find them, so here are
 * some dummy implementations. 
 */
 
%{

// Begin dummy implementations for missing Bullet methods

void CProfileIterator::Enter_Largest_Child()
{
}

void btMultiSapBroadphase::quicksort(btBroadphasePairArray& a, int lo, int hi)
{
}

bool btGeometryUtil::isInside(btAlignedObjectArray<btVector3> const&, btVector3 const&, float)
{
	return false;
}

// End dummy implementations for missing Bullet methods

%}