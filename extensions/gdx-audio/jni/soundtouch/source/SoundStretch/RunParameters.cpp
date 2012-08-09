////////////////////////////////////////////////////////////////////////////////
///
/// A class for parsing the 'soundstretch' application command line parameters
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2011-09-02 19:56:11 +0100 (Fr, 02 Sep 2011) $
// File revision : $Revision: 4 $
//
// $Id: RunParameters.cpp 131 2011-09-02 18:56:11Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////
//
// License :
//
//  SoundTouch audio processing library
//  Copyright (c) Olli Parviainen
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
////////////////////////////////////////////////////////////////////////////////

#include <string>
#include <stdlib.h>

#include "RunParameters.h"

using namespace std;

// Program usage instructions 

static const char licenseText[] = 
    "    LICENSE:\n"
    "    ========\n"
    "    \n"
    "    SoundTouch sound processing library\n"
    "    Copyright (c) Olli Parviainen\n"
    "    \n"
    "    This library is free software; you can redistribute it and/or\n"
    "    modify it under the terms of the GNU Lesser General Public\n"
    "    License version 2.1 as published by the Free Software Foundation.\n"
    "    \n"
    "    This library is distributed in the hope that it will be useful,\n"
    "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
    "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n"
    "    Lesser General Public License for more details.\n"
    "    \n"
    "    You should have received a copy of the GNU Lesser General Public\n"
    "    License along with this library; if not, write to the Free Software\n"
    "    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n"
    "    \n"
    "This application is distributed with full source codes; however, if you\n"
    "didn't receive them, please visit the author's homepage (see the link above).";

static const char whatText[] = 
    "This application processes WAV audio files by modifying the sound tempo,\n"
    "pitch and playback rate properties independently from each other.\n"
    "\n";

static const char usage[] = 
    "Usage :\n"
    "    soundstretch infilename outfilename [switches]\n"
    "\n"
    "To use standard input/output pipes, give 'stdin' and 'stdout' as filenames.\n"
    "\n"
    "Available switches are:\n"
    "  -tempo=n : Change sound tempo by n percents  (n=-95..+5000 %)\n"
    "  -pitch=n : Change sound pitch by n semitones (n=-60..+60 semitones)\n"
    "  -rate=n  : Change sound rate by n percents   (n=-95..+5000 %)\n"
    "  -bpm=n   : Detect the BPM rate of sound and adjust tempo to meet 'n' BPMs.\n"
    "             If '=n' is omitted, just detects the BPM rate.\n"
    "  -quick   : Use quicker tempo change algorithm (gain speed, lose quality)\n"
    "  -naa     : Don't use anti-alias filtering (gain speed, lose quality)\n"
    "  -speech  : Tune algorithm for speech processing (default is for music)\n"
    "  -license : Display the program license text (LGPL)\n";


// Converts a char into lower case
static int _toLowerCase(int c)
{
    if (c >= 'A' && c <= 'Z') 
    {
        c += 'a' - 'A';
    }
    return c;
}


// Constructor
RunParameters::RunParameters(const int nParams, const char * const paramStr[])
{
    int i;
    int nFirstParam;

    if (nParams < 3) 
    {
        // Too few parameters
        if (nParams > 1 && paramStr[1][0] == '-' && 
            _toLowerCase(paramStr[1][1]) == 'l') 
        {
            // '-license' switch
            throwLicense();
        }
        string msg = whatText;
        msg += usage;
        ST_THROW_RT_ERROR(msg.c_str());
    }

    inFileName = NULL;
    outFileName = NULL;
    tempoDelta = 0;
    pitchDelta = 0;
    rateDelta = 0;
    quick = 0;
    noAntiAlias = 0;
    goalBPM = 0;
    speech = FALSE;
    detectBPM = FALSE;

    // Get input & output file names
    inFileName = (char*)paramStr[1];
    outFileName = (char*)paramStr[2];

    if (outFileName[0] == '-')
    {
        // no outputfile name was given but parameters
        outFileName = NULL;
        nFirstParam = 2;
    }
    else
    {
        nFirstParam = 3;
    }

    // parse switch parameters
    for (i = nFirstParam; i < nParams; i ++) 
    {
        parseSwitchParam(paramStr[i]);
    }

    checkLimits();
}



// Checks parameter limits
void RunParameters::checkLimits()
{
    if (tempoDelta < -95.0f) 
    {
        tempoDelta = -95.0f;
    } 
    else if (tempoDelta > 5000.0f) 
    {
        tempoDelta = 5000.0f;
    }

    if (pitchDelta < -60.0f) 
    {
        pitchDelta = -60.0f;
    } 
    else if (pitchDelta > 60.0f) 
    {
        pitchDelta = 60.0f;
    }

    if (rateDelta < -95.0f) 
    {
        rateDelta = -95.0f;
    } 
    else if (rateDelta > 5000.0f) 
    {
        rateDelta = 5000.0f;
    }
}



// Unknown switch parameter -- throws an exception with an error message
void RunParameters::throwIllegalParamExp(const string &str) const
{
    string msg = "ERROR : Illegal parameter \"";
    msg += str;
    msg += "\".\n\n";
    msg += usage;
    ST_THROW_RT_ERROR(msg.c_str());
}



void RunParameters::throwLicense() const
{
    ST_THROW_RT_ERROR(licenseText);
}


float RunParameters::parseSwitchValue(const string &str) const
{
    int pos;

    pos = (int)str.find_first_of('=');
    if (pos < 0) 
    {
        // '=' missing
        throwIllegalParamExp(str);
    }

    // Read numerical parameter value after '='
    return (float)atof(str.substr(pos + 1).c_str());
}


// Interprets a single switch parameter string of format "-switch=xx"
// Valid switches are "-tempo=xx", "-pitch=xx" and "-rate=xx". Stores
// switch values into 'params' structure.
void RunParameters::parseSwitchParam(const string &str)
{
    int upS;

    if (str[0] != '-') 
    {
        // leading hyphen missing => not a valid parameter
        throwIllegalParamExp(str);
    }

    // Take the first character of switch name & change to lower case
    upS = _toLowerCase(str[1]);

    // interpret the switch name & operate accordingly
    switch (upS) 
    {
        case 't' :
            // switch '-tempo=xx'
            tempoDelta = parseSwitchValue(str);
            break;

        case 'p' :
            // switch '-pitch=xx'
            pitchDelta = parseSwitchValue(str);
            break;

        case 'r' :
            // switch '-rate=xx'
            rateDelta = parseSwitchValue(str);
            break;

        case 'b' :
            // switch '-bpm=xx'
            detectBPM = TRUE;
            try
            {
                goalBPM = parseSwitchValue(str);
            } 
            catch (const runtime_error)
            {
                // illegal or missing bpm value => just calculate bpm
                goalBPM = 0;
            }
            break;

        case 'q' :
            // switch '-quick'
            quick = 1;
            break;

        case 'n' :
            // switch '-naa'
            noAntiAlias = 1;
            break;

        case 'l' :
            // switch '-license'
            throwLicense();
            break;

        case 's' :
            // switch '-speech'
            speech = TRUE;
            break;

        default:
            // unknown switch
            throwIllegalParamExp(str);
    }
}
