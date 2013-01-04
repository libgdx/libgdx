#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
//cWiimote 0.2 by Kevin Forbes (http://simulatedcomicproduct.com)
//This code is public domain, and comes with no warranty. The user takes full responsibility for anything that happens as a result from using this code.

#ifndef WIIMOTE_H
#define WIIMOTE_H

#include "hiddevice.h"



class cWiiMote
{
public:
	cWiiMote();
	~cWiiMote();
	
	//connection management
	bool ConnectToDevice(int index = 0);
	bool Disconnect();	
	bool IsConnected() const {return mHIDDevice.IsConnected();}
	
	
	bool StartDataStream();
	bool StopDataStream();

	bool IsNunChuckAttached() { return mNunchuckAttached; }


	//this is the wiimote message pump. It should probably be called in loop from a thread
	bool HeartBeat(int timeout = 1);
	bool SetVibration(bool vib_on);
	bool SetLEDs(bool led1, bool led2, bool led3, bool led4);



	//Querying functions and structures:
	void GetCalibratedAcceleration(float & x, float & y, float &z) const;
	void GetCalibratedChuckAcceleration(float & x, float & y, float &z) const;
	void GetCalibratedChuckStick(float & x, float & y) const;
	bool GetIRP1(float &x, float &y) const;
	bool GetIRP2(float &x, float &y) const;


	struct tExpansionReport
	{
		bool mAttachmentPluggedIn;
		bool mIREnabled;
		bool mSpeakerEnabled;
		bool mLED1On;
		bool mLED2On;
		bool mLED3On;
		bool mLED4On;
		unsigned char mBatteryLevel;

		void Init()
		{
			mAttachmentPluggedIn = false;
			mIREnabled = false;
			mSpeakerEnabled = false;
			mLED1On = false;
			mLED2On = false;
			mLED3On = false;
			mLED4On = false;
			mBatteryLevel = 0;
		}
	};
	struct tButtonStatus
	{
		bool mA;
		bool mB;
		bool m1;
		bool m2;
		bool mPlus;
		bool mMinus;
		bool mHome;
		bool mUp;
		bool mDown;
		bool mLeft;
		bool mRight;
	
		void Init()
		{
			mA = mB = m1 = m2 = mPlus = mMinus = mHome = mUp = mDown = mLeft = mRight = false;
		}
	};
	struct tMotionReport
	{
		unsigned char mX;
		unsigned char mY;
		unsigned char mZ;
		
		void Init()
		{
			mX = mY = mZ = 0;
		}
	};

	struct tChuckReport
	{
		unsigned char mStickX;
		unsigned char mStickY;
		unsigned char mAccelX;
		unsigned char mAccelY;
		unsigned char mAccelZ;
		bool	mButtonC;
		bool	mButtonZ;
		void Init()
		{
			mStickX=mStickY=mAccelX=mAccelY=mAccelZ=0;
			mButtonC = mButtonZ = false;
		};
	};

	struct tIRReport
	{
		unsigned short mP1X;
		unsigned short mP1Y;
		
		unsigned short mP2X;
		unsigned short mP2Y;

		unsigned char mP1Size;
		unsigned char mP2Size;

		bool mP1Found;
		bool mP2Found;

		void Init()
		{
			mP1X = mP1Y = mP2X = mP2Y = mP1Size = mP2Size = 0;
			mP1Found = mP2Found = false;
		}


	};
	const tButtonStatus & GetLastButtonStatus() const {return mLastButtonStatus;}
	const tChuckReport & GetLastChuckReport() const {return mLastChuckReport;}
	const tMotionReport & GetLastMotionReport() const { return mLastMotionReport;}
	const tExpansionReport & GetLastExpansionReport() const { return mLastExpansionReport;}
	const tIRReport & GetLastIRReport() const { return mLastIRReport;}
	
	
	//debugging functions:
	void PrintStatus() const;

private:
	
	//parsing functions for input reports
	void ParseExpansionReport(const unsigned char * data);
	void ParseButtonReport(const unsigned char * data);
	void ParseMotionReport(const unsigned char * data);
	void ParseReadData(const unsigned char * data);
	void ParseChuckReport(const unsigned char * data);
	void ParseIRReport(const unsigned char * data);


	//tell the wiimote how to send data
	enum eReportMode
	{
		REPORT_MODE_EVENT_BUTTONS,
		REPORT_MODE_MOTION, 
		REPORT_MODE_MOTION_CHUCK,
		REPORT_MODE_MOTION_IR,
		REPORT_MODE_MOTION_CHUCK_IR
	};
	bool SetReportMode(eReportMode mode);


	//housekeeping functions
	void Init();
	void ClearBuffer();

	//low level tasks
	bool SelectInputChannel(bool continuous, unsigned char channel);
	bool UpdateOutput();
	bool ReadMemory(unsigned int address, unsigned short size, unsigned char * buffer) const;	
	bool WriteMemory(unsigned int address, unsigned char size, const unsigned char * buffer);

	bool ReadData(unsigned int address, unsigned short size, unsigned char * buffer);
	bool IssueReadRequest(unsigned int address, unsigned short size, unsigned char * buffer);
	bool ReadCalibrationData();
	bool SendReportMode();

	bool InitNunchuck();
	bool EnableIR();
	bool DisableIR();

	static inline unsigned char NunChuckByte(unsigned char in) {return (in ^ 0x17)+0x17;}
	//flash reading vars
	struct tMemReadInfo
	{
		enum eReadStatus
		{
			READ_PENDING,
			READ_NONE,
			READ_COMPLETE,
			READ_ERROR
		} mReadStatus;

		unsigned char * mReadBuffer;
		unsigned short mTotalBytesToRead;
		unsigned short mBytesRead;
		unsigned short mBaseAddress;
		void Init()
		{
			mReadStatus = READ_NONE;
			mReadBuffer = NULL;
			mTotalBytesToRead = 0;
			mBytesRead = 0;
			mBaseAddress = 0;
		}
	} mReadInfo;

	//calibration data for the wiimote
	struct tAccelCalibrationData
	{
		unsigned char mXZero;
		unsigned char mYZero;
		unsigned char mZZero;
		unsigned char mXG;
		unsigned char mYG;
		unsigned char mZG;
		void Init()
		{
			mXZero = mYZero = mZZero = mXG = mYG = mZG= 0;
		}
	} ;

	struct tStickCalibrationData
	{
		unsigned char mXmin;
		unsigned char mXmid;
		unsigned char mXmax;
		unsigned char mYmin;
		unsigned char mYmid;
		unsigned char mYmax;

		void Init()
		{
			mXmax = mYmax = mXmin = mYmin = mXmid = mYmid =0;
		}
	};
	
	tAccelCalibrationData mAccelCalibrationData;
	tAccelCalibrationData mNunchuckAccelCalibrationData;
	tStickCalibrationData mNunchuckStickCalibrationData;

	//output requests
	struct tOutputControls
	{
		bool mVibration;
		bool mLED1;
		bool mLED2;
		bool mLED3;
		bool mLED4;

		void Init()
		{
			mVibration = mLED1 = mLED2= mLED3= mLED4 = false;
		}
	};
	
	
	//input states
	tExpansionReport mLastExpansionReport;
	tButtonStatus mLastButtonStatus;
	tMotionReport mLastMotionReport;
	tChuckReport mLastChuckReport;
	tIRReport mLastIRReport;

	//output states
	tOutputControls mOutputControls;
	eReportMode	mReportMode;

	//our communications device
	cHIDDevice mHIDDevice;
	
	bool mNunchuckAttached;
	bool mIRRunning;
	bool mDataStreamRunning;

	//buffers for input/output
	static const int mOutputBufferSize = 22;
	unsigned char mOutputBuffer[mOutputBufferSize];
	static const int mInputBufferSize = 22;
	unsigned char mInputBuffer[mInputBufferSize];
};
#endif
#endif
