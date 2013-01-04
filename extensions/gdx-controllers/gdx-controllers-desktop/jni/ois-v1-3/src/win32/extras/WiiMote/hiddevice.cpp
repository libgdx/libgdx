#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
//cWiimote 0.2 by Kevin Forbes (http://simulatedcomicproduct.com)
//This code is public domain, and comes with no warranty. The user takes full responsibility for anything that happens as a result from using this code.
//This was based in part on Alan Macek <www.alanmacek.com>'s USB interface library

//Edited for Toshiba Stack support (hopefully also all others) by 
//Sean Stellingwerff (http://sean.stellingwerff.com) using information
//gathered from http://www.lvr.com/hidpage.htm (Thanks a million! :D) 

//#include "stdafx.h"
#include "hiddevice.h"

extern "C" 
{
	#include "hidsdi.h"
	#include <Setupapi.h>
}
#pragma comment(lib, "setupapi.lib")
#pragma comment(lib, "hid.lib")

HIDP_CAPS							Capabilities;
PSP_DEVICE_INTERFACE_DETAIL_DATA	detailData;

cHIDDevice::cHIDDevice() : mConnected(false), mHandle(NULL), mEvent(NULL)
{
}


cHIDDevice::~cHIDDevice()
{
	if (mConnected)
	{
		Disconnect();
	}
}

bool cHIDDevice::Disconnect()
{
	bool retval = false;
	if (mConnected)
	{
		retval = (CloseHandle(mHandle) == TRUE && CloseHandle(mEvent) == TRUE);
	
		mConnected = false;
	}

	return retval;
}

bool cHIDDevice::Connect(unsigned short device_id, unsigned short vendor_id, int index)
{
	if (mConnected)
	{
		if (!Disconnect())
		{
			return false;
		}
	}

	// Find the wiimote(s)
	//for (int i = 0; i <= index; i++)
	OpenDevice( device_id, vendor_id, index );

	return mConnected;
}

bool cHIDDevice::OpenDevice(unsigned short device_id, unsigned short vendor_id, int index)
{
	//Use a series of API calls to find a HID with a specified Vendor IF and Product ID.
	HIDD_ATTRIBUTES						Attributes;
	SP_DEVICE_INTERFACE_DATA			devInfoData;
	bool								LastDevice = FALSE;
	bool								MyDeviceDetected = FALSE; 
	int									MemberIndex = 0;
	int									MembersFound = 0;
	GUID								HidGuid;
	ULONG								Length;
	LONG								Result;
	HANDLE								hDevInfo;
	ULONG								Required;

	Length = 0;
	detailData = NULL;
	mHandle=NULL;

	HidD_GetHidGuid(&HidGuid);	
	hDevInfo=SetupDiGetClassDevs(&HidGuid, NULL, NULL, DIGCF_PRESENT|DIGCF_INTERFACEDEVICE);
		
	devInfoData.cbSize = sizeof(devInfoData);

	MemberIndex = 0;
	MembersFound = 0;
	LastDevice = FALSE;

	do
	{
		Result=SetupDiEnumDeviceInterfaces(hDevInfo, 0, &HidGuid, MemberIndex, &devInfoData);
		if (Result != 0)
		{
			Result = SetupDiGetDeviceInterfaceDetail(hDevInfo, &devInfoData, NULL, 0, &Length, NULL);

			detailData = (PSP_DEVICE_INTERFACE_DETAIL_DATA)malloc(Length);
			detailData -> cbSize = sizeof(SP_DEVICE_INTERFACE_DETAIL_DATA);
			Result = SetupDiGetDeviceInterfaceDetail(hDevInfo, &devInfoData, detailData, Length, &Required, NULL);

			mHandle=CreateFile(detailData->DevicePath, 0, FILE_SHARE_READ|FILE_SHARE_WRITE, (LPSECURITY_ATTRIBUTES)NULL,OPEN_EXISTING, 0, NULL);
			Attributes.Size = sizeof(Attributes);

			Result = HidD_GetAttributes(mHandle, &Attributes);
			//Is it the desired device?

			MyDeviceDetected = FALSE;

			if (Attributes.VendorID == vendor_id)
			{
				if (Attributes.ProductID == device_id)
				{
					if (MembersFound == index)
					{
						//Both the Vendor ID and Product ID match.
						//printf("Wiimote found!\n");
						mConnected = true;
						GetCapabilities();

						WriteHandle=CreateFile(detailData->DevicePath, GENERIC_WRITE, FILE_SHARE_READ|FILE_SHARE_WRITE, (LPSECURITY_ATTRIBUTES)NULL, OPEN_EXISTING, 0, NULL);
						MyDeviceDetected = TRUE;

						PrepareForOverlappedTransfer();

						mEvent = CreateEvent(NULL, TRUE, TRUE, "");
						mOverlapped.Offset = 0;
						mOverlapped.OffsetHigh = 0;
						mOverlapped.hEvent = mEvent;
					
					} else { 
						//The Product ID doesn't match.
						CloseHandle(mHandle);
					}

					MembersFound++;
				}
			} else {
				CloseHandle(mHandle);
			}
			free(detailData);
		} else {
			LastDevice=TRUE;
		}
		MemberIndex = MemberIndex + 1;
	} while ((LastDevice == FALSE) && (MyDeviceDetected == FALSE));

	SetupDiDestroyDeviceInfoList(hDevInfo);
	return MyDeviceDetected;
}

bool cHIDDevice::WriteToDevice(unsigned const char * OutputReport, int num_bytes)
{
	bool retval = false;
	if (mConnected)
	{
		DWORD bytes_written;
		retval = (WriteFile( WriteHandle, OutputReport, num_bytes, &bytes_written, &mOverlapped) == TRUE); 
		retval = retval && bytes_written == num_bytes;
	}
	return retval;
}

void cHIDDevice::PrepareForOverlappedTransfer()
{
	//Get a handle to the device for the overlapped ReadFiles.
	ReadHandle=CreateFile(detailData->DevicePath, GENERIC_READ, FILE_SHARE_READ|FILE_SHARE_WRITE, (LPSECURITY_ATTRIBUTES)NULL, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);
}

void cHIDDevice::GetCapabilities()
{
	//Get the Capabilities structure for the device.
	PHIDP_PREPARSED_DATA PreparsedData;
	HidD_GetPreparsedData(mHandle, &PreparsedData);
	HidP_GetCaps(PreparsedData, &Capabilities);

	//No need for PreparsedData any more, so free the memory it's using.
	HidD_FreePreparsedData(PreparsedData);
}

bool cHIDDevice::ReadFromDevice(unsigned const char * buffer, int max_bytes, int & bytes_read, int timeout)
{
	bool retval = false;
	if (mConnected)
	{
		ReadFile( ReadHandle, (LPVOID)buffer,max_bytes,(LPDWORD)&bytes_read,(LPOVERLAPPED) &mOverlapped); 
		DWORD Result = WaitForSingleObject(mEvent, timeout);
		if (Result == WAIT_OBJECT_0) 
		{		
			retval = true;
		}
		else 
		{
			CancelIo(mHandle);			
		}
		ResetEvent(mEvent);
	}
	return retval;
}
#endif
