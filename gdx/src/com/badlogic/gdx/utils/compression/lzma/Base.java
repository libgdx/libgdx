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

package com.badlogic.gdx.utils.compression.lzma;

public class Base {
	public static final int kNumRepDistances = 4;
	public static final int kNumStates = 12;

	public static final int StateInit () {
		return 0;
	}

	public static final int StateUpdateChar (int index) {
		if (index < 4) return 0;
		if (index < 10) return index - 3;
		return index - 6;
	}

	public static final int StateUpdateMatch (int index) {
		return (index < 7 ? 7 : 10);
	}

	public static final int StateUpdateRep (int index) {
		return (index < 7 ? 8 : 11);
	}

	public static final int StateUpdateShortRep (int index) {
		return (index < 7 ? 9 : 11);
	}

	public static final boolean StateIsCharState (int index) {
		return index < 7;
	}

	public static final int kNumPosSlotBits = 6;
	public static final int kDicLogSizeMin = 0;
	// public static final int kDicLogSizeMax = 28;
	// public static final int kDistTableSizeMax = kDicLogSizeMax * 2;

	public static final int kNumLenToPosStatesBits = 2; // it's for speed optimization
	public static final int kNumLenToPosStates = 1 << kNumLenToPosStatesBits;

	public static final int kMatchMinLen = 2;

	public static final int GetLenToPosState (int len) {
		len -= kMatchMinLen;
		if (len < kNumLenToPosStates) return len;
		return (int)(kNumLenToPosStates - 1);
	}

	public static final int kNumAlignBits = 4;
	public static final int kAlignTableSize = 1 << kNumAlignBits;
	public static final int kAlignMask = (kAlignTableSize - 1);

	public static final int kStartPosModelIndex = 4;
	public static final int kEndPosModelIndex = 14;
	public static final int kNumPosModels = kEndPosModelIndex - kStartPosModelIndex;

	public static final int kNumFullDistances = 1 << (kEndPosModelIndex / 2);

	public static final int kNumLitPosStatesBitsEncodingMax = 4;
	public static final int kNumLitContextBitsMax = 8;

	public static final int kNumPosStatesBitsMax = 4;
	public static final int kNumPosStatesMax = (1 << kNumPosStatesBitsMax);
	public static final int kNumPosStatesBitsEncodingMax = 4;
	public static final int kNumPosStatesEncodingMax = (1 << kNumPosStatesBitsEncodingMax);

	public static final int kNumLowLenBits = 3;
	public static final int kNumMidLenBits = 3;
	public static final int kNumHighLenBits = 8;
	public static final int kNumLowLenSymbols = 1 << kNumLowLenBits;
	public static final int kNumMidLenSymbols = 1 << kNumMidLenBits;
	public static final int kNumLenSymbols = kNumLowLenSymbols + kNumMidLenSymbols + (1 << kNumHighLenBits);
	public static final int kMatchMaxLen = kMatchMinLen + kNumLenSymbols - 1;
}
