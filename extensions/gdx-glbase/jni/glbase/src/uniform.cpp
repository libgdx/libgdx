/*!
 * @file uniform.cpp
 *
 * @brief Uniformクラス.
 *
 */

#include "uniform.h"
#include "json.h"
#include "arrays.h"
#include "glbase.h"
#include "macros.h"


/*!
 * @briefコンストラクタ.
 */
Uniform::Uniform() {
  name = NULL;
  priority = RenderEnums::UNIPRIORITY_MRF_FIRST;
  values = new ArrayF();
}

//ゲッタ.
char* Uniform::getName() {

  return name;
}

int Uniform::getPriority() {

  return priority;
}

ArrayF *Uniform::getValues() {

  return values;
}

void Uniform::setName( char *name ) {
	if( name != NULL ) {
		if( this->name != NULL ) {
			delete[] this->name;
		}

		this->name = strdup2( name );
	}
}

void Uniform::setPriority( int priority ) {
	this->priority = priority;
}

/*!
 * @briefセットアップ.
 */
bool Uniform::setUp( JObj* o, int leftShiftBits ) {

  if( (leftShiftBits < 1) || (leftShiftBits > 30) ) {
    etrace( "invalid param" );
    return false;
  }

  ArrayI _values;//!< Uniform値.

  OBJ_SETSTR( o, name, "Name" );
  OBJ_SETINT( o, priority, "Priority" );
  OBJ_SETINTARRAY( o, _values, "Values" );

  if( (_values.getLen() < 1) || (_values.getLen() > 4) ) {

    etrace( "invalid format" );
    return false;
  }

  values->resize( _values.getLen() );

  for( int i = 0; i < _values.getLen(); i++ ) {
    values->el[ i ] = (float)_values[ i ] / (1 << leftShiftBits);
  }

  return true;
}

/*!
 * @briefデストラクタ.
 */
Uniform::~Uniform() {
  delete[] name;
  delete values;
}
