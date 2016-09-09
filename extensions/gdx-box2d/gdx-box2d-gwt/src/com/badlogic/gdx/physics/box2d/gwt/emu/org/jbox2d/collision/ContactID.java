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
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 * 
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package org.jbox2d.collision;

/**
 * Contact ids to facilitate warm starting. Note: the ContactFeatures class is just embedded in here
 */
public class ContactID implements Comparable<ContactID> {

  public static enum Type {
    VERTEX, FACE
  }

  public byte indexA;
  public byte indexB;
  public byte typeA;
  public byte typeB;

  public int getKey() {
    return ((int) indexA) << 24 | ((int) indexB) << 16 | ((int) typeA) << 8 | ((int) typeB);
  }

  public boolean isEqual(final ContactID cid) {
    return getKey() == cid.getKey();
  }

  public ContactID() {}

  public ContactID(final ContactID c) {
    set(c);
  }

  public void set(final ContactID c) {
    indexA = c.indexA;
    indexB = c.indexB;
    typeA = c.typeA;
    typeB = c.typeB;
  }

  public void flip() {
    byte tempA = indexA;
    indexA = indexB;
    indexB = tempA;
    tempA = typeA;
    typeA = typeB;
    typeB = tempA;
  }

  /**
   * zeros out the data
   */
  public void zero() {
    indexA = 0;
    indexB = 0;
    typeA = 0;
    typeB = 0;
  }

  @Override
  public int compareTo(ContactID o) {
    return getKey() - o.getKey();
  }
}
