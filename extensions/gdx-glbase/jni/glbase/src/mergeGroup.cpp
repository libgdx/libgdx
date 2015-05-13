/**
 * @file mergeGroup.cpp
 * @brief Merge group implementation
 **/

#include "mergeGroup.h"
#include "layer.h"
#include "glbase.h"
#include "texture.h"

// Triangle indices for quad
const unsigned short MergeGroup::quad[6] = { 0, 1, 2, 0, 2, 3 };

MergeGroup::MergeGroup() : Object(), IMergeGroup()
{
}

MergeGroup::~MergeGroup()
{
}

void MergeGroup::createEmptyGroup(Object* base, int maxVertices, int polygonMaps)
{
  // Create vertex buffers
  Layer* layer = (Layer*)layers.get(0);
  if( base == NULL ){
    layer->createMergeBuffers(NULL, maxVertices, polygonMaps);
  } else {
    layer->createMergeBuffers((Layer*)base->getLayers()->get(0), maxVertices, polygonMaps);
  }
}

void MergeGroup::startGroup()
{
  Layer* layer = (Layer*)layers.get(0);
  layer->startGroup();
}

void MergeGroup::endGroup()
{
  Layer* layer = (Layer*)layers.get(0);
  layer->endGroup();
}

void MergeGroup::addPolygonMap(IPolygonMap* polygonMap, int targetPmId, 
			       Matrix* matrix)
{
  Layer* layer = (Layer*)layers.get(0);
  layer->addPolygonMap((PolygonMap*)polygonMap, targetPmId, matrix);
}

void MergeGroup::addPolygonMap(IPolygonMap* polygonMap, int targetPmId, 
			       float x, float y, float z)
{
  Layer* layer = (Layer*)layers.get(0);
  layer->addPolygonMap((PolygonMap*)polygonMap, targetPmId, x, y, z);
}

void MergeGroup::addObject(IObject* object, int targetPmId, Matrix* transform)
{
    Layer* l;
  ArrayList* list = ((Object*)object)->getLayers();
  foreach_element(list, l, Layer*){
    PolygonMap* pm;
    foreach_element(l->getPolygonMaps(), pm, PolygonMap*){
      addPolygonMap(pm, targetPmId, transform);
    }
  }
}

void MergeGroup::addObject(IObject* object, int targetPmId, 
			   float x, float y, float z)
{
  Layer* l;
  ArrayList* list = ((Object*)object)->getLayers();
  foreach_element(list, l, Layer*){
    PolygonMap* pm;
    foreach_element(l->getPolygonMaps(), pm, PolygonMap*){
      addPolygonMap(pm, targetPmId, x, y, z);
    }
  }
}

void MergeGroup::addTriangles(int vertices, int triangles, const float* points, 
		    const unsigned short* indices, const float* uvs) {
  Layer* layer = (Layer*)layers.get(0);
  layer->addTriangles(vertices, triangles, points, indices, uvs);
}

void MergeGroup::addSprite(float x, float y, float z, int textureId)
{
  Texture* texture = GLBase::get()->getTextureTable()->getTexture(textureId);
  if( texture == NULL ){
    GLBase::get()->doetrace("texture %d is not loaded", textureId);
  }

  addSprite(x, y, z, texture->getWidth(), texture->getHeight(), textureId,
	    0, 0, texture->getWidth(), texture->getHeight());
}

void MergeGroup::addSprite(float x, float y, float z, float w, float h, int textureId,
			   float sx, float sy, float sw, float sh, bool flipV)
{
  Texture* txt = GLBase::get()->getTextureTable()->getTexture(textureId);
  if( txt == NULL ){
    GLBase::get()->doetrace("texture %d is not loaded", textureId);
  }

  float u = sx/txt->getWidth();
  float v = sy/txt->getHeight();
  float u2 = (sx+sw)/txt->getWidth();
  float v2 = (sy+sh)/txt->getHeight();
  if( flipV ){
    v = 1.0f - v;
    v2 = 1.0f - v2;
  }
  addSpriteUV(x, y, z, w, h, 
	      u, v, u2, v2);
}

void MergeGroup::addSprite(int texture, float sx, float sy, float sw, float sh, 
			   Matrix* transform, bool flipV)
{
  Texture* txt = GLBase::get()->getTextureTable()->getTexture(texture);

  float points[] = { -0.5f, -0.5f, 0,
		     0.5f, -0.5f, 0,
		     0.5f, 0.5f, 0,
		     -0.5f, 0.5f, 0 };

  float u = sx/txt->getWidth();
  float v = sy/txt->getHeight();
  float u2 = (sx+sw)/txt->getWidth();
  float v2 = (sy+sh)/txt->getHeight();
  if( flipV ){
    v = 1.0f - v;
    v2 = 1.0f - v2;
  }

  float uvs[] = { u, v, 
		  u2, v,
		  u2, v2, 
		  u, v2 };

  // Transform vertices
  transform->transform3(points);
  transform->transform3(points+3);
  transform->transform3(points+6);
  transform->transform3(points+9);

  Layer* layer = (Layer*)layers.get(0);
  layer->addTriangles(4, 2, points, quad, uvs);
}

void MergeGroup::addSpriteUV(float x, float y, float z, float w, float h, 
		 float u, float v, float u2, float v2)
{
  float points[] = { x, y, z,
		     x+w, y, z,
		     x+w, y+h, z,
		     x, y+h, z };

  float uvs[] = { u, v, 
		  u2, v,
		  u2, v2, 
		  u, v2 };

  Layer* layer = (Layer*)layers.get(0);
  layer->addTriangles(4, 2, points, quad, uvs);
}

void MergeGroup::addQuad(SimpleVertex *vertex1, SimpleVertex *vertex2, SimpleVertex *vertex3, SimpleVertex *vertex4, float z) {
  float points[] = {
    vertex1->x, vertex1->y, z,
    vertex2->x, vertex2->y, z,
    vertex3->x, vertex3->y, z,
    vertex4->x, vertex4->y, z,
  };

  float uvs[] = {
    vertex1->u, vertex1->v,
    vertex2->u, vertex2->v,
    vertex3->u, vertex3->v,
    vertex4->u, vertex4->v,
  };

  Layer* layer = (Layer*)layers.get(0);

  layer->addTriangles(4, 2, points, quad, uvs);
}
