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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/** Value placeholder, allowing the value to be computed on request. Values are provided an Object for context, the type of which
 * depends on how the value is used. This reduces the number of value instances that need to be created and reduces verbosity in
 * code that specifies values.
 * @author Nathan Sweet */
abstract public class Value {
	abstract public float get (Object context);

	/** A value that is always zero. */
	static public final Fixed zero = new Fixed(0);

	/** A value that is only valid for use with a cell.
	 * @author Nathan Sweet */
	static abstract public class CellValue extends Value {
		public float get (Object context) {
			if (!(context instanceof Cell)) throw new UnsupportedOperationException("This value can only be used for a cell.");
			return get((Cell)context);
		}

		abstract public float get (Cell cell);
	}

	/** A value that is valid for use with an actor.
	 * @author Nathan Sweet */
	static abstract public class ActorValue extends Value {
		public float get (Object context) {
			if (!(context instanceof Actor)) throw new UnsupportedOperationException("This value can only be used for an actor.");
			return get((Actor)context);
		}

		abstract public float get (Actor actor);
	}

	/** A fixed value that is not computed each time it is used.
	 * @author Nathan Sweet */
	static public class Fixed extends Value {
		private final float value;

		public Fixed (float value) {
			this.value = value;
		}

		public float get (Object context) {
			return value;
		}
	}

	/** CellValue that is the minWidth of the actor in the cell. */
	static public CellValue minWidth = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getMinWidth();
			return actor == null ? 0 : actor.getWidth();
		}
	};

	/** CellValue that is the minHeight of the actor in the cell. */
	static public CellValue minHeight = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getMinHeight();
			return actor == null ? 0 : actor.getHeight();
		}
	};

	/** CellValue that is the prefWidth of the actor in the cell. */
	static public CellValue prefWidth = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getPrefWidth();
			return actor == null ? 0 : actor.getWidth();

		}
	};

	/** CellValue that is the prefHeight of the actor in the cell. */
	static public CellValue prefHeight = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getPrefHeight();
			return actor == null ? 0 : actor.getHeight();
		}
	};

	/** CellValue that is the maxWidth of the actor in the cell. */
	static public CellValue maxWidth = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getMaxWidth();
			return actor == null ? 0 : actor.getWidth();
		}
	};

	/** CellValue that is the maxHeight of the actor in the cell. */
	static public CellValue maxHeight = new CellValue() {
		public float get (Cell cell) {
			Actor actor = cell.actor;
			if (actor instanceof Layout) return ((Layout)actor).getMaxHeight();
			return actor == null ? 0 : actor.getHeight();
		}
	};

	/** Returns an ActorValue that is a percentage of the actor's width. */
	static public ActorValue percentWidth (final float percent) {
		return new ActorValue() {
			public float get (Actor actor) {
				return actor.getWidth() * percent;
			}
		};
	}

	/** Returns an ActorValue that is a percentage of the actor's height. */
	static public ActorValue percentHeight (final float percent) {
		return new ActorValue() {
			public float get (Actor actor) {
				return actor.getHeight() * percent;
			}
		};
	}

	/** Returns a value that is a percentage of the specified actor's width. */
	static public Value percentWidth (final float percent, final Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		return new Value() {
			public float get (Object context) {
				return actor.getWidth() * percent;
			}
		};
	}

	/** Returns a value that is a percentage of the specified actor's height. */
	static public Value percentHeight (final float percent, final Actor actor) {
		if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
		return new Value() {
			public float get (Object context) {
				return actor.getHeight() * percent;
			}
		};
	}
}
