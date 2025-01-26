// MIT License

// Copyright (c) 2019 Erin Catto

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#ifndef B2_TIMER_H
#define B2_TIMER_H

#include "b2_api.h"
#include "b2_settings.h"

/// Timer for profiling. This has platform specific code and may
/// not work on every platform.
class B2_API b2Timer
{
public:

	/// Constructor
	b2Timer();

	/// Reset the timer.
	void Reset();

	/// Get the time since construction or the last reset.
	float GetMilliseconds() const;

private:

#if defined(_WIN32)
	double m_start;
	static double s_invFrequency;
#elif defined(__linux__) || defined (__APPLE__)
	unsigned long long m_start_sec;
	unsigned long long m_start_usec;
#endif
};

#endif
