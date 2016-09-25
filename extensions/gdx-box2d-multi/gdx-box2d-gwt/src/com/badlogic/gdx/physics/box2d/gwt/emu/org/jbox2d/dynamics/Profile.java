/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.dynamics;

import java.util.List;

import org.jbox2d.common.MathUtils;

public class Profile {
  private static final int LONG_AVG_NUMS = 20;
  private static final float LONG_FRACTION = 1f / LONG_AVG_NUMS;
  private static final int SHORT_AVG_NUMS = 5;
  private static final float SHORT_FRACTION = 1f / SHORT_AVG_NUMS;

  public static class ProfileEntry {
    float longAvg;
    float shortAvg;
    float min;
    float max;
    float accum;

    public ProfileEntry() {
      min = Float.MAX_VALUE;
      max = -Float.MAX_VALUE;
    }

    public void record(float value) {
      longAvg = longAvg * (1 - LONG_FRACTION) + value * LONG_FRACTION;
      shortAvg = shortAvg * (1 - SHORT_FRACTION) + value * SHORT_FRACTION;
      min = MathUtils.min(value, min);
      max = MathUtils.max(value, max);
    }

    public void startAccum() {
      accum = 0;
    }

    public void accum(float value) {
      accum += value;
    }

    public void endAccum() {
      record(accum);
    }

    @Override
    public String toString() {
      return shortAvg + " (" + longAvg + ") [" + min + "," + max + "]";
    }
  }

  public final ProfileEntry step = new ProfileEntry();
  public final ProfileEntry stepInit = new ProfileEntry();
  public final ProfileEntry collide = new ProfileEntry();
  public final ProfileEntry solveParticleSystem = new ProfileEntry();
  public final ProfileEntry solve = new ProfileEntry();
  public final ProfileEntry solveInit = new ProfileEntry();
  public final ProfileEntry solveVelocity = new ProfileEntry();
  public final ProfileEntry solvePosition = new ProfileEntry();
  public final ProfileEntry broadphase = new ProfileEntry();
  public final ProfileEntry solveTOI = new ProfileEntry();

  public void toDebugStrings(List<String> strings) {
    strings.add("Profile:");
    strings.add(" step: " + step);
    strings.add("  init: " + stepInit);
    strings.add("  collide: " + collide);
    strings.add("  particles: " + solveParticleSystem);
    strings.add("  solve: " + solve);
    strings.add("   solveInit: " + solveInit);
    strings.add("   solveVelocity: " + solveVelocity);
    strings.add("   solvePosition: " + solvePosition);
    strings.add("   broadphase: " + broadphase);
    strings.add("  solveTOI: " + solveTOI);
  }
}
