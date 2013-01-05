//cWiimote 0.2 by Kevin Forbes (http://simulatedcomicproduct.com)
//This code is public domain, and comes with no warranty. The user takes full responsibility for anything that happens as a result from using this code.

//#include "wiimote.h"
//#include <stdio.h>

//Features:
// Read Accelerometer, button values from the wiimote
// Read Accelerometer, stick, and button values from the nunchuck
// Preliminary IR support

//Known issues:
// The IR support is spotty at best. It tends to kick out if you plug your 'chuck in and out too many times
// Reading 'chuck calibration data doesn't seem to work, so the code just uses defaults
// Multiple Wiimote support not yet tested
// May only work with Bluesoleil stack?

//Instructions:
// See below for how to connect to a device and start the data stream.
// It is up to the user to call heartbeat fast enough - if you're too slow, you will loose data. Ideally, this would be done in a separate thread
// There are several public functions for getting the values from the wiimote. Look in cWiiMote::PrintStatus for examples.

//Version History:
//0.1 Preliminary Release
//0.2 Added nunchuck, IR support

/*
int main(int nargs, const char * cargs)
{
	cWiiMote wiimote;
	
	if (wiimote.ConnectToDevice() &&
		wiimote.StartDataStream())
	{
		for (;;)
		{
			wiimote.HeartBeat();
			wiimote.PrintStatus();
		}
	}
	return 0;
}
*/

//#eof "$Id: main.cpp,v 1.1.2.1 2008/02/14 03:33:36 pjcast Exp $"
