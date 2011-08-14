/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.veryangryrobots.mobiles;

public class BaseShot extends GameObject {

	private float dx;
	private float dy;
	private float shotSpeed;

	public BaseShot () {
		super();
	}

	public void setShotSpeed (float shotSpeed) {
		this.shotSpeed = shotSpeed;
	}

	public void fire (float x, float y, float dx, float dy) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void update (float delta) {
		stateTime += delta;
		float n = shotSpeed * delta;
		x += dx * n;
		y += dy * n;
	}
}
