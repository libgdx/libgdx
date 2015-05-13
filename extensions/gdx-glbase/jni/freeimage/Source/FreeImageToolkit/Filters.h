// ==========================================================
// Upsampling / downsampling filters
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
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

#ifndef _FILTERS_H_
#define _FILTERS_H_

/**
 CGenericFilter is a generic abstract filter class used to access to the filter library.<br>
 Filters used in this library have been mainly taken from the following references : <br>
<b>Main reference</b> : <br>
Paul Heckbert, C code to zoom raster images up or down, with nice filtering. 
UC Berkeley, August 1989. [online] http://www-2.cs.cmu.edu/afs/cs.cmu.edu/Web/People/ph/heckbert.html

<b>Heckbert references</b> : <br>
<ul>
<li>Oppenheim A.V., Schafer R.W., Digital Signal Processing, Prentice-Hall, 1975
<li>Hamming R.W., Digital Filters, Prentice-Hall, Englewood Cliffs, NJ, 1983
<li>Pratt W.K., Digital Image Processing, John Wiley and Sons, 1978
<li>Hou H.S., Andrews H.C., "Cubic Splines for Image Interpolation and Digital Filtering", 
IEEE Trans. Acoustics, Speech, and Signal Proc., vol. ASSP-26, no. 6, pp. 508-517, Dec. 1978.
</ul>

*/
class CGenericFilter
{
protected:

    #define FILTER_PI  double (3.1415926535897932384626433832795)
    #define FILTER_2PI double (2.0 * 3.1415926535897932384626433832795)
    #define FILTER_4PI double (4.0 * 3.1415926535897932384626433832795)

    /// Filter support
	double  m_dWidth;

public:
    
    /// Constructor
	CGenericFilter (double dWidth) : m_dWidth (dWidth) {}
	/// Destructor
    virtual ~CGenericFilter() {}

    /// Returns the filter support
	double GetWidth()                   { return m_dWidth; }
	/// Change the filter suport
    void   SetWidth (double dWidth)     { m_dWidth = dWidth; }

    /// Returns F(dVal) where F is the filter's impulse response
	virtual double Filter (double dVal) = 0;
};

// -----------------------------------------------------------------------------------
// Filters library
// All filters are centered on 0
// -----------------------------------------------------------------------------------

/**
 Box filter<br>
 Box, pulse, Fourier window, 1st order (constant) b-spline.<br><br>

 <b>Reference</b> : <br>
 Glassner A.S., Principles of digital image synthesis. Morgan Kaufmann Publishers, Inc, San Francisco, Vol. 2, 1995
*/
class CBoxFilter : public CGenericFilter
{
public:
    /**
	Constructor<br>
	Default fixed width = 0.5
	*/
    CBoxFilter() : CGenericFilter(0.5) {}
    virtual ~CBoxFilter() {}

    double Filter (double dVal) { return (fabs(dVal) <= m_dWidth ? 1.0 : 0.0); }
};

/** Bilinear filter
*/
class CBilinearFilter : public CGenericFilter
{
public:

    CBilinearFilter () : CGenericFilter(1) {}
    virtual ~CBilinearFilter() {}

    double Filter (double dVal) {
		dVal = fabs(dVal); 
		return (dVal < m_dWidth ? m_dWidth - dVal : 0.0); 
	}
};


/**
 Mitchell & Netravali's two-param cubic filter<br>

 The parameters b and c can be used to adjust the properties of the cubic. 
 They are sometimes referred to as "blurring" and "ringing" respectively. 
 The default is b = 1/3 and c = 1/3, which were the values recommended by 
 Mitchell and Netravali as yielding the most visually pleasing results in subjective tests of human beings. 
 Larger values of b and c can produce interesting op-art effects--for example, try b = 0 and c = -5. <br><br>

 <b>Reference</b> : <br>
 Don P. Mitchell and Arun N. Netravali, Reconstruction filters in computer graphics. 
 In John Dill, editor, Computer Graphics (SIGGRAPH '88 Proceedings), Vol. 22, No. 4, August 1988, pp. 221-228.
*/
class CBicubicFilter : public CGenericFilter
{
protected:
	// data for parameterized Mitchell filter
    double p0, p2, p3;
    double q0, q1, q2, q3;

public:
    /**
	Constructor<br>
	Default fixed width = 2
	@param b Filter parameter (default value is 1/3)
	@param c Filter parameter (default value is 1/3)
	*/
    CBicubicFilter (double b = (1/(double)3), double c = (1/(double)3)) : CGenericFilter(2) {
		p0 = (6 - 2*b) / 6;
		p2 = (-18 + 12*b + 6*c) / 6;
		p3 = (12 - 9*b - 6*c) / 6;
		q0 = (8*b + 24*c) / 6;
		q1 = (-12*b - 48*c) / 6;
		q2 = (6*b + 30*c) / 6;
		q3 = (-b - 6*c) / 6;
	}
    virtual ~CBicubicFilter() {}

    double Filter(double dVal) { 
		dVal = fabs(dVal);
		if(dVal < 1)
			return (p0 + dVal*dVal*(p2 + dVal*p3));
		if(dVal < 2)
			return (q0 + dVal*(q1 + dVal*(q2 + dVal*q3)));
		return 0;
	}
};

/**
 Catmull-Rom spline, Overhauser spline<br>

 When using CBicubicFilter filters, you have to set parameters b and c such that <br>
 b + 2 * c = 1<br>
 in order to use the numerically most accurate filter.<br>
 This gives for b = 0 the maximum value for c = 0.5, which is the Catmull-Rom 
 spline and a good suggestion for sharpness.<br><br>


 <b>References</b> : <br>
 <ul>
 <li>Mitchell Don P., Netravali Arun N., Reconstruction filters in computer graphics. 
 In John Dill, editor, Computer Graphics (SIGGRAPH '88 Proceedings), Vol. 22, No. 4, August 1988, pp. 221-228.
 <li>Keys R.G., Cubic Convolution Interpolation for Digital Image Processing. 
 IEEE Trans. Acoustics, Speech, and Signal Processing, vol. 29, no. 6, pp. 1153-1160, Dec. 1981.
 </ul>

*/
class CCatmullRomFilter : public CGenericFilter
{
public:

    /**
	Constructor<br>
	Default fixed width = 2
	*/
	CCatmullRomFilter() : CGenericFilter(2) {}
    virtual ~CCatmullRomFilter() {}

    double Filter(double dVal) { 
		if(dVal < -2) return 0;
		if(dVal < -1) return (0.5*(4 + dVal*(8 + dVal*(5 + dVal))));
		if(dVal < 0)  return (0.5*(2 + dVal*dVal*(-5 - 3*dVal)));
		if(dVal < 1)  return (0.5*(2 + dVal*dVal*(-5 + 3*dVal)));
		if(dVal < 2)  return (0.5*(4 + dVal*(-8 + dVal*(5 - dVal))));
		return 0;
	}
};

/**
 Lanczos-windowed sinc filter<br>
 
 Lanczos3 filter is an alternative to CBicubicFilter with high values of c about 0.6 ... 0.75 
 which produces quite strong sharpening. It usually offers better quality (fewer artifacts) and a sharp image.<br><br>

*/
class CLanczos3Filter : public CGenericFilter
{
public:
    /**
	Constructor<br>
	Default fixed width = 3
	*/
	CLanczos3Filter() : CGenericFilter(3) {}
    virtual ~CLanczos3Filter() {}

    double Filter(double dVal) { 
		dVal = fabs(dVal); 
		if(dVal < m_dWidth)	{
			return (sinc(dVal) * sinc(dVal / m_dWidth));
		}
		return 0;
	}

private:
	double sinc(double value) {
		if(value != 0) {
			value *= FILTER_PI;
			return (sin(value) / value);
		} 
		return 1;
	}
};

/**
 4th order (cubic) b-spline<br>

*/
class CBSplineFilter : public CGenericFilter
{
public:

    /**
	Constructor<br>
	Default fixed width = 2
	*/
	CBSplineFilter() : CGenericFilter(2) {}
    virtual ~CBSplineFilter() {}

    double Filter(double dVal) { 

		dVal = fabs(dVal);
		if(dVal < 1) return (4 + dVal*dVal*(-6 + 3*dVal)) / 6;
		if(dVal < 2) {
			double t = 2 - dVal;
			return (t*t*t / 6);
		}
		return 0;
	}
};

// -----------------------------------------------------------------------------------
// Window function library
// -----------------------------------------------------------------------------------

/** 
 Blackman window
*/
class CBlackmanFilter : public CGenericFilter
{
public:
    /**
	Constructor<br>
	Default width = 0.5
	*/
    CBlackmanFilter (double dWidth = double(0.5)) : CGenericFilter(dWidth) {}
    virtual ~CBlackmanFilter() {}

    double Filter (double dVal) {
		if(fabs (dVal) > m_dWidth) {
			return 0; 
        }
        double dN = 2 * m_dWidth + 1; 
		dVal /= (dN - 1);
        return 0.42 + 0.5*cos(FILTER_2PI*dVal) + 0.08*cos(FILTER_4PI*dVal); 
    }
};

#endif  // _FILTERS_H_
