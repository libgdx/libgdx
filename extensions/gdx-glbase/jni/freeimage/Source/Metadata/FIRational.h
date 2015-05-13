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

#ifndef FIRATIONAL_H
#define FIRATIONAL_H

/**
Helper class to deal with rational numbers. 
NB: LONG data type is assumed to be a signed 32-bit number. 
*/
class FIRational {
private:
	/// numerator
	LONG _numerator;
	/// denominator
	LONG _denominator;

public:
	/// Default constructor
	FIRational();

	/// Constructor with longs
	FIRational(LONG n, LONG d = 1);

	/// Constructor with FITAG
	FIRational(const FITAG *tag);

	/// Constructor with a float
	FIRational(float value);

	/// Copy constructor
	FIRational (const FIRational& r);

	/// Destructor
	~FIRational();

	/// Assignement operator
	FIRational& operator=(FIRational& r);

	/// Get the numerator
	LONG getNumerator();

	/// Get the denominator
	LONG getDenominator();

	/// Converts rational value by truncating towards zero
	LONG truncate() {
		// Return truncated rational
		return _denominator ? (LONG) (_numerator / _denominator) : 0;
	}

	/**@name Implicit conversions */
	//@{	
	short shortValue() {
		return (short)truncate();
	}
	int intValue() {
		return (int)truncate();
	}
	LONG longValue() {
		return (LONG)truncate();
	}
	float floatValue() {
		return _denominator ? ((float)_numerator)/((float)_denominator) : 0;
	}
	double doubleValue() {
		return _denominator ? ((double)_numerator)/((double)_denominator) : 0;
	}
	//@}

	/// Checks if this rational number is an integer, either positive or negative
	BOOL isInteger();

	/// Convert as "numerator/denominator"
	std::string toString();

private:
	/// Initialize and normalize a rational number
	void initialize(LONG n, LONG d);

	/// Calculate GCD
	LONG gcd(LONG a, LONG b);
	
	/// Normalize numerator / denominator 
	void normalize();

};

#endif // FIRATIONAL_H

