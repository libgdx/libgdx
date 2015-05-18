/**
 * @file arrayList.cpp
 * @brief Array based list implementation
 **/

#include "arrayList.h"

#include "glbase.h"
#include "macros.h"
#include <string.h>

using namespace std;


ArrayList::ArrayList(int maxsize) {
  maxsize = max( maxsize, 0 );
  this->elements = (maxsize > 0) ? new void *[ maxsize ] : NULL;
  this->maxsize = maxsize;
  this->size = 0;
}

ArrayList::~ArrayList(){
  delete[] elements;
}

void* ArrayList::get(int position){
  
  // Bound check
  if( position >= size ) {
    etrace("ArrayList get(): index out of bounds: %d (size=%d)", position, size);
    return NULL;
  }
  else{
    return elements[position];
  }
}

bool ArrayList::add(void* element){
  if( size >= maxsize ) {
    etrace("Too many elements added to ArrayList with maxsize %d!! Consider increasing array size", maxsize);
    return false;
  }
  
  elements[size++] = element;
  return true;
}

int ArrayList::getSize(){
  return size;
}

bool ArrayList::isEmpty()
{
  return size==0;
}

void ArrayList::clear(){
  size = 0;
}

void ArrayList::resize( int maxsize ) {
  if( maxsize == this->maxsize ) {
    return;
  }

  maxsize = max( maxsize, 0 );

  void **newElements = NULL;
  int newSize = min( maxsize, size );

  if( maxsize > 0 ) {
    newElements = new void *[ maxsize ];

    if( newSize > 0 ) {
      memcpy( newElements, elements, sizeof( void * ) * newSize );
	}
  }

  delete[] elements;
  elements = newElements;
  this->maxsize = maxsize;
  size = newSize;
}
