%module btCollisionObjectWrapper

CREATE_POOLED_OBJECT(btCollisionObjectWrapper, com/badlogic/gdx/physics/bullet/btCollisionObjectWrapper);

%nodefaultdtor btCollisionObjectWrapper;

%{
#include <BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"