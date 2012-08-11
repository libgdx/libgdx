/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt.soundmanager2;

public class SMSound extends JavaScriptObject {
	protected SMSound () {
	}

	public native final void destruct () /*-{
														this.destruct();
														}-*/;

	public native final int getPosition () /*-{
														return this.position;
														}-*/;

	public native final void pause () /*-{
													this.pause();
													}-*/;

	public native final void play () /*-{
												if(this.loops) {
												var sound = this;		
												function loopSound(soundID) { 
												sound.play({onfinish:function(){ 
												loopSound(soundID); 
												}}); 
												}
												loopSound(this);
												} else {
												this.play();
												}
												}-*/;

	public native final void resume () /*-{
													this.resume();
													}-*/;

	public native final void stop () /*-{
												this.stop();
												}-*/;

	public native final void setVolume (int volume) /*-{
																	this.setVolume(volume);
																	}-*/;

	public native final int getVolume () /*-{
														return this.volume;
														}-*/;

	public native final void loops (int loops) /*-{
																this.loops = loops;
																}-*/;

	public native final int playState () /*-{
														return this.playState;
														}-*/;
}
