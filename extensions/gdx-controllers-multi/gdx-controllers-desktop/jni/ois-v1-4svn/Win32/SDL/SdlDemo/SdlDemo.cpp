#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <iostream>
#include <vector>
#include <sstream>
#include <OIS.h>
#include <SDL.h>
#include "resource.h"

LRESULT DlgProc( HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam );
void initSDL();
void destroySDL();
void initOIS();
void destroyOIS();

void OutputMessage( const std::string& message );

//Fun Globals ;-)
HWND hWnd = 0, hOut = 0, hDisp = 0;
bool appRunning = true;

using namespace OIS;

//////////// Common Event handler class ////////
class EventHandler : public KeyListener, public MouseListener
{
public:
	EventHandler() {}
	~EventHandler() {}
	bool keyPressed( const KeyEvent &arg ) {
		std::ostringstream ss;
		ss << "KeyPressed {" << arg.key	<< ", " << ((Keyboard*)(arg.device))->getAsString(arg.key)
			<< "} || Text (" << (arg.text > 0 ? (char)arg.text : '?') << ")";
		OutputMessage(ss.str());
		return true;
	}
	bool keyReleased( const KeyEvent &arg ) {
		if( arg.key == KC_ESCAPE || arg.key == KC_Q )
		{
			appRunning = false;
			return false;
		}
		std::ostringstream ss;
		ss << "KeyReleased (" << arg.key << ")";
		OutputMessage(ss.str());
		return true;
	}
	bool mouseMoved( const MouseEvent &arg ) {
		const MouseState& s = arg.state;
		std::ostringstream ss;
		ss << "MouseMoved: Abs("
		  << s.abX << ", " << s.abY << ", " << s.abZ << ") Rel("
		  << s.relX << ", " << s.relY << ", " << s.relZ << ")";
		OutputMessage(ss.str());
		return true;
	}
	bool mousePressed( const MouseEvent &arg, MouseButtonID id ) {
		std::ostringstream ss;
		ss << "MousePressed: " << id;
		OutputMessage(ss.str());
		return true;
	}
	bool mouseReleased( const MouseEvent &arg, MouseButtonID id ) {
		std::ostringstream ss;
		ss << "MouseReleased: " << id;
		OutputMessage(ss.str());
		return true;
	}
};

//More Fun Globals ;-)
EventHandler gHandler;
Mouse* gMouse = 0;
Keyboard* gKeyboard = 0;

//---------------------------------------------------------------------------------//
INT WINAPI WinMain( HINSTANCE hInst, HINSTANCE, LPSTR strCmdLine, INT )
{
	//Create a capture window for Input Grabbing
	hWnd = CreateDialog( 0, MAKEINTRESOURCE(IDD_MAIN_WINDOW), 0,(DLGPROC)DlgProc);
	if( hWnd == 0 ) exit(-1);
	ShowWindow(hWnd, SW_SHOW);
	
	hOut = GetDlgItem(hWnd, IDC_OUTPUT);
	if(hOut == 0) exit(-1);

	hDisp = GetDlgItem(hWnd, IDC_SDL_WIN);
	if(hDisp == 0) exit(-1);

	OutputMessage("Initialising Demo Application...");

	try
	{
		initSDL();
		initOIS();
	}
	catch(...)
	{
		appRunning = false;
	}

	while(appRunning)
	{
		Sleep( 30 );
		MSG  msg;
		while( PeekMessage( &msg, NULL, 0U, 0U, PM_REMOVE ) )
		{
			if( msg.message == WM_QUIT )
				appRunning = false;

			TranslateMessage( &msg );
			DispatchMessage( &msg );
		}

		if( gKeyboard )
		{
			gKeyboard->capture();
			if( gKeyboard->buffered() == false )
				if( gKeyboard->isKeyDown( KC_ESCAPE ) )
					appRunning = false;
		}

		if( gMouse )
		{
			gMouse->capture();
		}
	}

	destroyOIS();
	destroySDL();
	return 0;
}

//---------------------------------------------------------------------------------//
void initSDL()
{
	OutputMessage("Initialising SDL...");
	//I cannot get embedding functioning :/
	//std::ostringstream ss;
	//ss << "SDL_WINDOWID=" << hDisp;
	//_putenv(ss.str().c_str());
	//_putenv("SDL_VIDEODRIVER=windib");
	RECT r;
	GetWindowRect(hDisp, &r);

	if( SDL_Init(SDL_INIT_VIDEO) < 0 )
		throw("Error!");
	SDL_Surface *screen = SDL_SetVideoMode( r.right-r.left, r.bottom-r.top, 32, SDL_HWSURFACE );
	
	//SDL_Surface *screen = SDL_SetVideoMode( r.right-r.left, r.bottom-r.top, 0, 0 );
	//SetWindowPos(hDisp, 0, r.left, r.top, 0, 0, SWP_NOMOVE | SWP_NOSIZE);
	OutputMessage("Success!");
}

//---------------------------------------------------------------------------------//
void destroySDL()
{
	SDL_Quit();
}

//---------------------------------------------------------------------------------//
void initOIS()
{
	OutputMessage("Initialising OIS...");
	InputManager *im = InputManager::createInputSystem(ParamList());

	gKeyboard = static_cast<Keyboard*>(im->createInputObject(OISKeyboard, false));
	gKeyboard->setEventCallback( &gHandler );

	gMouse = static_cast<Mouse*>(im->createInputObject(OISMouse, false));
	gMouse->setEventCallback( &gHandler );
	
	std::ostringstream temp;
	unsigned int v = im->getVersionNumber();
	temp << "Success! >> " << "Version: " << (v>>16 ) << "." << ((v>>8) & 0x000000FF)
		<< "." << (v & 0x000000FF) << " >> Release Name: "
		<< im->getVersionName() << " >> Platform: " << im->inputSystemName();
	OutputMessage(temp.str());
	OutputMessage("");
	OutputMessage("***************************************************************");
	OutputMessage("TIP!: Keep the external SDL window active to recieve events");
	OutputMessage("TIP!: Git Escape in buffered or unbuffered to quit");
	OutputMessage("***************************************************************");
}

//---------------------------------------------------------------------------------//
void destroyOIS()
{
	if( InputManager::getSingletonPtr() )
	{
		InputManager::getSingletonPtr()->destroyInputObject(gKeyboard);
		InputManager::destroyInputSystem();
	}
}

//---------------------------------------------------------------------------------//
LRESULT DlgProc( HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam )
{
	int wmId = LOWORD(wParam), wmEvent = HIWORD(wParam);

	switch(uMsg)
	{
		case WM_CLOSE:
			PostQuitMessage(0);
			return TRUE;
		case WM_COMMAND:
		{
			switch(wmId)
			{
			case ID_EXIT:
				PostQuitMessage(0);
				return TRUE;
			case IDC_BUFF_KEYS:
			{
				gKeyboard->setBuffered( !gKeyboard->buffered() );
				std::ostringstream temp;
				temp << "** Setting Keyboard buffered Mode to: " << (gKeyboard->buffered() ? "Buffered" : "Unbuffered");
				OutputMessage(temp.str());
				return FALSE;
			}
			case IDC_BUFF_MOUSE:
			{
				gMouse->setBuffered( !gMouse->buffered() );
				std::ostringstream temp;
				temp << "** Setting Mouse buffered Mode to: " << (gMouse->buffered() ? "Buffered" : "Unbuffered");
				OutputMessage(temp.str());
				return FALSE;
			}
			default: break;
			}
		}
	}

	return FALSE;
}

//---------------------------------------------------------------------------------//
void OutputMessage( const std::string& message )
{
	static std::ostringstream buff;
	buff << message << "\r\n";
	SendMessage(hOut, WM_SETTEXT, 0, (LPARAM)buff.str().c_str());
}
