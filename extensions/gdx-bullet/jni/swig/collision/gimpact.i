/** @author Xoppa */
%module Gimpact

%{
#include <BulletCollision/Gimpact/btQuantization.h>
%}
%include "BulletCollision/Gimpact/btQuantization.h"

%{
#include <BulletCollision/Gimpact/btBoxCollision.h>
%}
%include "BulletCollision/Gimpact/btBoxCollision.h"

%{
#include <BulletCollision/Gimpact/btClipPolygon.h>
%}
%include "BulletCollision/Gimpact/btClipPolygon.h"

%{
#include <BulletCollision/Gimpact/btGeometryOperations.h>
%}
%include "BulletCollision/Gimpact/btGeometryOperations.h"

%{
#include <BulletCollision/Gimpact/btTriangleShapeEx.h>
%}
%include "BulletCollision/Gimpact/btTriangleShapeEx.h"

//////////////////////////////////////////////////////////////

%{
#include <BulletCollision/Gimpact/btGImpactBvhSructs.h>
%}
%include "BulletCollision/Gimpact/btGImpactBvhSructs.h"

%rename(atConst) btAlignedObjectArray< GIM_PAIR >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< GIM_PAIR >::operator [](int) const;
%template(btGimPairArray) btAlignedObjectArray<GIM_PAIR>;

%rename(atConst) btAlignedObjectArray< GIM_BVH_DATA >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< GIM_BVH_DATA >::operator [](int) const;
%template(btGimBvhDataArray) btAlignedObjectArray<GIM_BVH_DATA>;

%rename(atConst) btAlignedObjectArray< GIM_BVH_TREE_NODE >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< GIM_BVH_TREE_NODE >::operator [](int) const;
%template(btGimBvhTreeNodeArray) btAlignedObjectArray<GIM_BVH_TREE_NODE>;

%{
#include <BulletCollision/Gimpact/btGImpactBvh.h>
%}
%include "BulletCollision/Gimpact/btGImpactBvh.h"

//////////////////////////////////////////////////////////////

%{
#include <BulletCollision/Gimpact/btGImpactQuantizedBvhSructs.h>
%}
%include "BulletCollision/Gimpact/btGImpactQuantizedBvhSructs.h"

%rename(atConst) btAlignedObjectArray< BT_QUANTIZED_BVH_NODE >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< BT_QUANTIZED_BVH_NODE >::operator [](int) const;
%template(btGimQuantizedBvhNodeArray) btAlignedObjectArray<BT_QUANTIZED_BVH_NODE>;

%{
#include <BulletCollision/Gimpact/btGImpactQuantizedBvh.h>
%}
%include "BulletCollision/Gimpact/btGImpactQuantizedBvh.h"

//////////////////////////////////////////////////////////////

%rename(getChildShapeConst) btGImpactShapeInterface::getChildShape(int) const;
%rename(getChildShapeConst) btGImpactCompoundShape::getChildShape(int) const;
%rename(getChildShapeConst) btGImpactMeshShapePart::getChildShape(int) const;
%rename(getChildShapeConst) btGImpactMeshShape::getChildShape(int) const;
%rename(getMeshInterfaceConst) btGImpactMeshShape::getMeshInterface() const;
%rename(getMeshPartConst) btGImpactMeshShape::getMeshPart(int) const;
%{
#include <BulletCollision/Gimpact/btGImpactShape.h>
%}
%include "BulletCollision/Gimpact/btGImpactShape.h"

//////////////////////////////////////////////////////////////

%{
#include <BulletCollision/Gimpact/btContactProcessingSructs.h>
%}
%include "BulletCollision/Gimpact/btContactProcessingSructs.h"

%rename(atConst) btAlignedObjectArray< GIM_CONTACT >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< GIM_CONTACT >::operator [](int) const;
%template(btGimContactArray) btAlignedObjectArray<GIM_CONTACT>;

%{
#include <BulletCollision/Gimpact/btContactProcessing.h>
%}
%include "BulletCollision/Gimpact/btContactProcessing.h"

//////////////////////////////////////////////////////////////

%{
#include <BulletCollision/Gimpact/btCompoundFromGimpact.h>
%}
%include "BulletCollision/Gimpact/btCompoundFromGimpact.h"

%{
#include <BulletCollision/Gimpact/btGenericPoolAllocator.h>
%}
%include "BulletCollision/Gimpact/btGenericPoolAllocator.h"

%{
#include <BulletCollision/Gimpact/btGImpactCollisionAlgorithm.h>
%}
%include "BulletCollision/Gimpact/btGImpactCollisionAlgorithm.h"

%{
#include <BulletCollision/Gimpact/btGImpactMassUtil.h>
%}
%include "BulletCollision/Gimpact/btGImpactMassUtil.h"

/////////////////////////////////////////////////////////////////

%rename(sizeVal) gim_array::size;
%{
#include <BulletCollision/Gimpact/gim_array.h>
%}
%include "BulletCollision/Gimpact/gim_array.h"

%{
#include <BulletCollision/Gimpact/gim_memory.h>
%}
%include "BulletCollision/Gimpact/gim_memory.h"

%{
#include <BulletCollision/Gimpact/gim_basic_geometry_operations.h>
%}
%include "BulletCollision/Gimpact/gim_basic_geometry_operations.h"

%{
#include <BulletCollision/Gimpact/gim_bitset.h>
%}
%include "BulletCollision/Gimpact/gim_bitset.h"

%{
#include <BulletCollision/Gimpact/gim_box_collision.h>
%}
%include "BulletCollision/Gimpact/gim_box_collision.h"

%{
#include <BulletCollision/Gimpact/gim_box_set.h>
%}
%include "BulletCollision/Gimpact/gim_box_set.h"

%{
#include <BulletCollision/Gimpact/gim_clip_polygon.h>
%}
%include "BulletCollision/Gimpact/gim_clip_polygon.h"

%rename(pointer_const) gim_array< GIM_CONTACT >::pointer() const;
%rename(operatorSubscriptConst) gim_array< GIM_CONTACT >::operator [](size_t) const;
%rename(get_pointer_at_const) gim_array< GIM_CONTACT >::get_pointer_at(GUINT) const;
%rename(front_const) gim_array< GIM_CONTACT >::front() const;
%rename(back_const) gim_array< GIM_CONTACT >::back() const;
%rename(at_const) gim_array< GIM_CONTACT >::at(GUINT) const;
%template(gim_contact_array_internal) gim_array<GIM_CONTACT>;

%{
#include <BulletCollision/Gimpact/gim_contact.h>
%}
%include "BulletCollision/Gimpact/gim_contact.h"

%{
#include <BulletCollision/Gimpact/gim_geom_types.h>
%}
%include "BulletCollision/Gimpact/gim_geom_types.h"

%{
#include <BulletCollision/Gimpact/gim_geometry.h>
%}
%include "BulletCollision/Gimpact/gim_geometry.h"

%{
#include <BulletCollision/Gimpact/gim_hash_table.h>
%}
%include "BulletCollision/Gimpact/gim_hash_table.h"

%{
#include <BulletCollision/Gimpact/gim_linear_math.h>
%}
%include "BulletCollision/Gimpact/gim_linear_math.h"

%{
#include <BulletCollision/Gimpact/gim_math.h>
%}
%include "BulletCollision/Gimpact/gim_math.h"

%{
#include <BulletCollision/Gimpact/gim_radixsort.h>
%}
%include "BulletCollision/Gimpact/gim_radixsort.h"

%{
#include <BulletCollision/Gimpact/gim_tri_collision.h>
%}
%include "BulletCollision/Gimpact/gim_tri_collision.h"
