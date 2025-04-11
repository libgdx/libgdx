
package com.badlogic.gdx.utils;

public enum Justify {
	None, WrappedLinesBySpace, WrappedLinesByGlyph, OverflowedLinesBySpace, OverflowedLinesByGlyph, AllLinesBySpace, AllLinesByGlyph;

	public boolean matchChar (int ch) {
		switch (this) {
		case WrappedLinesBySpace:
		case OverflowedLinesBySpace:
		case AllLinesBySpace:
			return ch == ' ';
		case WrappedLinesByGlyph:
		case OverflowedLinesByGlyph:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}

	public boolean isNone () {
		return this == None;
	}

	/** Lines that were wrapped, excluding the last. */
	public boolean isWrappedLines () {
		switch (this) {
		case WrappedLinesBySpace:
		case WrappedLinesByGlyph:
			return true;
		}
		return false;
	}

	/** Lines that wrapped, including the last. */
	public boolean isOverflowedLines () {
		switch (this) {
		case OverflowedLinesBySpace:
		case OverflowedLinesByGlyph:
			return true;
		}
		return false;
	}

	/** All lines, regardless of wrapping. */
	public boolean isAllLines () {
		switch (this) {
		case AllLinesBySpace:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}

	/** Fill width by padding space glyphs. */
	public boolean isBySpace () {
		switch (this) {
		case WrappedLinesBySpace:
		case OverflowedLinesBySpace:
		case AllLinesBySpace:
			return true;
		}
		return false;
	}

	/** Fill width by padding all glyphs. */
	public boolean isByGlyph () {
		switch (this) {
		case WrappedLinesByGlyph:
		case OverflowedLinesByGlyph:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}
}
