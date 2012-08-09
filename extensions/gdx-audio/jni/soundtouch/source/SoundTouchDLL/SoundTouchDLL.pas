unit SoundTouchDLL;

//////////////////////////////////////////////////////////////////////////////
//
// SoundTouch.dll wrapper for accessing SoundTouch routines from Delphi/Pascal
//
//  Module Author : Christian Budde
//  
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouchDLL.pas 66 2009-02-24 14:32:44Z oparviai $
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

interface

uses
  Windows;

type
  TSoundTouchHandle = THandle;

  // Create a new instance of SoundTouch processor.
  TSoundTouchCreateInstance = function : TSoundTouchHandle; stdcall;

  // Destroys a SoundTouch processor instance.
  TSoundTouchDestroyInstance = procedure (Handle: TSoundTouchHandle); stdcall;

  // Get SoundTouch library version string
  TSoundTouchGetVersionString = function : PChar; stdcall;

  // Get SoundTouch library version Id
  TSoundTouchGetVersionId = function : Cardinal; stdcall;

  // Sets new rate control value. Normal rate = 1.0, smaller values
  // represent slower rate, larger faster rates.
  TSoundTouchSetRate = procedure (Handle: TSoundTouchHandle; newRate: Single); stdcall;

  // Sets new tempo control value. Normal tempo = 1.0, smaller values
  // represent slower tempo, larger faster tempo.
  TSoundTouchSetTempo = procedure (Handle: TSoundTouchHandle; newTempo: Single); stdcall;

  // Sets new rate control value as a difference in percents compared
  // to the original rate (-50 .. +100 %);
  TSoundTouchSetRateChange = procedure (Handle: TSoundTouchHandle; newRate: Single); stdcall;

  // Sets new tempo control value as a difference in percents compared
  // to the original tempo (-50 .. +100 %);
  TSoundTouchSetTempoChange = procedure (Handle: TSoundTouchHandle; newTempo: Single); stdcall;

  // Sets new pitch control value. Original pitch = 1.0, smaller values
  // represent lower pitches, larger values higher pitch.
  TSoundTouchSetPitch = procedure (Handle: TSoundTouchHandle; newPitch: Single); stdcall;

  // Sets pitch change in octaves compared to the original pitch
  // (-1.00 .. +1.00);
  TSoundTouchSetPitchOctaves = procedure (Handle: TSoundTouchHandle; newPitch: Single); stdcall;

  // Sets pitch change in semi-tones compared to the original pitch
  // (-12 .. +12);
  TSoundTouchSetPitchSemiTones = procedure (Handle: TSoundTouchHandle; newPitch: Single); stdcall;


  // Sets the number of channels, 1 = mono, 2 = stereo
  TSoundTouchSetChannels = procedure (Handle: TSoundTouchHandle; numChannels: Cardinal); stdcall;

  // Sets sample rate.
  TSoundTouchSetSampleRate = procedure (Handle: TSoundTouchHandle; SampleRate: Cardinal); stdcall;

  // Flushes the last samples from the processing pipeline to the output.
  // Clears also the internal processing buffers.
  //
  // Note: This function is meant for extracting the last samples of a sound
  // stream. This function may introduce additional blank samples in the end
  // of the sound stream, and thus it
  // in the middle of a sound stream.
  TSoundTouchFlush = procedure (Handle: TSoundTouchHandle); stdcall;

  // Adds 'numSamples' pcs of samples from the 'samples' memory position into
  // the input of the object. Notice that sample rate _has_to_ be set before
  // calling this function, otherwise throws a runtime_error exception.
  TSoundTouchPutSamples = procedure (Handle: TSoundTouchHandle;
                                     const Samples: PSingle; //< Pointer to sample buffer.
                                     NumSamples: Cardinal    //< Number of samples in buffer. Notice
                                                             //< that in case of stereo-sound a single sample
                                                             //< contains data for both channels.
                                    ); stdcall;

  // Clears all the samples in the object's output and internal processing
  // buffers.
  TSoundTouchClear = procedure (Handle: TSoundTouchHandle); stdcall;

  // Changes a setting controlling the processing system behaviour. See the
  // 'SETTING_...' defines for available setting ID's.
  //
  // \return 'TRUE' if the setting was succesfully changed
  TSoundTouchSetSetting = function (Handle: TSoundTouchHandle;
                                 SettingId: Integer;   //< Setting ID number. see SETTING_... defines.
                                 Value: Integer        //< New setting value.
                                ): Boolean; stdcall;

  // Reads a setting controlling the processing system behaviour. See the
  // 'SETTING_...' defines for available setting ID's.
  //
  // \return the setting value.
  TSoundTouchGetSetting = function (Handle: TSoundTouchHandle;
                                 settingId: Integer     //< Setting ID number, see SETTING_... defines.
                                ): Integer; stdcall;


  // Returns number of samples currently unprocessed.
  TSoundTouchNumUnprocessedSamples = function (Handle: TSoundTouchHandle): Cardinal; stdcall;

  // Adjusts book-keeping so that given number of samples are removed from beginning of the
  // sample buffer without copying them anywhere.
  //
  // Used to reduce the number of samples in the buffer when accessing the sample buffer directly
  // with 'ptrBegin' function.
  TSoundTouchReceiveSamples = function (Handle: TSoundTouchHandle;
                                     outBuffer: PSingle;           //< Buffer where to copy output samples.
                                     maxSamples: Integer      //< How many samples to receive at max.
                                    ): Cardinal; stdcall;

  // Returns number of samples currently available.
  TSoundTouchNumSamples = function (Handle: TSoundTouchHandle): Cardinal; stdcall;

  // Returns nonzero if there aren't any samples available for outputting.
  TSoundTouchIsEmpty = function (Handle: TSoundTouchHandle): Integer; stdcall;

var
  SoundTouchCreateInstance        : TSoundTouchCreateInstance;
  SoundTouchDestroyInstance       : TSoundTouchDestroyInstance;
  SoundTouchGetVersionString      : TSoundTouchGetVersionString;
  SoundTouchGetVersionId          : TSoundTouchGetVersionId;
  SoundTouchSetRate               : TSoundTouchSetRate;
  SoundTouchSetTempo              : TSoundTouchSetTempo;
  SoundTouchSetRateChange         : TSoundTouchSetRateChange;
  SoundTouchSetTempoChange        : TSoundTouchSetTempoChange;
  SoundTouchSetPitch              : TSoundTouchSetPitch;
  SoundTouchSetPitchOctaves       : TSoundTouchSetPitchOctaves;
  SoundTouchSetPitchSemiTones     : TSoundTouchSetPitchSemiTones;
  SoundTouchSetChannels           : TSoundTouchSetChannels;
  SoundTouchSetSampleRate         : TSoundTouchSetSampleRate;
  SoundTouchFlush                 : TSoundTouchFlush;
  SoundTouchPutSamples            : TSoundTouchPutSamples;
  SoundTouchClear                 : TSoundTouchClear;
  SoundTouchSetSetting            : TSoundTouchSetSetting;
  SoundTouchGetSetting            : TSoundTouchGetSetting;
  SoundTouchNumUnprocessedSamples : TSoundTouchNumUnprocessedSamples;
  SoundTouchReceiveSamples        : TSoundTouchReceiveSamples;
  SoundTouchNumSamples            : TSoundTouchNumSamples;
  SoundTouchIsEmpty               : TSoundTouchIsEmpty;

type
  TSoundTouch = class
  private
    FHandle     : TSoundTouchHandle;
    FRate       : Single;
    FPitch      : Single;
    FTempo      : Single;
    FSampleRate : Single;
    FChannels   : Cardinal;
    function GetNumSamples: Cardinal;
    function GetNumUnprocessedSamples: Cardinal;
    function GetIsEmpty: Integer;
    function GetPitchChange: Single;
    function GetRateChange: Single;
    function GetTempoChange: Single;
    procedure SetRate(const Value: Single);
    procedure SetPitch(const Value: Single);
    procedure SetTempo(const Value: Single);
    procedure SetPitchChange(const Value: Single);
    procedure SetRateChange(const Value: Single);
    procedure SetTempoChange(const Value: Single);
    procedure SetChannels(const Value: Cardinal);
    procedure SetSampleRate(const Value: Single);
  protected
    procedure SamplerateChanged; virtual;
    procedure ChannelsChanged; virtual;
    procedure PitchChanged; virtual;
    procedure TempoChanged; virtual;
    procedure RateChanged; virtual;
  public
    class function GetVersionString: string;
    class function GetVersionId: Cardinal;
    constructor Create; virtual;
    destructor Destroy; override;
    procedure Flush; virtual;
    procedure Clear; virtual;

    procedure PutSamples(const Samples: PSingle; const NumSamples: Cardinal);
    function ReceiveSamples(const outBuffer: PSingle; const maxSamples: Integer): Cardinal;

    function SetSetting(const SettingId: Integer; const Value: Integer): Boolean;
    function GetSetting(const settingId: Integer): Integer;

    property VersionString: string read GetVersionString;
    property VersionID: Cardinal read GetVersionId;
    property Channels: Cardinal read FChannels write SetChannels;
    property Rate: Single read FRate write SetRate;
    property RateChange: Single read GetRateChange write SetRateChange;
    property Tempo: Single read FTempo write SetTempo;
    property TempoChange: Single read GetTempoChange write SetTempoChange;
    property Pitch: Single read FPitch write SetPitch;
    property PitchChange: Single read GetPitchChange write SetPitchChange;
    property SampleRate: Single read FSampleRate write SetSampleRate;

    property NumSamples: Cardinal read GetNumSamples;
    property NumUnprocessedSamples: Cardinal read GetNumUnprocessedSamples;
    property IsEmpty: Integer read GetIsEmpty;
  end;

implementation

uses
  SysUtils;

{ TSoundTouch }

constructor TSoundTouch.Create;
begin
  inherited;
  FHandle := SoundTouchCreateInstance;
  FRate := 1;
  FTempo := 1;
  FPitch := 1;
  FChannels := 1;
  FSampleRate := 44100;
  SamplerateChanged;
  ChannelsChanged;
end;

destructor TSoundTouch.Destroy;
begin
  SoundTouchDestroyInstance(FHandle);
  inherited;
end;

procedure TSoundTouch.Flush;
begin
  SoundTouchFlush(FHandle);
end;

procedure TSoundTouch.Clear;
begin
  SoundTouchClear(FHandle);
end;

function TSoundTouch.GetIsEmpty: Integer;
begin
  result := SoundTouchIsEmpty(FHandle);
end;

function TSoundTouch.GetNumSamples: Cardinal;
begin
  result := SoundTouchNumSamples(FHandle);
end;

function TSoundTouch.GetNumUnprocessedSamples: Cardinal;
begin
  result := SoundTouchNumUnprocessedSamples(FHandle);
end;

function TSoundTouch.GetPitchChange: Single;
begin
  result := 100 * (FPitch - 1.0);
end;

function TSoundTouch.GetRateChange: Single;
begin
  result := 100 * (FRate - 1.0);
end;

function TSoundTouch.GetTempoChange: Single;
begin
  result := 100 * (FTempo - 1.0);
end;

class function TSoundTouch.GetVersionId: Cardinal;
begin
  result := SoundTouchGetVersionId;
end;

class function TSoundTouch.GetVersionString: string;
begin
  result := StrPas(SoundTouchGetVersionString);
end;

procedure TSoundTouch.SetChannels(const Value: Cardinal);
begin
  if FChannels <> Value then
  begin
    FChannels := Value;
    ChannelsChanged;
  end;
end;

procedure TSoundTouch.ChannelsChanged;
begin
  assert(FChannels in [1, 2]);
  SoundTouchSetChannels(FHandle, FChannels);
end;

procedure TSoundTouch.SetPitch(const Value: Single);
begin
  if FPitch <> Value then
  begin
    FPitch := Value;
    PitchChanged;
  end;
end;

procedure TSoundTouch.PitchChanged;
begin
  SoundTouchSetPitch(FHandle, FPitch);
end;

procedure TSoundTouch.putSamples(const Samples: PSingle;
  const NumSamples: Cardinal);
begin
  SoundTouchPutSamples(FHandle, Samples, NumSamples);
end;

procedure TSoundTouch.RateChanged;
begin
  SoundTouchSetRate(FHandle, FRate);
end;

function TSoundTouch.ReceiveSamples(const outBuffer: PSingle;
  const maxSamples: Integer): Cardinal;
begin
  result := SoundTouchReceiveSamples(FHandle, outBuffer, maxSamples);
end;

procedure TSoundTouch.SetPitchChange(const Value: Single);
begin
  Pitch := 1.0 + 0.01 * Value;
end;

procedure TSoundTouch.SetRate(const Value: Single);
begin
  if FRate <> Value then
  begin
    FRate := Value;
    RateChanged;
  end;
end;

procedure TSoundTouch.SetRateChange(const Value: Single);
begin
  Rate := 1.0 + 0.01 * Value;
end;

procedure TSoundTouch.SetSampleRate(const Value: Single);
begin
  if FSampleRate <> Value then
  begin
    FSampleRate := Value;
    SamplerateChanged;
  end;
end;

procedure TSoundTouch.SamplerateChanged;
begin
  assert(FSampleRate > 0);
  SoundTouchsetSampleRate(FHandle, round(FSampleRate));
end;

procedure TSoundTouch.SetTempo(const Value: Single);
begin
 if FTempo <> Value then
  begin
    FTempo := Value;
    TempoChanged;
  end;
end;

procedure TSoundTouch.SetTempoChange(const Value: Single);
begin
  Tempo := 1.0 + 0.01 * Value;
end;

function TSoundTouch.GetSetting(const SettingId: Integer): Integer;
begin
  result := SoundTouchGetSetting(FHandle, SettingId);
end;

function TSoundTouch.SetSetting(const SettingId: Integer;
  const Value: Integer): Boolean;
begin
  result := SoundTouchSetSetting(FHandle, SettingId, Value);
end;

procedure TSoundTouch.TempoChanged;
begin
  SoundTouchsetTempo(FHandle, FTempo);
end;

var
  SoundTouchLibHandle: HINST;
  SoundTouchDLL: PAnsiChar = 'SoundTouch.DLL';

procedure InitDLL;
begin
  SoundTouchLibHandle := LoadLibrary(SoundTouchDLL);
  if SoundTouchLibHandle <> 0 then
  try
    SoundTouchCreateInstance        := GetProcAddress(SoundTouchLibHandle, PAnsiChar( 2)); //'soundtouch_createInstance');
    SoundTouchDestroyInstance       := GetProcAddress(SoundTouchLibHandle, PAnsiChar( 3)); //'soundtouch_destroyInstance');
    SoundTouchGetVersionString      := GetProcAddress(SoundTouchLibHandle, PAnsiChar( 7)); //'soundtouch_getVersionString');
    SoundTouchGetVersionId          := GetProcAddress(SoundTouchLibHandle, PAnsiChar( 6)); //'soundtouch_getVersionId');
    SoundTouchSetRate               := GetProcAddress(SoundTouchLibHandle, PAnsiChar(17)); //'soundtouch_setRate');
    SoundTouchSetTempo              := GetProcAddress(SoundTouchLibHandle, PAnsiChar(21)); //'soundtouch_setTempo');
    SoundTouchSetRateChange         := GetProcAddress(SoundTouchLibHandle, PAnsiChar(18)); //'soundtouch_setRateChange');
    SoundTouchSetTempoChange        := GetProcAddress(SoundTouchLibHandle, PAnsiChar(22)); //'soundtouch_setTempoChange');
    SoundTouchSetPitch              := GetProcAddress(SoundTouchLibHandle, PAnsiChar(14)); //'soundtouch_setPitch');
    SoundTouchSetPitchOctaves       := GetProcAddress(SoundTouchLibHandle, PAnsiChar(15)); //'soundtouch_setPitchOctaves');
    SoundTouchSetPitchSemiTones     := GetProcAddress(SoundTouchLibHandle, PAnsiChar(16)); //'soundtouch_setPitchSemiTones');
    SoundTouchSetChannels           := GetProcAddress(SoundTouchLibHandle, PAnsiChar(13)); //'soundtouch_setChannels');
    SoundTouchSetSampleRate         := GetProcAddress(SoundTouchLibHandle, PAnsiChar(19)); //'soundtouch_setSampleRate');
    SoundTouchFlush                 := GetProcAddress(SoundTouchLibHandle, PAnsiChar(4)); //'soundtouch_flush');
    SoundTouchPutSamples            := GetProcAddress(SoundTouchLibHandle, PAnsiChar(11)); //'soundtouch_putSamples');
    SoundTouchClear                 := GetProcAddress(SoundTouchLibHandle, PAnsiChar(1)); //'soundtouch_clear');
    SoundTouchSetSetting            := GetProcAddress(SoundTouchLibHandle, PAnsiChar(20)); //'soundtouch_SetSetting');
    SoundTouchGetSetting            := GetProcAddress(SoundTouchLibHandle, PAnsiChar(5)); //'soundtouch_setSetting');
    SoundTouchNumUnprocessedSamples := GetProcAddress(SoundTouchLibHandle, PAnsiChar(10)); //'soundtouch_numUnprocessedSamples');
    SoundTouchReceiveSamples        := GetProcAddress(SoundTouchLibHandle, PAnsiChar(12)); //'soundtouch_receiveSamples');
    SoundTouchNumSamples            := GetProcAddress(SoundTouchLibHandle, PAnsiChar(9)); //'soundtouch_numSamples');
    SoundTouchIsEmpty               := GetProcAddress(SoundTouchLibHandle, PAnsiChar(8)); //'soundtouch_isEmpty');

  except
    FreeLibrary(SoundTouchLibHandle);
    SoundTouchLibHandle := 0;
  end;
end;

procedure FreeDLL;
begin
  if SoundTouchLibHandle <> 0 then FreeLibrary(SoundTouchLibHandle);
end;

initialization
  InitDLL;

finalization
  FreeDLL;

end.
