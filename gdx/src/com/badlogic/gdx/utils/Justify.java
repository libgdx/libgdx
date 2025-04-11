
package com.badlogic.gdx.utils;

/** The text justification hint on the type of spacing and which lines to justify. */
public enum Justify {
	/** No justification is applied. */
	None,
	/** Space glyphs are padded for wrapped lines except the last. */
	ParagraphBySpace,
	/** Spaces inserted between glyphs for wrapped lines except the last. */
	ParagraphByGlyph,
	/** Space glyphs are padded for wrapped lines including the last. */
	WrappedLinesBySpace,
	/** Spaces inserted between glyphs on wrapped lines including the last. */
	WrappedLinesByGlyph,
	/** Space glyphs are padded for all lines. */
	AllLinesBySpace,
	/** Spaces inserted between glyphs for all lines. */
	AllLinesByGlyph;

	public boolean matchChar (int ch) {
		switch (this) {
		case ParagraphBySpace:
		case WrappedLinesBySpace:
		case AllLinesBySpace:
			return ch == ' ';
		case ParagraphByGlyph:
		case WrappedLinesByGlyph:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}

	public boolean isNone () {
		return this == None;
	}

	public boolean isParagraph () {
		switch (this) {
		case ParagraphBySpace:
		case ParagraphByGlyph:
			return true;
		}
		return false;
	}

	public boolean isWrappedLines () {
		switch (this) {
		case WrappedLinesBySpace:
		case WrappedLinesByGlyph:
			return true;
		}
		return false;
	}

	public boolean isAllLines () {
		switch (this) {
		case AllLinesBySpace:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}

	public boolean isBySpace () {
		switch (this) {
		case ParagraphBySpace:
		case WrappedLinesBySpace:
		case AllLinesBySpace:
			return true;
		}
		return false;
	}

	public boolean isByGlyph () {
		switch (this) {
		case ParagraphByGlyph:
		case WrappedLinesByGlyph:
		case AllLinesByGlyph:
			return true;
		}
		return false;
	}
}
