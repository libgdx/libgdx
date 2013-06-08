//
// This module contains a bunch of well understood functions
// I apologise if the conventions used here are slightly
// different than what you are used to.
// 
 
#ifndef GENERIC_VECTOR_H
#define GENERIC_VECTOR_H

#include <stdio.h>
#include <math.h>


class Vector {
  public:
	float x,y,z;
	Vector(float _x=0.0,float _y=0.0,float _z=0.0){x=_x;y=_y;z=_z;};
	operator float *() { return &x;};
};

float magnitude(Vector v);
Vector normalize(Vector v);

Vector operator+(Vector v1,Vector v2);
Vector operator-(Vector v);
Vector operator-(Vector v1,Vector v2);
Vector operator*(Vector v1,float s)   ;
Vector operator*(float s,Vector v1)   ;
Vector operator/(Vector v1,float s)   ;
float   operator^(Vector v1,Vector v2);  // DOT product
Vector operator*(Vector v1,Vector v2);   // CROSS product
Vector planelineintersection(Vector n,float d,Vector p1,Vector p2);

class matrix{
 public:
	Vector x,y,z;
	matrix(){x=Vector(1.0f,0.0f,0.0f);
	         y=Vector(0.0f,1.0f,0.0f);
	         z=Vector(0.0f,0.0f,1.0f);};
	matrix(Vector _x,Vector _y,Vector _z){x=_x;y=_y;z=_z;};
};
matrix transpose(matrix m);
Vector operator*(matrix m,Vector v);
matrix operator*(matrix m1,matrix m2);

class Quaternion{
 public:
	 float r,x,y,z;
	 Quaternion(){x=y=z=0.0f;r=1.0f;};
	 Quaternion(Vector v,float t){v=normalize(v);r=(float)cos(t/2.0);v=v*(float)sin(t/2.0);x=v.x;y=v.y;z=v.z;};
	 Quaternion(float _r,float _x,float _y,float _z){r=_r;x=_x;y=_y;z=_z;};
	 float angle(){return (float)(acos(r)*2.0);}
	 Vector axis(){Vector a(x,y,z); return a*(float)(1/sin(angle()/2.0));}
	 Vector xdir(){return Vector(1-2*(y*y+z*z),  2*(x*y+r*z),  2*(x*z-r*y));}
	 Vector ydir(){return Vector(  2*(x*y-r*z),1-2*(x*x+z*z),  2*(y*z+r*x));}
	 Vector zdir(){return Vector(  2*(x*z+r*y),  2*(y*z-r*x),1-2*(x*x+y*y));}
	 matrix  getmatrix(){return matrix(xdir(),ydir(),zdir());}
	 //operator matrix(){return getmatrix();}
};
Quaternion operator-(Quaternion q);
Quaternion operator*(Quaternion a,Quaternion b);
Vector    operator*(Quaternion q,Vector v);
Vector    operator*(Vector v,Quaternion q);
Quaternion slerp(Quaternion a,Quaternion b,float interp);

#endif
