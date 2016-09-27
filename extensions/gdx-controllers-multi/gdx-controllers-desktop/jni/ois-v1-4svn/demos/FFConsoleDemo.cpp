#include "OIS.h"

#include <math.h>
#include <cstdlib>
#include <iostream>
#include <iomanip>
#include <ios>
#include <sstream>
#include <vector>

using namespace std;

////////////////////////////////////Needed Windows Headers////////////
#if defined OIS_WIN32_PLATFORM
#  define WIN32_LEAN_AND_MEAN
#  include "windows.h"
#  include "resource.h"

////////////////////////////////////Needed Linux Headers//////////////
#elif defined OIS_LINUX_PLATFORM
#  include <X11/Xlib.h>
#else
#  error Sorry, not yet implemented on this platform.
#endif


using namespace OIS;

#if defined OIS_WIN32_PLATFORM

// The dialog proc we have to give to CreateDialog
LRESULT DlgProc( HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam )
{
	return FALSE;
}

#endif

//////////// Event handler class declaration ////////////////////////////////////////////////
class Application;
class JoystickManager;
class EffectManager;

class EventHandler : public KeyListener, public JoyStickListener
{
  protected:

    Application*     _pApplication;
    JoystickManager* _pJoystickMgr;
	EffectManager*   _pEffectMgr;

  public:

    EventHandler(Application* pApp);
    void initialize(JoystickManager* pJoystickMgr, EffectManager* pEffectMgr);

	bool keyPressed( const KeyEvent &arg );
	bool keyReleased( const KeyEvent &arg );

	bool buttonPressed( const JoyStickEvent &arg, int button );
	bool buttonReleased( const JoyStickEvent &arg, int button );

	bool axisMoved( const JoyStickEvent &arg, int axis );

	bool povMoved( const JoyStickEvent &arg, int pov );
};

//////////// Variable classes ////////////////////////////////////////////////////////

class Variable
{
  protected:

    double _dInitValue;
    double _dValue;

  public:

    Variable(double dInitValue) : _dInitValue(dInitValue) { reset(); }

    double getValue() const { return _dValue; }

    void reset() { _dValue = _dInitValue; }

    virtual void setValue(double dValue) { _dValue = dValue; }

    virtual string toString() const
    {
	  ostringstream oss;
	  oss << _dValue;
	  return oss.str();
	}

    virtual void update() {};
};

class Constant : public Variable
{
  public:

    Constant(double dInitValue) : Variable(dInitValue) {}

    virtual void setValue(double dValue) { }

};

class LimitedVariable : public Variable
{
  protected:

    double _dMinValue;
    double _dMaxValue;

  public:

    LimitedVariable(double dInitValue, double dMinValue, double dMaxValue) 
	: _dMinValue(dMinValue), _dMaxValue(dMaxValue), Variable(dInitValue)
    {}

    virtual void setValue(double dValue) 
    { 
	  _dValue = dValue;
	  if (_dValue > _dMaxValue)
		_dValue = _dMaxValue;
	  else if (_dValue < _dMinValue)
		_dValue = _dMinValue;
	}

/*    virtual string toString() const
    {
	  ostringstream oss;
	  oss << setiosflags(ios_base::right) << setw(4) 
	      << (int)(200.0 * getValue()/(_dMaxValue - _dMinValue)); // [-100%, +100%]
	  return oss.str();
	}*/
};

class TriangleVariable : public LimitedVariable
{
  protected:

    double _dDeltaValue;

  public:

    TriangleVariable(double dInitValue, double dDeltaValue, double dMinValue, double dMaxValue) 
	: LimitedVariable(dInitValue, dMinValue, dMaxValue), _dDeltaValue(dDeltaValue) {};

    virtual void update()
    {
	  double dValue = getValue() + _dDeltaValue;
	  if (dValue > _dMaxValue)
	  {
		dValue = _dMaxValue;
		_dDeltaValue = -_dDeltaValue;
		//cout << "Decreasing variable towards " << _dMinValue << endl;
	  }
	  else if (dValue < _dMinValue)
	  {
		dValue = _dMinValue;
		_dDeltaValue = -_dDeltaValue;
		//cout << "Increasing variable towards " << _dMaxValue << endl;
	  }
	  setValue(dValue);
      //cout << "TriangleVariable::update : delta=" << _dDeltaValue << ", value=" << dValue << endl;
	}
};

//////////// Variable effect class //////////////////////////////////////////////////////////

typedef map<string, Variable*> MapVariables;
typedef void (*EffectVariablesApplier)(MapVariables& mapVars, Effect* pEffect);

class VariableEffect
{
  protected:

    // Effect description
    const char* _pszDesc;

    // The associate OIS effect
    Effect* _pEffect;

    // The effect variables.
    MapVariables _mapVariables;

    // The effect variables applier function.
    EffectVariablesApplier _pfApplyVariables;

    // True if the effect is currently being played.
    bool _bActive;

  public:

    VariableEffect(const char* pszDesc, Effect* pEffect, 
				   const MapVariables& mapVars, const EffectVariablesApplier pfApplyVars)
	: _pszDesc(pszDesc), _pEffect(pEffect), 
	  _mapVariables(mapVars), _pfApplyVariables(pfApplyVars), _bActive(false)
    {}

    ~VariableEffect()
    {
	  if (_pEffect)
		delete _pEffect;
	  MapVariables::iterator iterVars;
	  for (iterVars = _mapVariables.begin(); iterVars != _mapVariables.end(); iterVars++)
		if (iterVars->second)
		  delete iterVars->second;
	  
	}

    void setActive(bool bActive = true)
    {
	  reset();
	  _bActive = bActive;
	}

    bool isActive()
    {
	  return _bActive;
	}

	Effect* getFFEffect()
	{
	  return _pEffect;
	}

	const char* getDescription() const
	{
	  return _pszDesc;
	}

    void update()
    {
	  if (isActive())
	  {
		// Update the variables.
		MapVariables::iterator iterVars;
		for (iterVars = _mapVariables.begin(); iterVars != _mapVariables.end(); iterVars++)
		  iterVars->second->update();

		// Apply the updated variable values to the effect.
		_pfApplyVariables(_mapVariables, _pEffect);
	  }
    }

    void reset()
    {
	  MapVariables::iterator iterVars;
	  for (iterVars = _mapVariables.begin(); iterVars != _mapVariables.end(); iterVars++)
		iterVars->second->reset();
	  _pfApplyVariables(_mapVariables, _pEffect);
    }

    string toString() const
    {
	  string str;
	  MapVariables::const_iterator iterVars;
	  for (iterVars = _mapVariables.begin(); iterVars != _mapVariables.end(); iterVars++)
		str += iterVars->first + ":" + iterVars->second->toString() + " ";
	  return str;
	}
};

//////////// Joystick manager class ////////////////////////////////////////////////////////

class JoystickManager
{
  protected:

    // Input manager.
    InputManager* _pInputMgr;

    // Vectors to hold joysticks and associated force feedback devices
    vector<JoyStick*> _vecJoys;
    vector<ForceFeedback*> _vecFFDev;

    // Selected joystick
    int _nCurrJoyInd;

    // Force feedback detected ?
    bool _bFFFound;

    // Selected joystick master gain.
    float _dMasterGain;

    // Selected joystick auto-center mode.
    bool _bAutoCenter;

  public:

    JoystickManager(InputManager* pInputMgr, EventHandler* pEventHdlr)
	: _pInputMgr(pInputMgr), _nCurrJoyInd(-1), _dMasterGain(0.5), _bAutoCenter(true)

    {
	  _bFFFound = false;
	  for( int nJoyInd = 0; nJoyInd < pInputMgr->getNumberOfDevices(OISJoyStick); ++nJoyInd ) 
	  {
		//Create the stick
		JoyStick* pJoy = (JoyStick*)pInputMgr->createInputObject( OISJoyStick, true );
		cout << endl << "Created buffered joystick #" << nJoyInd << " '" << pJoy->vendor() 
			 << "' (Id=" << pJoy->getID() << ")";
		
		// Check for FF, and if so, keep the joy and dump FF info
		ForceFeedback* pFFDev = (ForceFeedback*)pJoy->queryInterface(Interface::ForceFeedback );
		if( pFFDev )
		{
		  _bFFFound = true;

		  // Keep the joy to play with it.
		  pJoy->setEventCallback(pEventHdlr);
		  _vecJoys.push_back(pJoy);

		  // Keep also the associated FF device
		  _vecFFDev.push_back(pFFDev);
		  
		  // Dump FF supported effects and other info.
		  cout << endl << " * Number of force feedback axes : " 
			   << pFFDev->getFFAxesNumber() << endl;
		  const ForceFeedback::SupportedEffectList &lstFFEffects = 
			pFFDev->getSupportedEffects();
		  if (lstFFEffects.size() > 0)
		  {
			cout << " * Supported effects :";
			ForceFeedback::SupportedEffectList::const_iterator itFFEff;
			for(itFFEff = lstFFEffects.begin(); itFFEff != lstFFEffects.end(); ++itFFEff)
			  cout << " " << Effect::getEffectTypeName(itFFEff->second);
			cout << endl << endl;
		  }
		  else
			cout << "Warning: no supported effect found !" << endl;
		}
		else
		{
		  cout << " (no force feedback support detected) => ignored." << endl << endl;
		  _pInputMgr->destroyInputObject(pJoy);
		}
	  }
	}

    ~JoystickManager()
    {
	  for(size_t nJoyInd = 0; nJoyInd < _vecJoys.size(); ++nJoyInd)
		_pInputMgr->destroyInputObject( _vecJoys[nJoyInd] );
	}

    size_t getNumberOfJoysticks() const
    {
	  return _vecJoys.size();
	}

    bool wasFFDetected() const
    {
	  return _bFFFound;
	}

	enum EWhichJoystick { ePrevious=-1, eNext=+1 };

    void selectJoystick(EWhichJoystick eWhich)
    {
	  // Note: Reset the master gain to half the maximum and autocenter mode to Off,
	  // when really selecting a new joystick.
	  if (_nCurrJoyInd < 0)
	  {
		_nCurrJoyInd = 0;
		_dMasterGain = 0.5; // Half the maximum.
		changeMasterGain(0.0);
	  }
	  else
	  {
		_nCurrJoyInd += eWhich;
		if (_nCurrJoyInd < -1 || _nCurrJoyInd >= (int)_vecJoys.size())
		  _nCurrJoyInd = -1;
		if (_vecJoys.size() > 1 && _nCurrJoyInd >= 0)
		{
		  _dMasterGain = 0.5; // Half the maximum.
		  changeMasterGain(0.0);
		}
	  }
	}

    ForceFeedback* getCurrentFFDevice()
    {
	  return (_nCurrJoyInd >= 0) ? _vecFFDev[_nCurrJoyInd] : 0;
	}

    void changeMasterGain(float dDeltaPercent)
    {
	  if (_nCurrJoyInd >= 0)
	  {
		_dMasterGain += dDeltaPercent / 100;
		if (_dMasterGain > 1.0)
		  _dMasterGain = 1.0;
		else if (_dMasterGain < 0.0)
		  _dMasterGain = 0.0;
		
		_vecFFDev[_nCurrJoyInd]->setMasterGain(_dMasterGain);
	  }
	}

    enum EAutoCenterHow { eOff, eOn, eToggle };

    void changeAutoCenter(EAutoCenterHow eHow = eToggle)
    {
	  if (_nCurrJoyInd >= 0)
	  {
		if (eHow == eToggle)
		  _bAutoCenter = !_bAutoCenter;
		else
		  _bAutoCenter = (eHow == eOn ? true : false);
		_vecFFDev[_nCurrJoyInd]->setAutoCenterMode(_bAutoCenter);
	  }
	}

    void captureEvents()
    {
	  // This fires off buffered events for each joystick we have
	  for(size_t nJoyInd = 0; nJoyInd < _vecJoys.size(); ++nJoyInd)
		if( _vecJoys[nJoyInd] )	
		  _vecJoys[nJoyInd]->capture();
	}

    string toString() const
    {
	  // Warning: Wrong result if more than 10 joysticks ...
	  ostringstream oss;
	  oss << "Joy:" << (_nCurrJoyInd >= 0 ? (char)('0' + _nCurrJoyInd) : '-');
	  oss << " Gain:" << setiosflags(ios_base::right) << setw(3) << (int)(_dMasterGain*100);
	  oss << "% Center:" << (_bAutoCenter ? " On " : "Off");
	  return oss.str();
	}
};

//////////// Effect variables applier functions /////////////////////////////////////////////
// These functions apply the given Variables to the given OIS::Effect

// Variable force "Force" + optional "AttackFactor" constant, on a OIS::ConstantEffect
void forceVariableApplier(MapVariables& mapVars, Effect* pEffect)
{
  double dForce = mapVars["Force"]->getValue();
  double dAttackFactor = 1.0;
  if (mapVars.find("AttackFactor") != mapVars.end())
	dAttackFactor = mapVars["AttackFactor"]->getValue();

  ConstantEffect* pConstForce = dynamic_cast<ConstantEffect*>(pEffect->getForceEffect());
  pConstForce->level = (int)dForce;
  pConstForce->envelope.attackLevel = (unsigned short)fabs(dForce*dAttackFactor);
  pConstForce->envelope.fadeLevel = (unsigned short)fabs(dForce); // Fade never reached, in fact.
}

// Variable "Period" on an OIS::PeriodicEffect
void periodVariableApplier(MapVariables& mapVars, Effect* pEffect)
{
  double dPeriod = mapVars["Period"]->getValue();

  PeriodicEffect* pPeriodForce = dynamic_cast<PeriodicEffect*>(pEffect->getForceEffect());
  pPeriodForce->period = (unsigned int)dPeriod;
}


//////////// Effect manager class //////////////////////////////////////////////////////////

class EffectManager
{
  protected:

    // The joystick manager
    JoystickManager* _pJoystickMgr;

    // Vector to hold variable effects
    vector<VariableEffect*> _vecEffects;

    // Selected effect
    int _nCurrEffectInd;

    // Update frequency (Hz)
    unsigned int _nUpdateFreq;

	// Indexes (in _vecEffects) of the variable effects that are playable by the selected joystick.
	vector<size_t> _vecPlayableEffectInd;


  public:

    EffectManager(JoystickManager* pJoystickMgr, unsigned int nUpdateFreq) 
	: _pJoystickMgr(pJoystickMgr), _nUpdateFreq(nUpdateFreq), _nCurrEffectInd(-1)
    {
	  Effect* pEffect;
	  MapVariables mapVars;
	  ConstantEffect* pConstForce;
	  PeriodicEffect* pPeriodForce;

	  // Please don't modify or remove effects (unless there is some bug ...) : 
	  // add new ones to enhance the test repository.
	  // And feel free to add any tested device, even when the test failed !
	  // Tested devices capabilities :
      // - Logitech G25 Racing wheel : 
	  //   * Only 1 axis => no directional 2D effect (only left and right)
	  //   * Full support for constant force under WinXPSP2DX9 and Linux 2.6.22.9
	  //   * Full support for periodic forces under WinXPSP2DX9 
	  //     (but poor rendering under 20ms period), and no support under Linux 2.6.22.9
	  //   * Full support reported (not tested) for all other forces under WinXPSP2DX9, 
	  //     and no support under Linux 2.6.22.9
      // - Logitech Rumble pad 2 :
	  //   * Only 1 axis => no directional 2D effect (only left and right)
	  //   * Forces amplitude is rendered through the inertia motors rotation frequency
	  //     (stronger force => quicker rotation)
	  //   * 2 inertia motors : 1 with small inertia, 1 with "heavy" one.
	  //     => poor force feedback rendering ...
	  //   * Support (poor) for all OIS forces under WinXPSP2DX9,
	  //      and only for Triangle, Square and Sine periodic forces under Linux 2.6.22.9
	  //      (reported by enumeration, but does not seem to work actually)
	  // Master gain setting tests:
      // - Logitech G25 Racing wheel : WinXPSP2DX9=OK, Linux2.6.22.9=OK.
      // - Logitech Rumble pad 2 : WinXPSP2DX9=OK, Linux2.6.22.9=OK.
	  // Auto-center mode setting tests:
      // - Logitech G25 Racing wheel : WinXPSP2DX9=Failed (DINPUT?), Linux2.6.22.9=Reported as not supported.
      // - Logitech Rumble pad 2 : WinXPSP2DX9=Failed (DINPUT?), Linux2.6.22.9=Reported as not supported.

	  // 1) Constant force on 1 axis with 20s-period triangle oscillations in [-10K, +10K].
	  // Notes: Linux: replay_length: no way to get it to work if not 0 or Effect::OIS_INFINITE
	  // Tested devices :
      // - Logitech G25 Racing wheel : WinXPSP2DX9=OK, Linux2.6.22.9=OK.
      // - Logitech Rumble pad 2 : WinXPSP2DX9=OK (but only light motor involved), 
	  //                           Linux2.6.22.9=Not supported
	  pEffect = new Effect(Effect::ConstantForce, Effect::Constant);
	  pEffect->direction = Effect::North;
	  pEffect->trigger_button = 0;
	  pEffect->trigger_interval = 0;
	  pEffect->replay_length = Effect::OIS_INFINITE; // Linux/Win32: Same behaviour as 0.
	  pEffect->replay_delay = 0;
	  pEffect->setNumAxes(1);
	  pConstForce = dynamic_cast<ConstantEffect*>(pEffect->getForceEffect());
	  pConstForce->level = 5000;  //-10K to +10k
	  pConstForce->envelope.attackLength = 0;
	  pConstForce->envelope.attackLevel = (unsigned short)pConstForce->level;
	  pConstForce->envelope.fadeLength = 0;
	  pConstForce->envelope.fadeLevel = (unsigned short)pConstForce->level;

	  mapVars.clear();
	  mapVars["Force"] = 
		new TriangleVariable(0.0, // F0
							 4*10000/_nUpdateFreq / 20.0, // dF for a 20s-period triangle
							 -10000.0, // Fmin 
							 10000.0); // Fmax
	  mapVars["AttackFactor"] = new Constant(1.0);

	  _vecEffects.push_back
		(new VariableEffect
		       ("Constant force on 1 axis with 20s-period triangle oscillations "
				"of its signed amplitude in [-10K, +10K]",
				pEffect, mapVars, forceVariableApplier));

	  // 2) Constant force on 1 axis with noticeable attack 
	  //    with 20s-period triangle oscillations in [-10K, +10K].
	  // Tested devices :
      // - Logitech G25 Racing wheel : WinXPSP2DX9=OK, Linux=OK.
      // - Logitech Rumble pad 2 : WinXPSP2DX9=OK (including attack, but only light motor involved), 
	  //                           Linux2.6.22.9=Not supported.
	  pEffect = new Effect(Effect::ConstantForce, Effect::Constant);
	  pEffect->direction = Effect::North;
	  pEffect->trigger_button = 0;
	  pEffect->trigger_interval = 0;
	  pEffect->replay_length = Effect::OIS_INFINITE; //(unsigned int)(1000000.0/_nUpdateFreq); // Linux: Does not work.
	  pEffect->replay_delay = 0;
	  pEffect->setNumAxes(1);
	  pConstForce = dynamic_cast<ConstantEffect*>(pEffect->getForceEffect());
	  pConstForce->level = 5000;  //-10K to +10k
	  pConstForce->envelope.attackLength = (unsigned int)(1000000.0/_nUpdateFreq/2);
	  pConstForce->envelope.attackLevel = (unsigned short)(pConstForce->level*0.1);
	  pConstForce->envelope.fadeLength = 0; // Never reached, actually.
	  pConstForce->envelope.fadeLevel = (unsigned short)pConstForce->level; // Idem

	  mapVars.clear();
	  mapVars["Force"] = 
		new TriangleVariable(0.0, // F0
							 4*10000/_nUpdateFreq / 20.0, // dF for a 20s-period triangle
							 -10000.0, // Fmin 
							 10000.0); // Fmax
	  mapVars["AttackFactor"] = new Constant(0.1);

	  _vecEffects.push_back
		(new VariableEffect
		       ("Constant force on 1 axis with noticeable attack (app update period / 2)"
				"and 20s-period triangle oscillations of its signed amplitude in [-10K, +10K]",
				pEffect, mapVars, forceVariableApplier));

	  // 3) Triangle periodic force on 1 axis with 40s-period triangle oscillations
	  //    of its period in [10, 400] ms, and constant amplitude
	  // Tested devices :
      // - Logitech G25 Racing wheel : WinXPSP2DX9=OK, Linux=OK.
      // - Logitech Rumble pad 2 : WinXPSP2DX9=OK but only light motor involved,
	  //                           Linux2.6.22.9=Failed.
	  pEffect = new Effect(Effect::PeriodicForce, Effect::Triangle);
	  pEffect->direction = Effect::North;
	  pEffect->trigger_button = 0;
	  pEffect->trigger_interval = 0;
	  pEffect->replay_length = Effect::OIS_INFINITE;
	  pEffect->replay_delay = 0;
	  pEffect->setNumAxes(1);
	  pPeriodForce = dynamic_cast<PeriodicEffect*>(pEffect->getForceEffect());
	  pPeriodForce->magnitude = 10000;  // 0 to +10k
	  pPeriodForce->offset = 0;
	  pPeriodForce->phase = 0;  // 0 to 35599
	  pPeriodForce->period = 10000;  // Micro-seconds
	  pPeriodForce->envelope.attackLength = 0;
	  pPeriodForce->envelope.attackLevel = (unsigned short)pPeriodForce->magnitude;
	  pPeriodForce->envelope.fadeLength = 0;
	  pPeriodForce->envelope.fadeLevel = (unsigned short)pPeriodForce->magnitude;

	  mapVars.clear();
	  mapVars["Period"] = 
		new TriangleVariable(1*1000.0, // P0
							 4*(400-10)*1000.0/_nUpdateFreq / 40.0, // dP for a 40s-period triangle
							 10*1000.0, // Pmin 
							 400*1000.0); // Pmax
	  _vecEffects.push_back
		(new VariableEffect
		       ("Periodic force on 1 axis with 40s-period triangle oscillations "
				"of its period in [10, 400] ms, and constant amplitude",
				pEffect, mapVars, periodVariableApplier));

	}

    ~EffectManager()
    {
	  vector<VariableEffect*>::iterator iterEffs;
	  for (iterEffs = _vecEffects.begin(); iterEffs != _vecEffects.end(); iterEffs++)
		delete *iterEffs;
	}

    void updateActiveEffects()
    {
	  vector<VariableEffect*>::iterator iterEffs;
	  for (iterEffs = _vecEffects.begin(); iterEffs != _vecEffects.end(); iterEffs++)
		if ((*iterEffs)->isActive())
		{
		  (*iterEffs)->update();
		  _pJoystickMgr->getCurrentFFDevice()->modify((*iterEffs)->getFFEffect());
		}
	}

    void checkPlayableEffects()
    {
	  // Nothing to do if no joystick currently selected
	  if (!_pJoystickMgr->getCurrentFFDevice())
		return;

	  // Get the list of indexes of effects that the selected device can play
	  _vecPlayableEffectInd.clear();
	  for (size_t nEffInd = 0; nEffInd < _vecEffects.size(); nEffInd++)
	  {
		const Effect::EForce eForce = _vecEffects[nEffInd]->getFFEffect()->force;
		const Effect::EType eType = _vecEffects[nEffInd]->getFFEffect()->type;
		if (_pJoystickMgr->getCurrentFFDevice()->supportsEffect(eForce, eType))
		{
		  _vecPlayableEffectInd.push_back(nEffInd);
		}
	  }

	  // Print details about playable effects
	  if (_vecPlayableEffectInd.empty())
	  {
		cout << endl << endl << "The device can't play any effect of the test set" << endl;
	  }
	  else
	  {
		cout << endl << endl << "Selected device can play the following effects :" << endl;
		for (size_t nEffIndInd = 0; nEffIndInd < _vecPlayableEffectInd.size(); nEffIndInd++)
			printEffect(_vecPlayableEffectInd[nEffIndInd]);
		cout << endl;
	  }
	}

    enum EWhichEffect { ePrevious=-1, eNone=0, eNext=+1 };

    void selectEffect(EWhichEffect eWhich)
    {

	  // Nothing to do if no joystick currently selected
	  if (!_pJoystickMgr->getCurrentFFDevice())
	  {
		  cout << "\nNo Joystick selected.\n";  
		return;
	  }

	  // Nothing to do if joystick cannot play any effect
	  if (_vecPlayableEffectInd.empty())
	  {
		  cout << "\nNo playable effects.\n"; 
		return;
	  }

	  // If no effect selected, and next or previous requested, select the first one.
	  if (eWhich != eNone && _nCurrEffectInd < 0)
		_nCurrEffectInd = 0;

	  // Otherwise, remove the current one from the device, 
	  // and then select the requested one if any.
	  else if (_nCurrEffectInd >= 0)
	  {
		_pJoystickMgr->getCurrentFFDevice()
		  ->remove(_vecEffects[_vecPlayableEffectInd[_nCurrEffectInd]]->getFFEffect());
		_vecEffects[_vecPlayableEffectInd[_nCurrEffectInd]]->setActive(false);
		_nCurrEffectInd += eWhich;
		if (_nCurrEffectInd < -1 || _nCurrEffectInd >= (int)_vecPlayableEffectInd.size())
		  _nCurrEffectInd = -1;
	  }

	  // If no effect must be selected, reset the selection index
	  if (eWhich == eNone)
	  {
		_nCurrEffectInd = -1;
	  }

	  // Otherwise, upload the new selected effect to the device if any.
	  else if (_nCurrEffectInd >= 0)
	  {
		_vecEffects[_vecPlayableEffectInd[_nCurrEffectInd]]->setActive(true);
		_pJoystickMgr->getCurrentFFDevice()
		  ->upload(_vecEffects[_vecPlayableEffectInd[_nCurrEffectInd]]->getFFEffect());
	  }
	}

    void printEffect(size_t nEffInd)
    {
	  cout << "* #" << nEffInd << " : " << _vecEffects[nEffInd]->getDescription() << endl;
	}

    void printEffects()
    {
	  for (size_t nEffInd = 0; nEffInd < _vecEffects.size(); nEffInd++)
		  printEffect(nEffInd);
	}

    string toString() const
    {
	  ostringstream oss;
	  oss << "DevMem: " << setiosflags(ios_base::right) << setw(3);

	  //This causes constant exceptions with my device. Not needed for anything other than debugging
		//if (_pJoystickMgr->getCurrentFFDevice())
		//	oss << _pJoystickMgr->getCurrentFFDevice()->getFFMemoryLoad() << "%";
		//else
		//	oss << "----";
	  
		oss << " Effect:" << setw(2);
	  if (_nCurrEffectInd >= 0)
		oss << _vecPlayableEffectInd[_nCurrEffectInd] 
			<< " " << _vecEffects[_vecPlayableEffectInd[_nCurrEffectInd]]->toString();
	  else
		oss << "--";
	  return oss.str();
	}
};

//////////// Application class ////////////////////////////////////////////////////////

class Application
{
  protected:
    InputManager*    _pInputMgr;
    EventHandler*    _pEventHdlr;
    Keyboard*        _pKeyboard;
    JoystickManager* _pJoystickMgr;
	EffectManager*   _pEffectMgr;

#if defined OIS_WIN32_PLATFORM
    HWND             _hWnd;
#elif defined OIS_LINUX_PLATFORM
    Display*         _pXDisp;
    Window           _xWin;
#endif

    bool             _bMustStop;
    bool             _bIsInitialized;

    int _nStatus;

    // App. hart beat frequency.
    static const unsigned int _nHartBeatFreq = 20; // Hz

    // Effects update frequency (Hz) : Needs to be quite lower than app. hart beat frequency,
	// if we want to be able to calmly study effect changes ...
    static const unsigned int _nEffectUpdateFreq = 1; // Hz

  public:

    Application(int argc, const char* argv[])
    {
	  _pInputMgr = 0;
	  _pEventHdlr = 0;
	  _pKeyboard = 0;
	  _pJoystickMgr = 0;
	  _pEffectMgr = 0;

#if defined OIS_WIN32_PLATFORM
	  _hWnd = 0;
#elif defined OIS_LINUX_PLATFORM
	  _pXDisp = 0;
	  _xWin = 0;
#endif

	  _bMustStop = false;

	  _bIsInitialized = false;
	  _nStatus = 0;
	}

    int initialize()
    {
	  ostringstream wnd;

#if defined OIS_WIN32_PLATFORM

	  //Create a capture window for Input Grabbing
	  _hWnd = CreateDialog( 0, MAKEINTRESOURCE(IDD_DIALOG1), 0,(DLGPROC)DlgProc);
	  if( _hWnd == NULL )
		OIS_EXCEPT(E_General, "Failed to create Win32 Window Dialog!");

	  ShowWindow(_hWnd, SW_SHOW);

	  wnd << (size_t)_hWnd; 

#elif defined OIS_LINUX_PLATFORM

	  //Connects to default X window
	  if( !(_pXDisp = XOpenDisplay(0)) )
		OIS_EXCEPT(E_General, "Error opening X!");

	  //Create a window
	  _xWin = XCreateSimpleWindow(_pXDisp,DefaultRootWindow(_pXDisp), 0,0, 100,100, 0, 0, 0);

	  //bind our connection to that window
	  XMapWindow(_pXDisp, _xWin);

	  //Select what events we want to listen to locally
	  XSelectInput(_pXDisp, _xWin, StructureNotifyMask);

	  //Wait for Window to show up
	  XEvent event;
	  do {	XNextEvent(_pXDisp, &event); } while(event.type != MapNotify);

	  wnd << _xWin;

#endif

	  // Create OIS input manager
	  ParamList pl;
	  pl.insert(make_pair(string("WINDOW"), wnd.str()));
	  _pInputMgr = InputManager::createInputSystem(pl);
	  cout << _pInputMgr->inputSystemName() << " created." << endl;

	  // Create the event handler.
	  _pEventHdlr = new EventHandler(this);

	  // Create a simple keyboard
	  _pKeyboard = (Keyboard*)_pInputMgr->createInputObject( OISKeyboard, true );
	  _pKeyboard->setEventCallback( _pEventHdlr );

	  // Create the joystick manager.
	  _pJoystickMgr = new JoystickManager(_pInputMgr, _pEventHdlr);
	  if( !_pJoystickMgr->wasFFDetected() )
	  {
		cout << "No Force Feedback device detected." << endl;
		_nStatus = 1;
		return _nStatus;
	  }

	  // Create force feedback effect manager.
	  _pEffectMgr = new EffectManager(_pJoystickMgr, _nEffectUpdateFreq);

	  // Initialize the event handler.
	  _pEventHdlr->initialize(_pJoystickMgr, _pEffectMgr);

	  _bIsInitialized = true;

	  return _nStatus;
	}

#if defined OIS_LINUX_PLATFORM

    // This is just here to show that you still receive x11 events, 
    // as the lib only needs mouse/key events
    void checkX11Events()
    {
	  XEvent event;
	  
	  //Poll x11 for events
	  while( XPending(_pXDisp) > 0 )
	  {
		XNextEvent(_pXDisp, &event);
	  }
	}
#endif

    int run()
    {
	  const unsigned int nMaxEffectUpdateCnt = _nHartBeatFreq / _nEffectUpdateFreq;
	  unsigned int nEffectUpdateCnt = 0;

	  // Initailize app. if not already done, and exit if something went wrong.
	  if (!_bIsInitialized)
		initialize();

	  if (!_bIsInitialized)
		return _nStatus;

	  try
	  {
		//Main polling loop
		while(!_bMustStop)
		{
		  // This fires off buffered events for keyboards
		  _pKeyboard->capture();

		  // This fires off buffered events for each joystick we have
		  _pJoystickMgr->captureEvents();

		  // Update currently selected effects if time has come to.
		  if (!nEffectUpdateCnt)
		  {
			_pEffectMgr->updateActiveEffects();
			nEffectUpdateCnt = nMaxEffectUpdateCnt;
		  }
		  else
			nEffectUpdateCnt--;

		  // Update state line.
		  cout << "\r" << _pJoystickMgr->toString() << " " << _pEffectMgr->toString()
			   << "                           ";

		  //Throttle down CPU usage & handle OS events
#if defined OIS_WIN32_PLATFORM
		  Sleep( (DWORD)(1000.0/_nHartBeatFreq) );
		  MSG msg;
		  while( PeekMessage( &msg, NULL, 0U, 0U, PM_REMOVE ) )
		  {
			TranslateMessage( &msg );
			DispatchMessage( &msg );
		  }
#elif defined OIS_LINUX_PLATFORM
		  checkX11Events();
		  usleep(1000000.0/_nHartBeatFreq);
#endif
		}
	  }
	  catch( const Exception &ex )
	  {
#if defined OIS_WIN32_PLATFORM
		MessageBox(0, ex.eText, "Exception Raised!", MB_OK);
#else
		cout << endl << "OIS Exception Caught!" << endl 
			 << "\t" << ex.eText << "[Line " << ex.eLine << " in " << ex.eFile << "]" << endl;
#endif
	  }

	  terminate();

	  return _nStatus;
	}

    void stop()
    {
	  _bMustStop = true;
	}

    void terminate()
    {
	  if (_pInputMgr)
	  {
		_pInputMgr->destroyInputObject( _pKeyboard );
		_pKeyboard = 0;
		if (_pJoystickMgr)
		{
		  delete _pJoystickMgr;
		  _pJoystickMgr = 0;
		}
		InputManager::destroyInputSystem(_pInputMgr);
		_pInputMgr = 0;
	  }
	  if (_pEffectMgr)
	  {
		delete _pEffectMgr;
		_pEffectMgr = 0;
	  }
	  if (_pEventHdlr)
	  {
		delete _pEventHdlr;
		_pEventHdlr = 0;
	  }

#if defined OIS_LINUX_PLATFORM
	  // Be nice to X and clean up the x window
	  XDestroyWindow(_pXDisp, _xWin);
	  XCloseDisplay(_pXDisp);
#endif
	}

    JoystickManager* getJoystickManager()
    {
	  return _pJoystickMgr;
	}

    EffectManager* getEffectManager()
    {
	  return _pEffectMgr;
	}

	void printHelp()
	{
	  cout << endl
		   << "Keyboard actions :" << endl
		   << "* Escape      : Exit App" << endl
		   << "* H           : This help menu" << endl
		   << "* Right/Left  : Select next/previous joystick among the FF capable detected ones" << endl
		   << "* Up/Down     : Select next/previous effect for the selected joystick" << endl
		   << "* PgUp/PgDn   : Increase/decrease from 5% the master gain "
		   <<                  "for all the joysticks" << endl
		   << "* Space       : Toggle auto-centering on all the joysticks" << endl;
	  if (_bIsInitialized)
	  {
		cout << endl << "Implemented effects :" << endl << endl;
		_pEffectMgr->printEffects();
		cout << endl;
	  }
	}
};

//////////// Event handler class definition ////////////////////////////////////////////////

EventHandler::EventHandler(Application* pApp)
: _pApplication(pApp)
{}

void EventHandler::initialize(JoystickManager* pJoystickMgr, EffectManager* pEffectMgr)
{
  _pJoystickMgr = pJoystickMgr;
  _pEffectMgr = pEffectMgr;
}

bool EventHandler::keyPressed( const KeyEvent &arg )
{
  switch (arg.key)
  {
	// Quit.
	case KC_ESCAPE:
	  _pApplication->stop();
	  break;
	  
	// Help.
	case KC_H:
	  _pApplication->printHelp();
	  break;
	  
	// Change current joystick.
	case KC_RIGHT:
	  _pEffectMgr->selectEffect(EffectManager::eNone);
	  _pJoystickMgr->selectJoystick(JoystickManager::eNext);
	  _pEffectMgr->checkPlayableEffects();
	  break;
	case KC_LEFT:
	  _pEffectMgr->selectEffect(EffectManager::eNone);
	  _pJoystickMgr->selectJoystick(JoystickManager::ePrevious);
	  _pEffectMgr->checkPlayableEffects();
	  break;

	// Change current effect.
	case KC_UP:
	  _pEffectMgr->selectEffect(EffectManager::eNext);
	  break;
	case KC_DOWN:
	  _pEffectMgr->selectEffect(EffectManager::ePrevious);
	  break;

	// Change current master gain.
	case KC_PGUP:
	  _pJoystickMgr->changeMasterGain(5.0); // Percent
	  break;
	case KC_PGDOWN:
	  _pJoystickMgr->changeMasterGain(-5.0); // Percent
	  break;
	  
	// Toggle auto-center mode.
	case KC_SPACE:
	  _pJoystickMgr->changeAutoCenter();
	  break;
	  
	default:
	  cout << "Non mapped key: " << arg.key << endl;
  }
  return true;
}

bool EventHandler::keyReleased( const KeyEvent &arg )
{
  return true;
}

bool EventHandler::buttonPressed( const JoyStickEvent &arg, int button )
{
  return true;
}
bool EventHandler::buttonReleased( const JoyStickEvent &arg, int button )
{
  return true;
}
bool EventHandler::axisMoved( const JoyStickEvent &arg, int axis )
{
  return true;
}
bool EventHandler::povMoved( const JoyStickEvent &arg, int pov )
{
  return true;
}

//==========================================================================================
int main(int argc, const char* argv[])
{

  cout << endl 
	   << "This is a simple command line Force Feedback testing demo ..." << endl
	   << "All connected joystick devices will be created and if FF Support is found," << endl
	   << "you'll be able to play some predefined variable effects on them." << endl << endl
	   << "Note: 1 effect can be played on 1 joystick at a time for the moment." << endl << endl;

  Application app(argc, argv);
  
  int status = app.initialize();

  if (!status)
  {
	app.printHelp();

	status = app.run();
  }
  
  cout << endl << endl << "Exiting ..." << endl << endl;

#if defined OIS_WIN32_PLATFORM && _DEBUG
  cout << "Click on this window and ..." << endl;
  system("pause");
#endif

  exit(status);
}
