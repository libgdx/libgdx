/*
 * Copyright 2011 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
 // See http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/media/Sound.html

 package {
  import flash.external.ExternalInterface;
  import flash.media.Sound;
  import flash.display.Sprite;
  import flash.events.Event;
  import flash.events.IOErrorEvent;
  import flash.media.SoundChannel;
  import flash.media.SoundLoaderContext;
  import flash.net.URLRequest;
  import flash.media.SoundTransform;
  
  public class Voices extends Sprite
  {
    private var sounds:Array = [];
    private var volume:Array = [];
    private var panning:Array = [];
    private var channel:Array = [];
    private var loop:Array = [];
    private var domId:String;
    
    public function Voices() {
      ExternalInterface.marshallExceptions = true;
      
      domId = this.root.loaderInfo.parameters.id;
      log("***************************************************************************************************");
      log("Creating Voices '" + domId + "' ...");
      
      log("ExternalInterface.available = " + ExternalInterface.available);
      log("ExternalInterface.objectID = " + ExternalInterface.objectID);
      
      var result:Object;
      
      ExternalInterface.addCallback("createSound", createSound);
      log("addCallback(createSound) -> done");
      
      ExternalInterface.addCallback("playSound", playSound);
      log("addCallback(playSound) -> done");
      
      ExternalInterface.addCallback("stopSound", stopSound);
      log("addCallback(stopSound) -> done");
      
      ExternalInterface.addCallback("setLooping", setLooping);
      log("addCallback(setLooping) -> done");
  
      ExternalInterface.addCallback("setVolume", setVolume);
      log("addCallback(setVolume) -> done");
  
      ExternalInterface.addCallback("setPanning", setPanning);
      log("addCallback(setPanning) -> done");
  
      log("Voices created.");
  
      // notify JavaScript that we are ready
      result = ExternalInterface.call("document.VoicesMovie['" + domId + "'].ready");
      log("document.VoicesMovie['" + domId + "'].ready() -> " + result);
    }

    private function createSound(id:Number, url:String, checkPolicyFile:Boolean):void {
      var func:String = "createSound(id=" + id + ", url='" + url + "')";
      logStart(func);

      // default to false
      loop[id] = 0;

      var bufferTime:Number = 1000; // default
      var context:SoundLoaderContext = new SoundLoaderContext(bufferTime, checkPolicyFile);

      sounds[id] = new Sound();
      volume[id] = 1;
      panning[id] = 0;
      sounds[id].addEventListener(Event.COMPLETE, function(event:Event):void {
        var func:String = "Event.COMPLETE (=Sound Loaded) id=" + id;
        logStart(func);
        try {
          var result:Object = ExternalInterface.call("document.VoicesMovie['" + domId + "'].soundLoaded", id);
        } catch(e:Error) {
          logError(func, e);
        }
        log("document.VoicesMovie['" + domId + "'].soundLoaded(" + id + ") -> " + result + " // " + typeof result);
        logEnd(func);
      });
      // TODO handle IO_ERROR
//      sounds[id].addEventListener(IOErrorEvent.IO_ERROR, function(event:Event):void {
//        log("IOErrorEvent.IO_ERROR id=" + id);
//        var result:Object = ExternalInterface.call("document.VoicesMovie['" + domId + "'].soundLoaded", id);
//        log("document.VoicesMovie['" + domId + "'].soundLoaded(" + id + ") -> " + result);
//      });
      sounds[id].load(new URLRequest(url), context);
      logEnd(func);
    }
    
    private function playSound(id:Number):Boolean {
      var func:String = "playSound(id=" + id + ")";
      logStart(func);
      var success:Boolean;
      
      if (channel[id] != null) {
        channel[id].stop();
      }
      var transform:SoundTransform = new SoundTransform(volume[id], panning[id]);
      /*
       * Sound#play() returns null if there is no sound card, or if the maximum number of sound channels (32) has been reached.
       * See http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/media/Sound.html#play()
       */
      channel[id] = sounds[id].play(0, loop[id], transform);
      if (channel[id] == null) {
        log("No (more) channel(s) available to play sound id " + id);
        success = false;
      } else {
        channel[id].addEventListener(Event.SOUND_COMPLETE, function(event:Event):void {
          var func:String = "Event.SOUND_COMPLETE (=Playback Completed) id=" + id;
          logStart(func);
          var method:String = "document.VoicesMovie['" + domId + "'].playbackCompleted";
          try {
            var result:Object = ExternalInterface.call(method, id);
            logResult(method, result);
          } catch(e:Error) {
            logError(method, e)
          }
        });
        success = true;
      }
      logEnd(func);
      return success;
    }
    
    private function stopSound(id:Number):void {
      var func:String = "stopSound(id=" + id + ")";
      logStart(func);
      channel[id].stop();
      logEnd(func);
    }

    private function setLooping(id:Number, loopCount:Number):void {
      var func:String = "setLooping(id=" + id + ", loopCount=" + loopCount + ")";
      logStart(func);
      loop[id] = loopCount;
      logEnd(func);
    }
    
    private function setVolume(id:Number, volume:Number):void {
      var func:String = "setVolume(id=" + id + ", volume=" + volume + "%)";
      logStart(func);
      this.volume[id] = volume;
      logEnd(func);
    }
    
    private function setPanning(id:Number, panning:Number):void {
      var func:String = "setPanning(id=" + id + ", panning=" + panning + ")";
      logStart(func);
      this.panning[id] = panning;
      logEnd(func);
    }

    private function logStart(func:String):void {
      log(">> " + func);
    }

    private function logEnd(func:String):void {
      log("<< " + func);
    }

    private function logResult(method:String, result:Object):void {
      log(method + " -> " + (typeof result) + ": " + result);
    }
    
    private function logError(func:String, e:Error):void {
      log(func + " -> ERROR(" + e + "): " + e.getStackTrace());
    }
    
    private function log(text:String):void {
      //ExternalInterface.call("document.VoicesMovie['" + this.root.loaderInfo.parameters.id + "'].log", text);
    }
  }
}