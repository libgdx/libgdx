/*******************************************************************************
 * Copyright 2025 See AUTHORS file.
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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Efficient JSON parser that does minimal parsing to extract values matching specified patterns using a single pass.
 * 
 * <h4>Match</h4> Match objects, arrays, or field names (not field values) in the JSON:
 * <ul>
 * <li>{@code /} Path separator.
 * <li>{@code name} Matches field "name".
 * <li>{@code a,b,c} Matches fields "a", "b", or "c".
 * <li>{@code *} Matches one object, array, or field.
 * <li>{@code **} Matches zero or more objects, arrays, or fields.
 * </ul>
 * 
 * <h4>Capture</h4> Surround any match with parenthesis to capture that value.
 * <ul>
 * <li>{@code (name)} Captures field "name".
 * <li>{@code (a),b,(c,d)} Captures multiple fields.
 * <li>{@code (*)} or {@code (**)} Captures wildcard matches.
 * </ul>
 * 
 * <h4>Process</h4> Use {@code @} after a match to control when processors receive captured values.
 * <ul>
 * <li>{@code devices/(id,name)} Without {@code @} values are processed once at the end and only the first values matched are
 * captured.
 * <li>{@code devices/(id@,name@)} Process "id" and "name" as soon as each is captured.
 * <li>{@code devices/(id,name)@} Same as {@code (id@,name@)}.
 * <li>{@code devices@/(id,name)} Process "id" and "name" as a pair for each device.
 * <li>{@code *@/(id,name)} Any match can be annotated to process.
 * <li>{@code **@/(id,name)} All captures after {@code **@} are processed as soon as each is captured.
 * </ul>
 * 
 * <h4>Arrays</h4> Arrays are captured as-is:
 * <ul>
 * <li>{@code data/(items)} with <code>{data:{items:[1,2,3]}}</code> gives: <code>{items=[1,2,3]}</code>
 * </ul>
 * Use {@code []} in a capture to collect into an array rather than as a single value.
 * <ul>
 * <li><code>*&#47;(id)</code> with <code>{first:{id:1},second:{id:2}}</code> gives: <code>{id=1}</code> (parsing ends after first
 * match)
 * <li><code>*&#47;(id[])</code> gives: <code>{id=[1, 2]}</code> (all matches collected in an array)
 * </ul>
 * 
 * <h4>Examples</h4>
 * 
 * <code>{users:[{name:nate},{name:iva}]}</code><br>
 * Process each user name: <code>users@/(name)</code>
 * <ol>
 * <li>The JSON root is matched implicitly.
 * <li>{@code users} matches the <code>users:[</code> array. The field name and value are matched together.
 * <li>{@code @} processes captured values at the end of each array item.
 * <li>{@code (name)} captures the "name" field and value.
 * <li>Result: Processors are called with <code>nate</code> and again with <code>iva</code>.
 * </ol>
 * <p>
 * <code>{config:{port:8081}}</code><br>
 * Get the first port from "config" found at any depth: <code>**&#47;config/(port)</code><br>
 * Result: Processors are called with: <code>8081</code>
 * <p>
 * <code>{services:[{status:up},{status:down},{status:failed}]}</code><br>
 * Get all service statuses in an array: <code>services/*&#47;(status[])</code><br>
 * Result: Processors are called with: <code>[up, down, failed]</code>
 * <p>
 * <code>{items:[{id:123,type:cookies},{id:456,type:cake}]}</code><br>
 * Process each "id" and "type" pair: <code>items@/(id,type)</code><br>
 * Result: Since there are multiple captures, processors are called with an object: <code>{id=123,type=cookies}</code> and
 * <code>{id=456,type=cake}</code>
 * 
 * <h4>Escaping</h4> Use special characters <code>/,*@()[]',\</code> by surrounding them with single quotes.
 * <ul>
 * <li>{@code email/'moo@dog.com'} Uses "moo@dog.com" as a literal string.
 * <li>{@code words/'can''t'} Escape single quote with two single quotes.
 * </ul>
 * 
 * <h4>Keys</h4> Capture keys using {@code ()}.
 * <ul>
 * <li>{@code object/()} with <code>{object:{a:1,b:2,c:3}}</code> gives: <code>a</code> (parsing ends after first match)
 * <li>{@code object/()[]} gives: <code>[a,b,c]</code> (all keys collected in an array)
 * </ul>
 * 
 * <h4>Behavior notes</h4>
 * <ul>
 * <li>reject() prevents further matching at this level or deeper, useful for filtering.
 * <li>clear() discards unprocessed captured values.
 * <li>end() prevents further matching and ends parsing. stop() does the same but also clears.
 * <li>If not using {@code *@}, {@code **@}, or {@code []} parsing ends once all specified values are captured.
 * <li>A single capture before processing provides the value directly, multiple captures provide an object.
 * <li>A {@code ""} pattern captures the entire JSON document.
 * </ul>
 * @author Nathan Sweet */
public class JsonMatcher extends JsonSkimmer {
	static private final boolean debug = false;

	static private final int none = 0; // @off
	static final int match   = 0b000001;
	static final int process = 0b000010;
	static final int capture = 0b000100;
	static final int array   = 0b001000;
	static final int keys    = 0b010000;
	static final int single  = 0b100000; // @on

	Processor processor;
	Pattern[] patterns = new Pattern[0];
	int total;
	private boolean rejected;
	boolean stoppable = true;

	int depth, captured;
	char[] chars;
	final IntArray path = new IntArray();
	Pattern processPattern;

	/** This processor is invoked for all pattern matches, after per pattern processors and before {@link #process(JsonValue)}. */
	public void setProcessor (@Null Processor processor) {
		this.processor = processor;
	}

	/** Adds a pattern for value extraction.
	 * @return The new pattern's index. */
	public int addPattern (String pattern) {
		return addPattern(pattern, null);
	}

	/** Adds a pattern for value extraction. The processor is invoked only for this pattern's matches.
	 * @return The new pattern's index. */
	public int addPattern (String pattern, @Null Processor processor) {
		if (chars != null) throw new IllegalStateException();
		Pattern[] newPatterns = new Pattern[patterns.length + 1];
		System.arraycopy(patterns, 0, newPatterns, 0, patterns.length);
		Pattern newPattern;
		if (pattern.isEmpty()) {
			newPattern = new Pattern(new Node(new Match[0], false, null), processor);
			newPattern.captureRoot = true;
			newPattern.captureAll = true;
		} else
			newPattern = PatternParser.parse(this, pattern, processor);
		newPatterns[patterns.length] = newPattern;
		patterns = newPatterns;
		if (debug) System.out.println(newPattern);
		return patterns.length - 1;
	}

	@Override
	public void parse (char[] data, int offset, int length) {
		if (chars != null) throw new IllegalStateException();
		chars = data;
		try {
			super.parse(data, offset, length);
			for (Pattern pattern : patterns)
				process(pattern);
		} finally {
			for (Pattern pattern : patterns)
				pattern.reset();
			depth = 0;
			captured = 0;
			chars = null;
			path.clear();
		}
	}

	@Override
	protected void push (@Null JsonToken name, boolean object) {
		if (debug) debug(null, "push: " + name + ":" + (object ? "{}" : "[]") + ", depth: " + depth);
		if (name != null)
			path.add(name.start, name.length);
		else
			path.add(object ? 0 : 1, 0);
		if (depth == 0) {
			for (Pattern pattern : patterns)
				if (pattern.captureRoot) captureAllStart(pattern, single, null, object);
		} else {
			for (Pattern pattern : patterns) {
				if (pattern.captureAll) {
					if (debug) debug(pattern, "current: " + pattern.current + " CAPTURE ALL");
					JsonValue value = new JsonValue(object ? ValueType.object : ValueType.array);
					captureAllValue(pattern, name, value);
					pattern.stack.add(value);
				} else {
					Node node = pattern.current;
					if (debug) debug(pattern, "current: " + pattern.current + (depth <= node.dead ? "" : " DEAD"));
					if (depth <= node.dead) {
						Node next = node.next;
						int flags = next == null ? none : next.match(name);
						if (flags != none) {
							while (true) {
								if ((flags & process) != 0) {
									process(pattern); // Process anything previously captured.
									if (stop) return;
								}
								if ((flags & capture) != 0) { // Capture key, object, or array.
									if ((flags & keys) != 0)
										captureValue(pattern, flags, null, name.value());
									else
										captureAllStart(pattern, flags, name, object);
									break;
								}
								// Advance to next node(s).
								if (debug) debug(pattern, "NEXT: " + node.next + ", depth: " + depth);
								pattern.current = next;
								next.pop = depth;
								Node nextNext = next.next;
								if (!next.starStar || nextNext == null) break;
								flags = nextNext.match(name);
								if (flags == none) break;
								next = nextNext;
								if (debug) debug(pattern, "ALSO: " + next + ", depth: " + depth);
							}
						} else if (node.starStar) {
							if (debug) debug(pattern, "KEEP: " + node + ", depth: " + depth);
						} else if (node.backtrack != null) {
							if (debug) debug(pattern, "BACKTRACK: " + node.backtrack + ", depth: " + depth);
							pattern.current = node.backtrack;
						} else { // Can't match deeper.
							node.dead = depth;
							if (debug) debug(pattern, "DEAD: " + node + ", depth: " + depth);
						}
					}
				}
			}
		}
		depth++;
	}

	@Override
	protected void pop () {
		int nextDepth = depth - 1;
		if (debug) debug(null, "pop " + nextDepth);
		for (Pattern pattern : patterns) {
			if (pattern.captureAll) {
				pattern.stack.pop();
				if (pattern.stack.notEmpty()) break;
				if (debug) debug(pattern, "CAPTURE ALL END");
				pattern.captureAll = pattern.captureRoot;
				captured();
			}
			Node node = pattern.current;
			if (debug) debug(pattern,
				"current: " + pattern.current + " pop at " + node.pop + ", captured: " + pattern.capture.toJson(OutputType.minimal));
			while (true) {
				if (node.dead == nextDepth) node.dead = Integer.MAX_VALUE; // Reject can set dead at any level.
				if (node.pop != nextDepth) {
					if (node.processEach) {
						process(pattern);
						if (stop) return;
					}
					break;
				}
				if (node.processEach || node.processPop) {
					process(pattern);
					if (stop) return;
				}
				if (node.prev == null) break;
				node = node.prev;
				pattern.current = node;
			}
		}
		depth = nextDepth;
		path.size -= 2;
	}

	@Override
	protected void value (@Null JsonToken name, JsonToken value) {
		if (debug) debug(null, "value: " + name + "=" + value);
		for (Pattern pattern : patterns) {
			if (pattern.captureAll) {
				pattern.captured = true;
				captureAllValue(pattern, name, value.value());
			} else if (depth <= pattern.current.dead) {
				Node next = pattern.current.next;
				int flags = next == null ? none : next.match(name);
				if (flags != none) {
					while (true) {
						if ((flags & process) != 0) { // Process anything previously captured.
							process(pattern);
							if (stop) return;
						}
						if ((flags & capture) != 0) { // Capture key or value.
							if (debug) debug(pattern, "CAPTURE: " + name + "=" + value + " (" + (captured + 1) + "/" + total + ")");
							if ((flags & keys) != 0) {
								if (name != null) captureValue(pattern, flags, null, name.value());
							} else
								captureValue(pattern, flags, name, value.value());
							captured();
							if ((flags & process) != 0) {
								process(pattern);
								if (stop) return;
							}
							break;
						}
						// Try next node(s).
						Node nextNext = next.next;
						if (!next.starStar || nextNext == null) break;
						flags = nextNext.match(name);
						if (flags == none) break;
						next = nextNext;
						if (debug) debug(pattern, "ALSO TRY: " + next + ", depth: " + depth);
					}
				}
			}
		}
	}

	private void captureValue (Pattern pattern, int flags, @Null JsonToken name, JsonValue value) {
		JsonValue capture = pattern.capture;
		if ((flags & single) != 0) {
			capture.name = name == null ? null : name.toString();
			if ((flags & array) != 0) {
				if (!pattern.captured) capture.setType(ValueType.array);
				capture.addChild(value);
			} else
				capture.set(value);
		} else {
			String key = name == null ? "" : name.toString();
			if ((flags & array) != 0) {
				JsonValue array = capture.get(key);
				if (array == null) {
					array = new JsonValue(ValueType.array);
					capture.addChild(key, array);
				}
				array.addChild(value);
				value.parent = array;
			} else
				capture.setChild(key, value);
		}
		pattern.captured = true;
	}

	private void captured () {
		if (stoppable && ++captured == total) {
			if (debug) debug(null, "END PARSING");
			end();
		}
	}

	private void captureAllStart (Pattern pattern, int flags, @Null JsonToken name, boolean object) {
		ValueType type = object ? ValueType.object : ValueType.array;
		JsonValue capture;
		if ((flags & (single | array)) == single) { // single but not array.
			capture = pattern.capture;
			capture.setType(type);
		} else {
			capture = new JsonValue(type);
			captureValue(pattern, flags, name, capture);
		}
		pattern.stack.add(capture);
		pattern.captureAll = true;
		pattern.captured = true;
		if (debug) debug(pattern, "CAPTURE ALL BEGIN");
	}

	private void captureAllValue (Pattern pattern, @Null JsonToken name, JsonValue value) {
		if (pattern.stack.isEmpty()) // Capturing root with primitive.
			pattern.capture.set(value);
		else {
			JsonValue parent = pattern.stack.peek();
			if (name == null)
				parent.addChild(value);
			else
				parent.setChild(name.toString(), value);
		}
	}

	private void process (Pattern pattern) {
		if (!pattern.captured) return;
		JsonValue capture = pattern.capture;
		if (debug) debug(pattern, "PROCESS: " + capture.toJson(OutputType.minimal));
		rejected = false;
		processPattern = pattern;
		try {
			if (pattern.processor != null) {
				pattern.processor.process(capture);
				if (rejected) return;
			}
			if (processor != null) {
				processor.process(capture);
				if (rejected) return;
			}
			process(capture);
		} finally {
			pattern.clearCapture();
			processPattern = null;
		}
	}

	/** Called for all pattern matches, after any processors have been invoked. */
	protected void process (JsonValue value) {
	}

	/** 0 when not processing. */
	public int depth () {
		return depth;
	}

	/** Returns the index of the pattern currently being processed. -1 when not processing. */
	public int pattern () {
		for (int i = 0, n = patterns.length; i < n; i++)
			if (patterns[i] == processPattern) return i;
		return -1;
	}

	/** Causes parsing to stop as soon as possible, without any further processing. */
	@Override
	public void stop () {
		rejected = true;
		clearAll();
		super.stop();
	}

	/** Causes parsing to stop as soon as possible, processing any values already collected. */
	public void end () {
		super.stop();
	}

	/** Rejects the current pattern. Only valid during processing.
	 * @see #rejectAll() */
	public void reject () {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(processPattern, "REJECT" + (depth - 1));
		processPattern.current.dead = depth - 1;
		rejected = true;
	}

	/** Rejects a specific pattern. Only valid during processing.
	 * @see #rejectAll() */
	public void reject (int patternIndex) {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(patterns[patternIndex], "REJECT" + (depth - 1));
		patterns[patternIndex].current.dead = depth - 1;
		rejected = true;
	}

	/** Prevents further matching of any pattern at the current level or deeper. Matching resumes when parsing returns to a higher
	 * level. Only valid during processing. */
	public void rejectAll () {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(null, "REJECT ALL " + (depth - 1));
		int dead = depth - 1;
		for (Pattern pattern : patterns)
			pattern.current.dead = dead;
		rejected = true;
	}

	/** Clears any captured values for the current pattern. Only valid during processing. */
	public void clear () {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(processPattern, "CLEAR");
		processPattern.clearCapture();
	}

	/** Clears any captured values for the specified pattern. Only valid during processing. */
	public void clear (int patternIndex) {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(patterns[patternIndex], "CLEAR");
		patterns[patternIndex].clearCapture();
	}

	/** Clears any captured values for all patterns. Only valid during processing. */
	public void clearAll () {
		if (processPattern == null) throw new IllegalStateException();
		if (debug) debug(null, "CLEAR ALL");
		for (Pattern pattern : patterns)
			pattern.clearCapture();
	}

	/** Returns the current JSON path using "/" as separator. Anonymous objects use "{}", arrays "[]". "" when not processing. */
	public String path () {
		buffer.size = 0;
		for (int i = 0, n = path.size; i < n; i += 2) {
			if (i > 0) buffer.append('/');
			int start = path.get(i), length = path.get(i + 1);
			if (length == 0)
				buffer.append(start == 0 ? "{}" : "[]");
			else
				buffer.append(chars, start, length);
		}
		return buffer.toString();
	}

	/** Returns the last segment of the JSON path. "" when not processing.
	 * @see #path() */
	public String parent () {
		int n = path.size;
		if (n == 0) return "";
		int start = path.get(n - 2), length = path.get(n - 1);
		if (length == 0) return start == 0 ? "{}" : "[]";
		return new String(chars, start, length);
	}

	/** Returns the segment of the JSON path up the specified segments from the end, starting at 0, or "" if there aren't enough
	 * segments. "" when not processing.
	 * @see #path() */
	public String parent (int up) {
		int n = path.size, i = n - (up << 1);
		if (i < 2) return "";
		int start = path.get(i - 2), length = path.get(i - 1);
		if (length == 0) return start == 0 ? "{}" : "[]";
		return new String(chars, start, length);
	}

	/** @see PatternParser */
	Match newMatch (String name, boolean brackets, boolean at, boolean processEach, boolean valueCapture, boolean keyCapture,
		boolean star, boolean starStar) {

		int flags = match;
		if (at || processEach) flags |= process;
		if (valueCapture) {
			flags |= capture;
			total++;
			if (brackets) {
				flags |= array;
				stoppable = false;
			}
			if (processEach) flags |= single;
		}
		if (keyCapture) flags |= keys;
		if ((star || starStar) && (flags & process) != 0) stoppable = false;
		return new Match(name, flags, star, starStar);
	}

	/** @see PatternParser */
	Node newNode (Match[] matches, boolean processEach, Node backtrack, @Null Node prev) {
		Node node = new Node(matches, processEach, backtrack);
		if (prev == null) {
			// Use first node as root.
			if (node.starStar) return node;
			// Implicit root.
			prev = new Node(new Match[] {new Match(".", 0, false, false)}, false, null);
		}
		prev.next = node;
		prev.nextStarStar = node.starStar;
		node.prev = prev;
		return node;
	}

	/** @see PatternParser */
	Pattern newPattern (Node root, Processor processor) {
		// Mark matches as single if not [] or () and there's 1 capture since @. Captures after **@ are already marked.
		Node current = root;
		boolean multi = false;
		Match lastCapture = null;
		do {
			for (Match match : current.matches) {
				if ((match.flags & (capture | keys)) != 0) {
					if (lastCapture != null)
						multi = true;
					else
						lastCapture = match;
				}
				if ((match.flags & process) != 0) {
					if (!multi && lastCapture != null) lastCapture.flags |= single;
					multi = false;
					lastCapture = null;
				}
			}
			current = current.next;
		} while (current != null);
		if (!multi && lastCapture != null) lastCapture.flags |= single;

		return new Pattern(root, processor);
	}

	private void debug (Pattern pattern, String text) {
		for (int i = 0; i < depth; i++)
			System.out.print("  ");
		for (int i = 0, n = patterns.length; i < n; i++)
			if (patterns[i] == pattern) System.out.print("[" + i + "] ");
		System.out.println(text);
	}

	static class Pattern {
		final Node root;
		final Processor processor;
		final JsonValue capture = new JsonValue(ValueType.object);
		boolean captured, captureAll, captureRoot;
		final Array<JsonValue> stack = new Array();
		Node current;

		Pattern (Node root, Processor processor) {
			this.root = root;
			this.processor = processor;
			current = root;
		}

		void clearCapture () {
			captured = false;
			capture.name = null;
			capture.child = null;
			capture.last = null;
			capture.setType(ValueType.object);
			capture.size = 0;
		}

		void reset () {
			Node node = root;
			do {
				node.dead = Integer.MAX_VALUE;
				node = node.next;
			} while (node != null);
			clearCapture();
			captureAll = captureRoot;
			stack.clear();
			current = root;
		}

		public String toString () {
			if (!debug) return super.toString();
			return root.toString();
		}
	}

	static class Node {
		final Match[] matches;
		final boolean processEach;
		boolean processPop, starStar, nextStarStar;
		@Null Node prev, next, backtrack;
		int pop, dead = Integer.MAX_VALUE;

		Node (Match[] matches, boolean processEach, Node backtrack) {
			this.matches = matches;
			this.processEach = processEach;
			this.backtrack = backtrack;
			for (Match match : matches) {
				if (match.starStar) starStar = true;
				if ((match.flags & process) != 0) processPop = true;
			}
		}

		int match (@Null JsonToken name) {
			if (name != null) {
				for (Match match : matches)
					if (match.any || name.equalsString(match.name)) return match.flags;
			} else {
				for (Match match : matches)
					if (match.any) return match.flags;
			}
			return nextStarStar ? next.match(name) : none;
		}

		private void toString (StringBuilder buffer) {
			for (int i = 0, last = matches.length - 1; i <= last; i++) {
				matches[i].toString(buffer);
				if (i < last) buffer.append(',');
			}
			if (next != null) {
				buffer.append('/');
				next.toString(buffer);
			}
		}

		public String toString () {
			if (!debug) return super.toString();
			StringBuilder buffer = new StringBuilder();
			toString(buffer);
			return buffer.toString();
		}
	}

	static class Match {
		final String name;
		int flags;
		final boolean star, starStar, any;

		Match (String name, int flags, boolean star, boolean starStar) {
			this.name = name;
			this.flags = flags;
			this.star = star;
			this.starStar = starStar;
			any = star || starStar;
		}

		void toString (StringBuilder buffer) {
			if ((flags & capture) != 0) buffer.append('(');
			buffer.append(name);
			if ((flags & array) != 0) buffer.append("[]");
			if ((flags & process) != 0) buffer.append('@');
			if ((flags & capture) != 0) buffer.append(')');
			if ((flags & single) != 0) buffer.append('1');
		}

		public String toString () {
			if (!debug) return super.toString();
			StringBuilder buffer = new StringBuilder();
			toString(buffer);
			return buffer.toString();
		}
	}

	static public interface Processor {
		/** Called with captured values. */
		public void process (JsonValue value);
	}
}
