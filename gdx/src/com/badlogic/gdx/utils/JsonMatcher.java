
package com.badlogic.gdx.utils;

/** Efficient JSON parser that does minimal parsing to extract values matching specified patterns.
 * <h4>Pattern syntax</h4>
 * <ul>
 * <li>{@code /} Path separator.
 * <li>{@code *} Matches any single object or array.
 * <li>{@code @} Matches any single object or array, calls process() after each object or array.
 * <li>{@code **} Matches zero or more objects or arrays.
 * <li>{@code @@} Matches zero or more objects or arrays, calls process() after any capture.
 * <li>{@code [name]} Captures field "name" as a single value.
 * <li>{@code [name[]]} Captures field "name" into an array.
 * <li>{@code [name,id,title]} Captures multiple fields.
 * <li>{@code [*]} Captures first object, array, or field.
 * <li>{@code [*[]]} Captures all objects, arrays, or fields into an array.
 * <li>{@code [@]} Captures each object, array, or field, calls process() after each.
 * <li>{@code [@name]} Captures field "name", calls process() right away.
 * </ul>
 * 
 * <h4>Examples</h4>
 * 
 * <code>{users:[{name:nate},{name:iva}]}</code><br>
 * Process each user name: <code>users/@[name]</code>
 * <ol>
 * <li>The JSON root is matched implicitly.
 * <li>{@code users} matches the <code>users:[</code> array. The field name and value are matched together.
 * <li>{@code @} matches each object in the array. process() will be called at the end of each object.
 * <li>{@code [name]} captures the name field and value.
 * <li>Result: process() is called with <code>{name=nate}</code> and again with <code>{name=iva}</code>.
 * </ol>
 * <p>
 * <code>{config:{port:8081}}</code><br>
 * Get the first port from config found at any depth: <code>**&#47;config[port]</code><br>
 * Result: process() is called with: <code>{port=8081}</code>
 * <p>
 * <code>{services:[{status:up},{status:down},{status:failed}]}</code><br>
 * Get all service statuses in an array: <code>services/*[status[]]</code><br>
 * Result: process() is called with: <code>{status=[up, down, failed]}</code>
 * <p>
 * <code>{items:[{id:123,type:cookies},{id:456,type:cake}]}</code><br>
 * Process each id and name: <code>items/@[id,type]</code><br>
 * Result: process() is called with: <code>{id=123,type=cookies}</code> and <code>{id=456,type=cake}</code>
 * 
 * <h4>Arrays</h4>
 * 
 * Arrays are captured as-is:
 * <ul>
 * <li>{@code data[items]} with <code>{data:{items:[1,2,3]}}</code> gives: <code>{items=[1,2,3]}</code>
 * </ul>
 * To collect multiple values into an array, capture with {@code []} after the name:
 * <ul>
 * <li>{@code *[id]} with <code>{first:{id:1},second:{id:2}}</code> gives: <code>{id=1}</code> (parsing stops after first match)
 * <li>{@code *[id[]]} gives: <code>{id=[1, 2]}</code> (all matches in an array)
 * </ul>
 * 
 * <h4>Processing behavior</h4>
 * <ul>
 * <li>Calling reject() prevents any further matching at this level or deeper, making for easy filtering.
 * <li>Calling stop() prevents further matching and stops parsing.
 * <li>Without {@code @} (in any form), {@code [*]}, or {@code []} parsing stops once all specified values are captured.
 * <li>With {@code @} process() is called after the object or array that {@code @} matched.
 * <li>With multiple {@code @} process() is called for each capture, except for captures after the last {@code @}.
 * <li>With {@code @@} process() is called for each capture.
 * <li>With {@code [@]} process() is called for each object, array, or field.
 * <li>When capturing a whole object or array, other patterns will not match inside it.
 * <li>The key for unnamed values (eg array elements) is empty string ("").
 * <li>Value types are true, false, Long, Double, Array, ObjectMap, or null.
 * </ul>
 * @author Nathan Sweet */
public class JsonMatcher extends JsonSkimmer {
	static private final int none = 0, captureValue = 0b001, captureArray = 0b010, captureProcess = 0b100;

	Processor processor;
	final Array<Processor> processors = new Array();
	final Array<Node> roots = new Array();
	int total;
	boolean stoppable = true, rejected;

	Node[] active = new Node[1];
	final ObjectMap<String, Object> values = new ObjectMap();
	final Array captureAll = new Array();
	int depth, captured;
	char[] chars;
	final IntArray path = new IntArray();

	public void setProcessor (@Null Processor processor) {
		this.processor = processor;
	}

	/** Adds a pattern for value extraction. */
	public void addPattern (String pattern) {
		addPattern(pattern, null);
	}

	/** Adds a pattern for value extraction. */
	public void addPattern (String pattern, @Null Processor processor) {
		String test = pattern.replace("@@", "@");
		int atIndex = test.indexOf('@');
		boolean processEach = atIndex != test.lastIndexOf('@');
		if (atIndex == -1 && processor != null)
			throw new IllegalArgumentException("Invalid pattern, @ required for per pattern processor: " + pattern);

		Node root = null, current = null;
		for (String original : pattern.split("/")) {
			String part = original;

			// Capture.
			String[] capture = null;
			int[] captureType = null;
			boolean captureStar = false, captureAt = false;
			int brace = part.indexOf('[');
			if (brace != -1) {
				if (!part.endsWith("]")) throw new IllegalArgumentException("Invalid pattern, no ] at end: " + pattern);
				String capturePart = part.substring(brace + 1, part.length() - 1).trim();
				if (capturePart.isEmpty()) throw new IllegalArgumentException("Invalid pattern, empty capture: " + pattern);
				capture = part.substring(brace + 1, part.length() - 1).split(",");
				total += capture.length;
				captureType = new int[capture.length];
				for (int i = 0, n = capture.length; i < n; i++) {
					String value = capture[i].trim();
					if (value.endsWith("[]")) {
						value = value.substring(0, value.length() - 2);
						captureType[i] = captureArray;
						stoppable = false;
					} else
						captureType[i] = captureValue;
					if (value.equals("*")) {
						captureStar = true;
						capture = null;
						break;
					} else if (value.equals("@")) {
						captureAt = true;
						capture = null;
						break;
					} else if (value.startsWith("@")) {
						value = value.substring(1);
						captureType[i] |= captureProcess;
						stoppable = false;
					}
					capture[i] = value;
				}
				part = part.substring(0, brace);
			}

			// Add node.
			String name = part.trim();
			Node node = new Node(name, processEach, capture, captureType, captureStar, captureAt);
			if (root == null && (name.isEmpty() || node.zeroPlus))
				root = node;
			else {
				if (root == null)
					current = root = new Node(".", false, null, null, false, false);
				else if (name.isEmpty())
					throw new IllegalArgumentException("Invalid pattern, " + original + " missing name: " + pattern);
				current.next = node;
				current.nextZeroPlus = node.zeroPlus;
				node.prev = current;
			}
			processEach = node.processEach; // All subsequent captures process immediately.
			if (stoppable && (node.processPop || captureAt || processEach || captureStar)) stoppable = false;
			current = node;
		}
		if (total == 0) throw new IllegalArgumentException("Invalid pattern, no capture: " + pattern);
		roots.add(root);
		processors.add(processor);
	}

	@Override
	public void parse (char[] data, int offset, int length) {
		int n = roots.size;
		for (int i = 0; i < n; i++)
			roots.get(i).reset();
		if (active.length != n) active = new Node[n];
		System.arraycopy(roots.items, 0, active, 0, n);
		depth = 0;
		captured = 0;
		chars = data;
		try {
			super.parse(data, offset, length);
			if (values.notEmpty()) process(-1);
		} finally {
			chars = null;
			values.clear();
			captureAll.clear();
			path.clear();
		}
	}

	@Override
	protected void push (@Null JsonString name, boolean object) {
		if (name != null)
			path.add(name.start, name.length);
		else
			path.add(object ? 0 : 1, 0);
		if (depth > 0) {
			if (captureAll.notEmpty()) {
				Object value = object ? new ObjectMap() : new Array();
				captureAllValue(name, value);
				captureAll.add(value);
			} else {
				for (int i = 0, n = active.length; i < n; i++) {
					Node node = active[i];
					if (depth <= node.dead) {
						int capture = node.capture(name);
						if (capture != none) // Capture object or array.
							captureAllStart(node, capture, name, object);
						else if (node.next(name)) { // Advance to next nodes.
							Node next = node.next;
							while (true) {
								next.pop = depth;
								if (!next.zeroPlus || !next.next(name)) break;
								next = next.next;
							}
							active[i] = next;
						} else if (!node.zeroPlus) // Can't match deeper.
							node.dead = depth;
					}
				}
			}
		}
		depth++;
	}

	@Override
	protected void pop () {
		depth--;
		path.size -= 2;
		if (captureAll.notEmpty()) {
			captureAll.pop();
			if (captureAll.notEmpty()) return;
			captured();
		}
		boolean process = values.notEmpty();
		for (int i = 0, n = active.length; i < n; i++) {
			Node node = active[i];
			while (true) {
				if (node.pop != depth) {
					if (process && node.processEach) process(i);
					break;
				}
				if (process && (node.processEach || node.processPop)) process(i);
				node.dead = Integer.MAX_VALUE;
				if (node.prev == null) break;
				node = node.prev;
				active[i] = node;
			}
		}
	}

	@Override
	protected void value (@Null JsonString name, JsonString value) {
		if (captureAll.notEmpty()) {
			captureAllValue(name, value.decode());
			return;
		}
		for (int i = 0, n = active.length; i < n; i++) {
			Node node = active[i];
			if (depth <= node.dead) {
				int capture = node.capture(name);
				if (capture != none) {
					captureValue(node, capture, name, value.decode());
					captured();
					if ((capture & captureProcess) != 0) process(i);
				}
			}
		}
	}

	private void captureValue (Node node, int capture, @Null JsonString name, Object value) {
		String key = name == null ? "" : name.toString();
		if ((capture & captureArray) != 0) {
			if (values.get(key) instanceof Array array)
				array.add(value);
			else {
				var array = new Array();
				array.add(value);
				values.put(key, array);
			}
		} else
			values.put(key, value);
	}

	private void captured () {
		if (stoppable && ++captured == total) stop();
	}

	private void captureAllStart (Node node, int capture, @Null JsonString name, boolean object) {
		Object value;
		if (object && name == null)
			value = values;
		else {
			value = object ? new ObjectMap() : new Array();
			captureValue(node, capture, name, value);
		}
		captureAll.add(value);
	}

	private void captureAllValue (@Null JsonString name, Object value) {
		Object parent = captureAll.peek();
		if (name == null)
			((Array)parent).add(value);
		else
			((ObjectMap)parent).put(name.toString(), value);
	}

	private void process (int index) {
		try {
			rejected = false;
			if (index != -1) {
				Processor patternProcessor = processors.get(index);
				if (patternProcessor != null) {
					patternProcessor.process(values);
					if (rejected) return;
				}
			}
			if (processor != null) {
				processor.process(values);
				if (rejected) return;
			}
			process(values);
		} finally {
			values.clear();
		}
	}

	/** Called with captured values after any processors have been invoked.
	 * @param map Reused after this method returns. */
	protected void process (ObjectMap<String, Object> map) {
	}

	/** When called during processing, no further matches will occur at this level or deeper. Matching is resumed once parsing goes
	 * below this level. */
	public void reject () {
		rejected = true;
		int dead = depth - 1;
		for (int i = 0, n = active.length; i < n; i++)
			active[i].dead = dead;
	}

	@Override
	public void stop () {
		rejected = true;
		super.stop();
	}

	public int depth () {
		return depth;
	}

	/** Returns the current JSON path using "/" as separator. Anonymous objects use "{}", arrays "[]". Invalid when not parsing. */
	public String path () {
		buffer.length = 0;
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

	/** Returns the last segment of the JSON path. Invalid when not parsing.
	 * @see #path() */
	public String parent () {
		int n = path.size;
		if (n == 0) return "";
		int start = path.get(n - 2), length = path.get(n - 1);
		if (length == 0) return start == 0 ? "{}" : "[]";
		return new String(chars, start, length);
	}

	/** Returns the segment of the JSON path up the specified segments from the end, or "" if there aren't enough segments. Invalid
	 * when not parsing.
	 * @see #path() */
	public String parent (int up) {
		int n = path.size, i = n - (up << 1);
		if (i < 0) return "";
		int start = path.get(i - 2), length = path.get(i - 1);
		if (length == 0) return start == 0 ? "{}" : "[]";
		return new String(chars, start, length);
	}

	static private class Node {
		private final String name;
		final boolean processPop, processEach, zeroPlus;
		private final boolean anyNode;
		private final int anyCapture;
		private final @Null String[] capture;
		private final @Null int[] captureType;
		@Null Node prev, next;
		boolean nextZeroPlus;
		int pop, dead;

		Node (String name, boolean processEach, @Null String[] capture, int[] captureType, boolean captureStar, boolean captureAt) {
			this.name = name;
			this.capture = capture;
			boolean star = name.equals("*"), starStar = name.equals("**"), at = name.equals("@"), atAt = name.equals("@@");
			processPop = at;
			if (!processEach) processEach = atAt || captureAt;
			this.processEach = processEach;
			if (captureType != null && (atAt || processEach)) {
				for (int i = 0, n = captureType.length; i < n; i++)
					captureType[i] |= captureProcess;
			}
			zeroPlus = starStar || atAt;
			anyNode = star || starStar || at || atAt;
			if (captureStar || captureAt) {
				anyCapture = captureType[0];
				this.captureType = null;
			} else {
				anyCapture = 0;
				this.captureType = captureType;
			}
		}

		void reset () {
			dead = Integer.MAX_VALUE;
			if (next != null) next.reset();
		}

		int capture (@Null JsonString name) {
			if (anyCapture != 0) return anyCapture;
			if (name != null && capture != null) {
				for (int i = 0, n = capture.length; i < n; i++)
					if (name.equalsString(capture[i])) return captureType[i];
			}
			if (nextZeroPlus) return next.capture(name);
			return none;
		}

		boolean next (@Null JsonString name) {
			return next != null && (next.anyNode || (name != null && name.equalsString(next.name)));
		}
	}

	static public interface Processor {
		/** Called with captured values.
		 * @param map Reused after this method returns. */
		public void process (ObjectMap<String, Object> map);
	}
}
