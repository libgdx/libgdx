#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
//cWiimote 0.2 by Kevin Forbes (http://simulatedcomicproduct.com)
//This code is public domain, and comes with no warranty. The user takes full responsibility for anything that happens as a result from using this code.

//Edited for Toshiba Stack support (hopefully also all others) by 
//Sean Stellingwerff (http://sean.stellingwerff.com) using information
//gathered from http://www.lvr.com/hidpage.htm (Thanks a million! :D) 

//#include "stdafx.h"
#include "wiimote.h"
#include <stdio.h>

//output channels
const unsigned char OUTPUT_CHANNEL_FORCE_FEEDBACK = 0x13;
const unsigned char OUTPUT_CHANNEL_LED = 0x11;
const unsigned char OUTPUT_CHANNEL_REPORT = 0x12;
const unsigned char OUTPUT_READ_MEMORY = 0x17;
const unsigned char OUTPUT_WRITE_MEMORY = 0x16;

const unsigned char OUTPUT_ENABLE_IR = 0x13;
const unsigned char OUTPUT_ENABLE_IR2 = 0x1a;

//report request types
const unsigned char REQUEST_CONTINUOUS_REPORTS = 0x4;
const unsigned char REQUEST_SINGLE_REPORTS = 0x0;

//input channels
const unsigned char INPUT_CHANNEL_BUTTONS_ONLY = 0x30;
const unsigned char INPUT_CHANNEL_BUTTONS_MOTION = 0x31;
const unsigned char INPUT_CHANNEL_WRITE_CONFIRM = 0x22;
const unsigned char INPUT_CHANNEL_EXPANSION_PORT = 0x20;

const unsigned char INPUT_CHANNEL_MOTION_IR = 0x33;
const unsigned char INPUT_CHANNEL_MOTION_CHUCK_IR = 0x37;
const unsigned char INPUT_CHANNEL_MOTION_CHUCK = 0x35;

//the ID values for a wiimote
const unsigned short mVendorID = 0x057E;
const unsigned short mDeviceID = 0x0306;

//how to find the calibration data for the wiimote
const unsigned short CALIBRATION_ADDRESS = 0x16;
const unsigned short CALIBRATION_DATA_LENGTH = 7;

//nunchuck constants
const unsigned long NUNCHUCK_STATUS_ADDRESS = 0x04A40000;
const unsigned long NUNCHUCK_CALIBRATION_ADDRESS = 0x04A40020;
const unsigned long NUNCHUCK_CALIBRATION_ADDRESS_2 = 0x04A40030;
const unsigned long NUNCHUCK_INIT_ADDRESS= 0x04A40040;
const unsigned long NUNCHUK_ID_ADDRESS = 0x04a400f0;
const unsigned char NUNCHUCK_INIT_VAL= 0x0;

//IR constants
const unsigned long IR_REG_1 = 0x04b00030;
const unsigned long IR_REG_2 = 0x04b00033;
const unsigned long IR_SENS_ADDR_1 = 0x04b00000;
const unsigned long IR_SENS_ADDR_2 = 0x04b0001a;

const unsigned char IR_SENS_MIDRANGE_PART1[] = {0x02, 0x00, 0x00, 0x71, 0x01, 0x00, 0xaa, 0x00, 0x64};
const unsigned char IR_SENS_MIDRANGE_PART2[] = {0x63, 0x03};

const unsigned char IR_MODE_OFF = 0;
const unsigned char IR_MODE_STD = 1;
const unsigned char IR_MODE_EXP = 3;
const unsigned char IR_MODE_FULL = 5;

cWiiMote::cWiiMote()
{
	Init();
}

cWiiMote::~cWiiMote()
{
	Disconnect();
}

void cWiiMote::Init()
{
	mReportMode = REPORT_MODE_EVENT_BUTTONS;
	mLastButtonStatus.Init();
	mLastExpansionReport.Init();
	mLastMotionReport.Init();
	mOutputControls.Init();
	mReadInfo.Init();
	mAccelCalibrationData.Init();
	mNunchuckAccelCalibrationData.Init();
	mNunchuckStickCalibrationData.Init();
	mLastIRReport.Init();
	mNunchuckAttached = false;
	mIRRunning = false;
	mDataStreamRunning = false;
}

bool cWiiMote::SetReportMode(eReportMode mode)
{
	mReportMode = mode;
	return SendReportMode();
}

bool cWiiMote::SendReportMode()
{
	bool continuous = true;
	unsigned char channel = INPUT_CHANNEL_BUTTONS_ONLY;
	bool check_chuck = false;
	
	switch (mReportMode)
	{
	case REPORT_MODE_MOTION_IR:
		channel = INPUT_CHANNEL_MOTION_IR;
		break;
	case REPORT_MODE_MOTION_CHUCK_IR:
		channel = INPUT_CHANNEL_MOTION_CHUCK_IR;
		check_chuck = true;
		break;
	case REPORT_MODE_MOTION_CHUCK:
		channel = INPUT_CHANNEL_MOTION_CHUCK;
		check_chuck = true;
		break;
	case REPORT_MODE_MOTION:
		channel = INPUT_CHANNEL_BUTTONS_MOTION;
		break;
	case REPORT_MODE_EVENT_BUTTONS:
		channel = INPUT_CHANNEL_BUTTONS_ONLY;
		continuous = false;
		break;
	default:
		break;
	}

	//check to make sure that there is a chuck attached
//	if (check_chuck && !mNunchuckAttached)
//	{
//		printf("Supposed to check for nunchuck, but couldn't find one!");
//		return false;
//	}

	bool retval = SelectInputChannel(continuous,channel);
	return retval;
}

bool cWiiMote::ConnectToDevice(int index)
{
	Init();
	const bool retval = mHIDDevice.Connect(mDeviceID,mVendorID,index) && 
						SetReportMode(REPORT_MODE_MOTION_CHUCK_IR) && 
						UpdateOutput() &&
						ReadCalibrationData();

	if (retval)
	{
		InitNunchuck();
	}
	return retval;
}

bool cWiiMote::Disconnect()
{
	bool retval = false;
	StopDataStream();
	
	if (mHIDDevice.IsConnected())
	{
		retval = mHIDDevice.Disconnect();
	}

	return retval;
}

bool cWiiMote::SetVibration(bool vib_on)
{
	bool retval = true;
	if (mOutputControls.mVibration != vib_on)
	{
		mOutputControls.mVibration = vib_on;
		retval = UpdateOutput();
	}
	return retval;
}

void cWiiMote::ClearBuffer()
{
	memset(mOutputBuffer,0, mOutputBufferSize);
}

bool cWiiMote::SetLEDs(bool led1, bool led2, bool led3, bool led4)
{
	const bool no_change = mOutputControls.mLED1 == led1 &&
							mOutputControls.mLED2 == led2 &&
							mOutputControls.mLED3 == led3 &&
							mOutputControls.mLED4 == led4;

	if (no_change)
	{
		return true;
	}

	mOutputControls.mLED1 = led1;
	mOutputControls.mLED2 = led2;	
	mOutputControls.mLED3 = led3;
	mOutputControls.mLED4 = led4;
	return UpdateOutput();
}

bool cWiiMote::UpdateOutput()
{
	ClearBuffer();
	mOutputBuffer[0] = OUTPUT_CHANNEL_LED;
	mOutputBuffer[1] =  (mOutputControls.mVibration ? 0x1 : 0x0) |
						(mOutputControls.mLED1 ? 0x1 : 0x0) << 4 | 
						(mOutputControls.mLED2 ? 0x1 : 0x0) << 5 | 
						(mOutputControls.mLED3 ? 0x1 : 0x0) << 6 | 
						(mOutputControls.mLED4 ? 0x1 : 0x0) << 7; 
	return mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
}

bool cWiiMote::HeartBeat(int timeout)
{
	bool retval = true;
	int bytes_read = 0;
	

	//most of these reports aren't implemented yet. I don't have a sensor bar or a nunchuck :)
	if (mHIDDevice.ReadFromDevice(mInputBuffer,mInputBufferSize,bytes_read) && (bytes_read > 0,timeout))
	{
		const int channel = mInputBuffer[0];
		switch (channel)
		{
			case INPUT_CHANNEL_EXPANSION_PORT:// 	 6 	Expansion Port change
				{
					ParseButtonReport(&mInputBuffer[1]);
					ParseExpansionReport(&mInputBuffer[2]);
					bool restart = mDataStreamRunning;
					StopDataStream();
					InitNunchuck();
					
					if (restart)
					{
						retval = StartDataStream();
					}
				}
			break;

			case INPUT_CHANNEL_BUTTONS_ONLY:// 	2 	Buttons only
				ParseButtonReport(&mInputBuffer[1]);
			break;

			case 0x21:// 	21 	Read data
				ParseButtonReport(&mInputBuffer[1]);
				ParseReadData(&mInputBuffer[3]);
				break;

			case INPUT_CHANNEL_WRITE_CONFIRM:// 	4 	Write data
			break;

			case 0x31:// 	5 	Buttons | Motion Sensing Report
				ParseButtonReport(&mInputBuffer[1]);
				ParseMotionReport(&mInputBuffer[3]);
			break;

			case 0x32:// 	16 	Buttons | Expansion Port | IR??
				ParseButtonReport(&mInputBuffer[1]);
			break;

			case INPUT_CHANNEL_MOTION_IR:
				ParseButtonReport(&mInputBuffer[1]);
				ParseMotionReport(&mInputBuffer[3]);
				ParseIRReport(&mInputBuffer[6]);
			break;

			case INPUT_CHANNEL_MOTION_CHUCK_IR:
				ParseButtonReport(&mInputBuffer[1]);
				ParseMotionReport(&mInputBuffer[3]);
				ParseIRReport(&mInputBuffer[6]);
				ParseChuckReport(&mInputBuffer[16]);
			break;

			case INPUT_CHANNEL_MOTION_CHUCK:
				ParseButtonReport(&mInputBuffer[1]);
				ParseMotionReport(&mInputBuffer[3]);
				ParseChuckReport(&mInputBuffer[6]);

			break;

			case 0x34:// 	21 	Buttons | Expansion Port | IR??
			case 0x3d:// 	21 	Buttons | Expansion Port | IR??
				ParseButtonReport(&mInputBuffer[1]);
			break;

			case 0x3e:// 	21 	Buttons | Motion Sensing Report | IR??
			case 0x3f:// 	21 	Buttons | Motion Sensing Report | IR??
				ParseButtonReport(&mInputBuffer[1]);
			break;
			default:
				retval = false;
				//unknown report
			break;
		}		
	}
	return retval;
}

void cWiiMote::ParseExpansionReport(const unsigned char *data)
{
	//four bytes long
	mLastExpansionReport.mAttachmentPluggedIn = (data[0] & 0x02) != 0;
	mLastExpansionReport.mIREnabled = (data[0] & 0x08) != 0;
	mLastExpansionReport.mSpeakerEnabled = (data[0] & 0x04) != 0;
	mLastExpansionReport.mLED1On = (data[0] & 0x10) != 0;
	mLastExpansionReport.mLED2On = (data[0] & 0x20) != 0;
	mLastExpansionReport.mLED3On = (data[0] & 0x40) != 0;
	mLastExpansionReport.mLED4On = (data[0] & 0x80) != 0;
	
	//two unknown bytes
	mLastExpansionReport.mBatteryLevel = data[3];
}

void cWiiMote::ParseButtonReport(const unsigned char * data)
{
	//two bytes long
	mLastButtonStatus.mA = (data[1] & 0x08) != 0;
 	mLastButtonStatus.mB = (data[1] & 0x04) != 0;
 	mLastButtonStatus.m1 = (data[1] & 0x02) != 0;
 	mLastButtonStatus.m2 = (data[1] & 0x01) != 0;
 	mLastButtonStatus.mPlus = (data[0] & 0x10) != 0;
 	mLastButtonStatus.mMinus = (data[1] & 0x10) != 0;
 	mLastButtonStatus.mHome = (data[1] & 0x80) != 0;
 	mLastButtonStatus.mUp = (data[0] & 0x08) != 0;
 	mLastButtonStatus.mDown = (data[0] & 0x04) != 0;
 	mLastButtonStatus.mLeft = (data[0] & 0x01) != 0;
 	mLastButtonStatus.mRight = (data[0] & 0x02) != 0;
}

void cWiiMote::ParseMotionReport(const unsigned char * data)
{
	//three bytes long
	mLastMotionReport.mX = data[0];
	mLastMotionReport.mY = data[1];
	mLastMotionReport.mZ = data[2];
}

void cWiiMote::PrintStatus() const
{
	float wX,wY,wZ;
	float cX,cY,cZ;
	float sX,sY;
	float irX,irY;
	
	wX =wY=wZ=cX=cY=cZ=sX=sY=irX=irY=0.f;

	GetCalibratedAcceleration(wX,wY,wZ);
	printf("W:[%+1.2f %+1.2f %+1.2f] ",wX,wY,wZ);

	if (mNunchuckAttached)
	{
		GetCalibratedChuckAcceleration(cX,cY,cZ);
		printf("N:[%+1.2f %+1.2f %+1.2f] ",cX,cY,cZ);

		GetCalibratedChuckStick(sX,sY);
		printf("S:[%+1.2f %+1.2f] ",sX,sY);
	}

	if (mIRRunning)
	{
		if (GetIRP1(irX,irY))
		{
			printf("P1:[%+1.2f %+1.2f]",irX,irY);
		}
		if (GetIRP2(irX,irY))
		{
			printf("P2:[%+1.2f %+1.2f]",irX,irY);
		}
	}


	//print the button status
	if (mLastButtonStatus.m1)
		printf("1");
	if (mLastButtonStatus.m2)
		printf("2");
	if (mLastButtonStatus.mA)
		printf("A");
	if (mLastButtonStatus.mB)
		printf("B");
	if (mLastButtonStatus.mPlus)
		printf("+");
	if (mLastButtonStatus.mMinus)
		printf("-");
	if (mLastButtonStatus.mUp)
		printf("U");
	if (mLastButtonStatus.mDown)
		printf("D");
	if (mLastButtonStatus.mLeft)
		printf("L");
	if (mLastButtonStatus.mRight)
		printf("R");
	if (mLastButtonStatus.mHome)
		printf("H");

	if (mNunchuckAttached)
	{
		if (mLastChuckReport.mButtonZ)
			printf("Z");
		if (mLastChuckReport.mButtonC)
			printf("C");
	}

	printf("\n");

}


bool cWiiMote::SelectInputChannel(bool continuous, unsigned char channel)
{
	ClearBuffer();
	mOutputBuffer[0] = OUTPUT_CHANNEL_REPORT;
	mOutputBuffer[1] = (continuous ? REQUEST_CONTINUOUS_REPORTS : REQUEST_SINGLE_REPORTS) | (mOutputControls.mVibration ? 0x1 : 0x0);
	mOutputBuffer[2] = channel;
	return mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
}


//this may or may not work to read buffers greater than 16 bytes. . . .
bool cWiiMote::IssueReadRequest(unsigned int address, unsigned short size, unsigned char * buffer)
{
	bool retval = false;
	if (mReadInfo.mReadStatus != tMemReadInfo::READ_PENDING)
	{
		ClearBuffer();
		mOutputBuffer[0] = OUTPUT_READ_MEMORY;
		mOutputBuffer[1] = (((address & 0xff000000) >> 24) & 0xFE) | (mOutputControls.mVibration ? 0x1 : 0x0);
		mOutputBuffer[2] = (address & 0x00ff0000) >> 16;
		mOutputBuffer[3] = (address & 0x0000ff00) >> 8;
		mOutputBuffer[4] = (address & 0xff);
		
		mOutputBuffer[5] = (size & 0xff00) >> 8;
		mOutputBuffer[6] = (size & 0xff);
		
		if (mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize))
		{
			mReadInfo.mReadStatus = tMemReadInfo::READ_PENDING;
			mReadInfo.mReadBuffer = buffer;
			mReadInfo.mTotalBytesToRead = size;
			mReadInfo.mBytesRead =0;
			mReadInfo.mBaseAddress = (unsigned short)(address & 0xFFFF);
			retval = true;
		}
	}

	return retval;
}

void cWiiMote::ParseReadData(const unsigned char * data)
{
	if(mReadInfo.mReadStatus == tMemReadInfo::READ_PENDING)
	{
		const bool error = (data[0] & 0x0F) != 0;
		if (error)
		{
			mReadInfo.mReadStatus = tMemReadInfo::READ_ERROR;
		}
		else
		{
			unsigned char bytes = (data[0] >> 4)+1;
			unsigned short offset = ((unsigned short)data[1] << 8) + data[2];
			unsigned int space_left_in_buffer = mReadInfo.mTotalBytesToRead -  mReadInfo.mBytesRead;
			if (offset == mReadInfo.mBytesRead + mReadInfo.mBaseAddress &&
				space_left_in_buffer >= bytes)
			{
				memcpy(&mReadInfo.mReadBuffer[mReadInfo.mBytesRead],&data[3],bytes);
				
				mReadInfo.mBytesRead+= bytes;
				if (mReadInfo.mBytesRead >= mReadInfo.mTotalBytesToRead)
				{
					mReadInfo.mReadStatus = tMemReadInfo::READ_COMPLETE;
				}
			}
		}
	}

}

bool cWiiMote::ReadData(unsigned int address, unsigned short size, unsigned char * buffer)
{
	if (IssueReadRequest(address, size,buffer))
	{
		while (mReadInfo.mReadStatus == tMemReadInfo::READ_PENDING)
		{
			if (!HeartBeat(1000))
			{
				break;
			}
		}
	}
	return mReadInfo.mReadStatus == tMemReadInfo::READ_COMPLETE;
}

bool cWiiMote::ReadCalibrationData()
{
	bool retval = false;
	unsigned char buffer[CALIBRATION_DATA_LENGTH];
	if (ReadData(CALIBRATION_ADDRESS, CALIBRATION_DATA_LENGTH,buffer))
	{
		mAccelCalibrationData.mXZero = buffer[0];
		mAccelCalibrationData.mYZero = buffer[1];
		mAccelCalibrationData.mZZero = buffer[2];
		mAccelCalibrationData.mXG = buffer[4];
		mAccelCalibrationData.mYG = buffer[5];
		mAccelCalibrationData.mZG = buffer[6];
		retval = true;
	}
	
	return retval;
}

void cWiiMote::GetCalibratedAcceleration(float & x, float & y, float &z) const
{
 	x = (mLastMotionReport.mX - mAccelCalibrationData.mXZero) / (float)(mAccelCalibrationData.mXG- mAccelCalibrationData.mXZero);
	y = (mLastMotionReport.mY - mAccelCalibrationData.mYZero) / (float)(mAccelCalibrationData.mYG- mAccelCalibrationData.mYZero);
	z = (mLastMotionReport.mZ - mAccelCalibrationData.mZZero) / (float)(mAccelCalibrationData.mZG- mAccelCalibrationData.mZZero);
}

void cWiiMote::GetCalibratedChuckAcceleration(float & x, float & y, float &z) const
{
	if (!mNunchuckAttached)
	{
		x = y = z = 0.f;
		return;
	}

	x = (mLastChuckReport.mAccelX - mNunchuckAccelCalibrationData.mXZero) / (float)(mNunchuckAccelCalibrationData.mXG- mNunchuckAccelCalibrationData.mXZero);
	y = (mLastChuckReport.mAccelY - mNunchuckAccelCalibrationData.mYZero) / (float)(mNunchuckAccelCalibrationData.mYG- mNunchuckAccelCalibrationData.mYZero);
	z = (mLastChuckReport.mAccelZ - mNunchuckAccelCalibrationData.mZZero) / (float)(mNunchuckAccelCalibrationData.mZG- mNunchuckAccelCalibrationData.mZZero);
}
void cWiiMote::GetCalibratedChuckStick(float & x, float & y) const
{
	if (!mNunchuckAttached)
	{
		x = y = 0.f;
		return;
	}

	if (mLastChuckReport.mStickX < mNunchuckStickCalibrationData.mXmid)
	{
		x = ((mLastChuckReport.mStickX - mNunchuckStickCalibrationData.mXmin) / (float)(mNunchuckStickCalibrationData.mXmid - mNunchuckStickCalibrationData.mXmin)) -  1.f;
	}
	else
	{
		x = ((mLastChuckReport.mStickX - mNunchuckStickCalibrationData.mXmid) / (float)(mNunchuckStickCalibrationData.mXmax - mNunchuckStickCalibrationData.mXmid));
	}

	if (mLastChuckReport.mStickY < mNunchuckStickCalibrationData.mYmid)
	{
		y = ((mLastChuckReport.mStickY - mNunchuckStickCalibrationData.mYmin) / (float)(mNunchuckStickCalibrationData.mYmid - mNunchuckStickCalibrationData.mYmin)) -  1.f;
	}
	else
	{
		y = ((mLastChuckReport.mStickY - mNunchuckStickCalibrationData.mYmid) / (float)(mNunchuckStickCalibrationData.mYmax - mNunchuckStickCalibrationData.mYmid));
	}
}


bool cWiiMote::WriteMemory(unsigned int address, unsigned char size, const unsigned char * buffer)
{
	bool retval = false;
	if (size <= 16)
	{
		ClearBuffer();
		mOutputBuffer[0] = OUTPUT_WRITE_MEMORY;
		mOutputBuffer[1] = (address & 0xff000000) >> 24 | (mOutputControls.mVibration ? 0x1 : 0x0);
		mOutputBuffer[2] = (address & 0x00ff0000) >> 16;
		mOutputBuffer[3] = (address & 0x0000ff00) >> 8;
		mOutputBuffer[4] = (address & 0xff);
		mOutputBuffer[5] = size;
		memcpy(&mOutputBuffer[6],buffer,size);
		retval = mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
	}

	return retval;
}

bool cWiiMote::InitNunchuck()
{

	bool retval = false;
	
	//first init the nunchuck, if it is present
	if (WriteMemory(NUNCHUCK_INIT_ADDRESS,1,&NUNCHUCK_INIT_VAL))
	{
	
		unsigned char buffer[16];
		//now try to read the nunchuck's calibration data
		if (ReadData(NUNCHUCK_CALIBRATION_ADDRESS,16,buffer))
		{
			
			//note that this hasn't worked properly for me yet (I get all 0xff). 
			/*mNunchuckAccelCalibrationData.mXZero = NunChuckByte(buffer[0]);
			mNunchuckAccelCalibrationData.mYZero = NunChuckByte(buffer[1]);
			mNunchuckAccelCalibrationData.mZZero = NunChuckByte(buffer[2]);

			mNunchuckAccelCalibrationData.mXG = NunChuckByte(buffer[4]);
			mNunchuckAccelCalibrationData.mYG = NunChuckByte(buffer[5]);
			mNunchuckAccelCalibrationData.mZG = NunChuckByte(buffer[6]);

			mNunchuckStickCalibrationData.mXmax = NunChuckByte(buffer[8]);
			mNunchuckStickCalibrationData.mXmin = NunChuckByte(buffer[9]);
			mNunchuckStickCalibrationData.mXmid = NunChuckByte(buffer[10]);
			mNunchuckStickCalibrationData.mYmax = NunChuckByte(buffer[11]);
			mNunchuckStickCalibrationData.mYmin = NunChuckByte(buffer[12]);
			mNunchuckStickCalibrationData.mYmid = NunChuckByte(buffer[13]);*/

			//these are default values from the wiili wiki
			mNunchuckAccelCalibrationData.mXZero = 0x7E;
			mNunchuckAccelCalibrationData.mYZero = 0x7A;
			mNunchuckAccelCalibrationData.mZZero = 0x7D;
			mNunchuckAccelCalibrationData.mXG = 0xB0;
			mNunchuckAccelCalibrationData.mYG = 0xAF;
			mNunchuckAccelCalibrationData.mZG = 0xB1;
			mNunchuckStickCalibrationData.mXmax = 0xe5;
			mNunchuckStickCalibrationData.mXmin = 0x21;
			mNunchuckStickCalibrationData.mXmid =  0x7c;
			mNunchuckStickCalibrationData.mYmax = 0xe7;
			mNunchuckStickCalibrationData.mYmin =  0x23;
			mNunchuckStickCalibrationData.mYmid = 0x7a;
			retval = true;

		}
	}
	mNunchuckAttached = retval;
	return retval;
}

void cWiiMote::ParseChuckReport(const unsigned char * data)
{
	mLastChuckReport.mStickX = NunChuckByte(data[0]);
	mLastChuckReport.mStickY = NunChuckByte(data[1]);
	mLastChuckReport.mAccelX = NunChuckByte(data[2]);
	mLastChuckReport.mAccelY = NunChuckByte(data[3]);
	mLastChuckReport.mAccelZ = NunChuckByte(data[4]);
	mLastChuckReport.mButtonC = (NunChuckByte(data[5]) & 0x2) == 0;
	mLastChuckReport.mButtonZ = (NunChuckByte(data[5]) & 0x1) == 0;
}

bool cWiiMote::EnableIR()
{
	bool retval = false;
	
	DisableIR();

	if (!mIRRunning)
	{
		ClearBuffer();
		mOutputBuffer[0] = OUTPUT_ENABLE_IR;
		mOutputBuffer[1] = 0x4 | (mOutputControls.mVibration ? 0x1 : 0x0);
		retval = mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
		
		if (retval)
		{
			mOutputBuffer[0] = OUTPUT_ENABLE_IR2;
			mOutputBuffer[1] = 0x4 | (mOutputControls.mVibration ? 0x1 : 0x0);
			retval = mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
		}

		if (retval)
		{
			unsigned char val = 0x1;
			retval = WriteMemory(IR_REG_1,1,&val);
		}
		
		if (retval)
		{
			retval = WriteMemory(IR_SENS_ADDR_1,9,IR_SENS_MIDRANGE_PART1);
		}

		if (retval)
		{
			retval = WriteMemory(IR_SENS_ADDR_2,2,IR_SENS_MIDRANGE_PART2);
		}


		if (retval)
		{
			retval = WriteMemory(IR_REG_2,1,&IR_MODE_EXP);
		}
		
		if (retval)
		{
			unsigned char val = 0x8;
			retval = WriteMemory(IR_REG_1,1,&val);
		}


		mIRRunning = retval;
	}
	return retval;

}

bool cWiiMote::DisableIR()
{
	bool retval = false;

	if (mIRRunning)
	{
		ClearBuffer();
		mOutputBuffer[0] = OUTPUT_ENABLE_IR;
		mOutputBuffer[1] = (mOutputControls.mVibration ? 0x1 : 0x0);
		retval = mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
		
		if (retval)
		{
			mOutputBuffer[0] = OUTPUT_ENABLE_IR2;
			mOutputBuffer[1] = (mOutputControls.mVibration ? 0x1 : 0x0);
			retval = mHIDDevice.WriteToDevice(mOutputBuffer,mOutputBufferSize);
		}

		mIRRunning = false;
	}
	return retval;

}

void cWiiMote::ParseIRReport(const unsigned char * data)
{
	mLastIRReport.mP1X = data[0] << 2 | (data[2] & 0x30) >>4;
	mLastIRReport.mP1Y = data[1] << 2 | (data[2] & 0xc0) >>6;
	mLastIRReport.mP1Size = data[2] & 0xf;

	mLastIRReport.mP2X = data[3] << 2 | (data[5] & 0x30) >>4;
	mLastIRReport.mP2Y = data[4] << 2 | (data[5] & 0xc0) >>6;
	mLastIRReport.mP2Size = data[5] & 0xf;

	mLastIRReport.mP1Found =  !(data[0] == 0xff && data[1] == 0xff && data[2] == 0xff);
	mLastIRReport.mP2Found =  !(data[3] == 0xff && data[4] == 0xff && data[5] == 0xff);
}

bool cWiiMote::GetIRP1(float &x, float &y) const
{
	bool retval = false;
	if (mIRRunning && mLastIRReport.mP1Found)
	{
		x = mLastIRReport.mP1X / 1024.f;
		y = mLastIRReport.mP1Y / 1024.f;
		retval = true;
	}
	return retval;
}


bool cWiiMote::GetIRP2(float &x, float &y) const
{
	bool retval = false;
	if (mIRRunning && mLastIRReport.mP2Found)
	{
		x = mLastIRReport.mP2X / 1024.f;
		y = mLastIRReport.mP2Y / 1024.f;
		retval = true;
	}
	return retval;

}

bool cWiiMote::StartDataStream()
{
	bool retval = false;
	
	StopDataStream();

	if (mNunchuckAttached)
	{
		retval =SetReportMode(REPORT_MODE_MOTION_CHUCK_IR);
	}
	else
	{
		retval = SetReportMode(REPORT_MODE_MOTION_IR);
	}
	EnableIR();

	mDataStreamRunning = retval;
	return retval;
}


bool cWiiMote::StopDataStream()
{
	if (mDataStreamRunning)
	{
		mDataStreamRunning = false;
		DisableIR();
		SetReportMode(REPORT_MODE_EVENT_BUTTONS);
	}
	return true;;
}
#endif
