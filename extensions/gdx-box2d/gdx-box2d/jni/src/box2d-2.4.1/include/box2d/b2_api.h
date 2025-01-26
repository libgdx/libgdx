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

#ifndef B2_API_H
#define B2_API_H

#ifdef B2_SHARED
  #if defined _WIN32 || defined __CYGWIN__
    #ifdef box2d_EXPORTS
      #ifdef __GNUC__
        #define B2_API __attribute__ ((dllexport))
      #else
        #define B2_API __declspec(dllexport)
      #endif
    #else
      #ifdef __GNUC__
        #define B2_API __attribute__ ((dllimport))
      #else
        #define B2_API __declspec(dllimport)
      #endif
    #endif
  #else
    #if __GNUC__ >= 4
      #define B2_API __attribute__ ((visibility ("default")))
    #else
      #define B2_API
    #endif
  #endif
#else
  #define B2_API
#endif

#endif
