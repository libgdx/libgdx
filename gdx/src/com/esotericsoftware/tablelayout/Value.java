/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.esotericsoftware.tablelayout;

/** Base class for a table or cell property value. Values are provided a table or cell for context. Eg, the value may compute its
 * size taking into consideration the size of the table or the widget in the cell. Some values may be only valid for use with
 * either call.
 * @author Nathan Sweet */
abstract public class Value {
	/** Returns the value in the context of the specified table. */
	abstract public float get (Object table);

	/** Returns the value in the context of the specified cell. */
	abstract public float get (Cell cell);

	/** Returns the value in the context of a width for the specified table. */
	public float width (Object table) {
		return Toolkit.instance.width(get(table));
	}

	/** Returns the value in the context of a height for the specified table. */
	public float height (Object table) {
		return Toolkit.instance.height(get(table));
	}

	/** Returns the value in the context of a width for the specified cell. */
	public float width (Cell cell) {
		return Toolkit.instance.width(get(cell));
	}

	/** Returns the value in the context of a height for the specified cell. */
	public float height (Cell cell) {
		return Toolkit.instance.height(get(cell));
	}

	/** A value that is always zero. */
	static public final Value zero = new CellValue() {
		public float get (Cell cell) {
			return 0;
		}

		public float get (Object table) {
			return 0;
		}
	};

	/** A value that is only valid for use with a cell.
	 * @author Nathan Sweet */
	static abstract public class CellValue extends Value {
		public float get (Object table) {
			throw new UnsupportedOperationException("This value can only be used for a cell property.");
		}
	}

	/** A value that is valid for use with a table or a cell.
	 * @author Nathan Sweet */
	static abstract public class TableValue extends Value {
		public float get (Cell cell) {
			return get(cell.getLayout().getTable());
		}
	}

	/** A fixed value that is not computed each time it is used.
	 * @author Nathan Sweet */
	static public class FixedValue extends Value {
		private float value;

		public FixedValue (float value) {
			this.value = value;
		}

		public void set (float value) {
			this.value = value;
		}

		public float get (Object table) {
			return value;
		}

		public float get (Cell cell) {
			return value;
		}
	}

	/** Value for a cell that is the minWidth of the widget in the cell. */
	static public Value minWidth = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("minWidth can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getMinWidth(widget);
		}
	};

	/** Value for a cell that is the minHeight of the widget in the cell. */
	static public Value minHeight = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("minHeight can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getMinHeight(widget);
		}
	};

	/** Value for a cell that is the prefWidth of the widget in the cell. */
	static public Value prefWidth = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("prefWidth can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getPrefWidth(widget);
		}
	};

	/** Value for a cell that is the prefHeight of the widget in the cell. */
	static public Value prefHeight = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("prefHeight can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getPrefHeight(widget);
		}
	};

	/** Value for a cell that is the maxWidth of the widget in the cell. */
	static public Value maxWidth = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("maxWidth can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getMaxWidth(widget);
		}
	};

	/** Value for a cell that is the maxHeight of the widget in the cell. */
	static public Value maxHeight = new CellValue() {
		public float get (Cell cell) {
			if (cell == null) throw new RuntimeException("maxHeight can only be set on a cell property.");
			Object widget = cell.widget;
			if (widget == null) return 0;
			return Toolkit.instance.getMaxHeight(widget);
		}
	};

	/** Returns a value that is a percentage of the table's width. */
	static public Value percentWidth (final float percent) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getWidth(table) * percent;
			}
		};
	}

	/** Returns a value that is a percentage of the table's height. */
	static public Value percentHeight (final float percent) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getHeight(table) * percent;
			}
		};
	}

	/** Returns a value that is a percentage of the specified widget's width. */
	static public Value percentWidth (final float percent, final Object widget) {
		return new Value() {
			public float get (Cell cell) {
				return Toolkit.instance.getWidth(widget) * percent;
			}

			public float get (Object table) {
				return Toolkit.instance.getWidth(widget) * percent;
			}
		};
	}

	/** Returns a value that is a percentage of the specified widget's height. */
	static public Value percentHeight (final float percent, final Object widget) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getHeight(widget) * percent;
			}
		};
	}
}
