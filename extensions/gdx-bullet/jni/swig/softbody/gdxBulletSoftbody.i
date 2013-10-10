%module gdxBulletSoftbody

%{
#include <BulletSoftBody/btSoftBodySolvers.h>
%}
%include "BulletSoftBody/btSoftBodySolvers.h"

%{
#include <BulletSoftBody/btDefaultSoftBodySolver.h>
%}
%include "BulletSoftBody/btDefaultSoftBodySolver.h"

%{
#include <BulletSoftBody/btSparseSDF.h>
%}
%include "BulletSoftBody/btSparseSDF.h"

%include "./btSoftBody.i"

%{
#include <BulletSoftBody/btSoftBodyConcaveCollisionAlgorithm.h>
%}
%include "BulletSoftBody/btSoftBodyConcaveCollisionAlgorithm.h"

%{
#include <BulletSoftBody/btSoftBodyData.h>
%}
%include "BulletSoftBody/btSoftBodyData.h"

%{
#include <BulletSoftBody/btSoftBodyHelpers.h>
%}
%include "BulletSoftBody/btSoftBodyHelpers.h"

%{
#include <BulletSoftBody/btSoftBodyInternals.h>
%}
%include "BulletSoftBody/btSoftBodyInternals.h"

%{
#include <BulletSoftBody/btSoftBodyRigidBodyCollisionConfiguration.h>
%}
%include "BulletSoftBody/btSoftBodyRigidBodyCollisionConfiguration.h"

%{
#include <BulletSoftBody/btSoftBodySolverVertexBuffer.h>
%}
%include "BulletSoftBody/btSoftBodySolverVertexBuffer.h"

%{
#include <BulletSoftBody/btSoftRigidCollisionAlgorithm.h>
%}
%include "BulletSoftBody/btSoftRigidCollisionAlgorithm.h"

%{
#include <BulletSoftBody/btSoftRigidDynamicsWorld.h>
%}
%include "BulletSoftBody/btSoftRigidDynamicsWorld.h"

%{
#include <BulletSoftBody/btSoftSoftCollisionAlgorithm.h>
%}
%include "BulletSoftBody/btSoftSoftCollisionAlgorithm.h"
