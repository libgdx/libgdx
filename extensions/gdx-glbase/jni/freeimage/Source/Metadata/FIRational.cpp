// ==========================================================
// Helper class for rational numbers
//
// Design and implementation by
// - Hervé Drolon <drolon@infonie.fr>
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
// ==========================================================

#include "FreeImage.h"
#include "Utilities.h"
#include "FIRational.h"

/// Initialize and normalize a rational number
void FIRational::initialize(LONG n, LONG d) {
	if(d) {
		_numerator = n;
		_denominator = d;
		// normalize rational
		normalize();
	} else {
		_numerator = 0;
		_denominator = 0;
	}
}

/// Default constructor
FIRational::FIRational() {
	_numerator = 0;
	_denominator = 0;
}

/// Constructor with longs
FIRational::FIRational(LONG n, LONG d) {
	initialize(n, d);
}

/// Constructor with FITAG
FIRational::FIRational(const FITAG *tag) {
	switch(FreeImage_GetTagType((FITAG*)tag)) {
		case FIDT_RATIONAL:		// 64-bit unsigned fraction 
		{
			DWORD *pvalue = (DWORD*)FreeImage_GetTagValue((FITAG*)tag);
			initialize((LONG)pvalue[0], (LONG)pvalue[1]);
			break;
		}

		case FIDT_SRATIONAL:	// 64-bit signed fraction 
		{
			LONG *pvalue = (LONG*)FreeImage_GetTagValue((FITAG*)tag);
			initialize((LONG)pvalue[0], (LONG)pvalue[1]);
			break;
		}
	}
}

FIRational::FIRational(float value) {
	if (value == (float)((LONG)value)) {
	   _numerator = (LONG)value;
	   _denominator = 1L;
	} else {
		int k, count;
		LONG n[4];

		float x = fabs(value);
		int sign = (value > 0) ? 1 : -1;

		// make a continued-fraction expansion of x
		count = -1;
		for(k = 0; k < 4; k++) {
			n[k] = (LONG)floor(x);
			count++;
			x -= (float)n[k];
			if(x == 0) break;
			x = 1 / x;
		}
		// compute the rational
		_numerator = 1;
		_denominator = n[count];

		for(int i = count - 1; i >= 0; i--) {
			if(n[i] == 0) break;
			LONG _num = (n[i] * _numerator + _denominator);
			LONG _den = _numerator;
			_numerator = _num;
			_denominator = _den;
		}
		_numerator *= sign;
	}
}

/// Copy constructor
FIRational::FIRational (const FIRational& r) {
	initialize(r._numerator, r._denominator);
}

/// Destructor
FIRational::~FIRational() {
}

/// Assignement operator
FIRational& FIRational::operator=(FIRational& r) {
	if(this != &r) {
		initialize(r._numerator, r._denominator);
	}
	return *this;
}

/// Get the numerator
LONG FIRational::getNumerator() {
	return _numerator;
}

/// Get the denominator
LONG FIRational::getDenominator() {
	return _denominator;
}

/// Calculate GCD
LONG FIRational::gcd(LONG a, LONG b) {
	LONG temp;
	while (b) {		// While non-zero value
		temp = b;	// Save current value
		b = a % b;	// Assign remainder of division
		a = temp;	// Copy old value
	}
	return a;		// Return GCD of numbers
}

/// Normalize numerator / denominator 
void FIRational::normalize() {
	if (_numerator != 1 && _denominator != 1) {	// Is there something to do?
		 // Calculate GCD
		LONG common = gcd(_numerator, _denominator);
		if (common != 1) { // If GCD is not one			
			_numerator /= common;	// Calculate new numerator
			_denominator /= common;	// Calculate new denominator
		}
	}
	if(_denominator < 0) {	// If sign is in denominator
		_numerator *= -1;	// Multiply num and den by -1
		_denominator *= -1;	// To keep sign in numerator
	}
}

/// Checks if this rational number is an Integer, either positive or negative
BOOL FIRational::isInteger() {
	if(_denominator == 1 || (_denominator != 0 && (_numerator % _denominator == 0)) || (_denominator == 0 && _numerator == 0))
		return TRUE;
	return FALSE;
}

/// Convert as "numerator/denominator"
std::string FIRational::toString() {
	std::ostringstream s;
	if(isInteger()) {
		s << intValue();
	} else {
		s << _numerator << "/" << _denominator;
	}
	return s.str();
}


