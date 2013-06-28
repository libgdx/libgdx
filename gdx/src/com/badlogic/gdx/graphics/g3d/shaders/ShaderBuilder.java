package com.badlogic.gdx.graphics.g3d.shaders;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderBuilder {
	/** The part can only be present in the vertex shader. */
	public final static int VERTEX 	= 1 << 0;
	/** The part can only be present in the fragment shader. */
	public final static int FRAGMENT	= 1 << 1;
	/** The part can be present in either vertex or fragment shader (or both depending on the parts properties). */ 
	public final static int AUTO 		= -1;
	
	public static class Part {
		protected static int clazzCounter = 0;
		public final static int CLAZZ = ++clazzCounter; 
		
		public static Comparator<Part> modifierComparator = new Comparator<Part>() {
			@Override
			public int compare (Part arg0, Part arg1) {
				final int lhs = arg0.priority, rhs = arg1.priority;
				final int sl = lhs < 0 ? 2 : (lhs > 0 ? -2 : 0);
				final int sr = rhs < 0 ? 2 : (rhs > 0 ? -2 : 0);
				if (sl == sr)
					return lhs - rhs;
				return sl - sr;
			}
		};
		
		/** The CLAZZ value of the class this Part can be safely cast to (to avoid reflection). */
		public final int clazz;
		/** The unique name of this part, e.g. the variable name, function name, etc. */
		public final String name;
		/** Whether the part must be inside the main method of the shader. */ 
		public boolean inline; // FIXME change this name to something like inBody/local/global/whatever...
		/** In which shader location this part can be placed (VERTEX, FRAGMENT or AUTO). */
		public int location;
		/** Whether the part can be used in multiple locations. */
		public boolean transferable;
		/** The flags the materialMask must contain for this part to be valid. */
		public long materialFlags;
		/** The flags the vertexMask must contain for this part to be valid. */
		public long vertexFlags;
		/** The flags the userMask must contain for this part to be valid. */
		public long userFlags;
		/** The name of the shader input this part depends on. */
		public final String input;
		/** Whether the specified input (if any) must be valid for this part to be valid. */
		public boolean requireInput;
		/** The other part that this part modifies. */ 
		public final Part modifies;
		/** The priority for this modifier, 0=don't care, >0=beginning, <0=end. There can be only one modifier with the same
		 * non-zero priority for each part. Adding a modifier with the same nonzero priority will replace the previous one.
		 * The first modifier is always 1, the second is 2, and so on, the last modifier is always -1, the second last -2, etc.
		 * E.g. if you have a part that created a variable, it's first modifier initializes the variable. 
		 * Note that initially you can have multiple nonzero modifiers with the same value, the one to use will be chosen depending
		 * on the validation of the part (materialFlags, vertexFlags and userFlags or it's dependencies). */
		public int priority;
		/** Whether this Part must have at least one valid modifier with priority of 1. */
		public boolean requireInitializer;
		/** The code to create this part. */
		public String code;
		public String varying;
		public String setToVarying;
		public String setFromVarying;
		/** The modifiers that apply to this part. */
		protected final Array<Part> modifiers = new Array<Part>();
		/** The other parts this Part depends on. */
		protected final Array<Part> dependencies = new Array<Part>();
		protected final Array<Part> reverseDependencies = new Array<Part>();
		// Validation values (known before building, set during validate):
		/** For the current build process: the locations where this part is valid as a dependency */
		protected int valid;
		/** For the current build process: the locations where this part is created */
		protected int located;
		// Building values (set during building):
		/** Whether this part is currently being build. */
		protected int building;
		/** Whether this part and all applicable modifiers are created. */  
		protected int built;
		protected boolean applyingModifiers;
		
		public Part (int clazz, String name, boolean inline, int location, boolean transferable, long materialFlags,
			long vertexFlags, long userFlags, String input, boolean requireInput, Part modifies, int prioriy,
			boolean requireInitializer, String code, Part... dependencies) {
			
			if (inline && transferable)
				throw new GdxRuntimeException("An inline part cannot be transferable");
			if (transferable && MathUtils.isPowerOfTwo(location))
				throw new GdxRuntimeException("A transferable must be able to located on multiple locations");
			if (requireInput && (input == null || input.length() < 1))
				throw new GdxRuntimeException("Cannot require an empty input");
			if (modifies != null && !inline)
				throw new GdxRuntimeException("A modifier must be inline");
			
			this.clazz = clazz;
			this.name = name;
			this.inline = inline;
			this.location = location;
			this.transferable = transferable;
			this.materialFlags = materialFlags;
			this.vertexFlags = vertexFlags;
			this.userFlags = userFlags;
			this.input = input;
			this.requireInput = requireInput;
			this.modifies = modifies;
			this.priority = prioriy;
			this.requireInitializer = requireInitializer;
			this.code = code;
			
			if (modifies != null)
				modifies.addModifier(this);
			for (final Part dependency : dependencies) {
				if (!inline && dependency.inline)
					throw new GdxRuntimeException("Only an inline part can depend on another inline part");
				addDependency(dependency);
			}
		}
		
		private void addDependency(final Part dependency) {
			if (!dependencies.contains(dependency, true)) {
				dependencies.add(dependency);
				if (!dependency.reverseDependencies.contains(this, true))
					dependency.reverseDependencies.add(this);
			}
		}
		
		private void addModifier(final Part modifier) {
			if (!modifiers.contains(modifier, true))
				modifiers.add(modifier);
		}
		
		/** Checks whether this meets all requirements to be valid, does not check its dependencies.
		 * Override to have more control the comparison. */
		public boolean compare(final ShaderBuilder builder) {  
			if (!(((builder.materialMask & materialFlags) == materialFlags) &&   
				((builder.vertexMask & vertexFlags) == vertexFlags) &&   
		   	((builder.userMask & userFlags) == userFlags)))
				return false;
			if (requireInput) {
				BaseShader.Input inp = builder.shader.getInput(input);
				if (inp == null || !inp.compare(builder.materialMask, builder.vertexMask, builder.userMask))
					return false;
			}
			return true;
		}
		
		private boolean validating;
		/** Check if and where this part is valid, can only be called after a call to reset */
		public void validate(final ShaderBuilder builder) {
			if (validating) // FIXME need to re-validate if this happens?
				return;
			validating = true;
			if (valid != 0 && !doValidate(builder))
				invalidate();
			validating = false;
		}
		
		private boolean doValidate(final ShaderBuilder builder) {
			// First check this part to the builder
			if (!compare(builder))
				return false;
			// Next check all its dependencies, without them this part is also not valid
			for (final Part dependency : dependencies) {
				dependency.validate(builder);
				if ((valid &= dependency.valid) == 0)
					return false;
			}
			if (modifies != null) {
				// In the unlikely case that this part is not validated by the part it modifies, validate that part first
				modifies.validate(builder);
				// If the part that this part modifies is not valid, obviously this part is also not valid
				if (modifies.valid == 0)
					return false;
			}
			// Finally validate the modifiers
			for (final Part modifier : modifiers) {
				modifier.validate(builder);
				if (requireInitializer && !hasInitializer())
					return false;
			}
			// Note that at this point valid contains the locations where part CAN be located, next the modifiers must be taken
			// into account and the located value must be set.
			return valid != 0;
		}
		
		/** Check if this part has at least one valid modifier with priority = 1, can only be called after a call to validate */
		public boolean hasInitializer() {
			final int n = modifiers.size;
			for (int i = 0; i < n; i++) {
				final Part modifier = modifiers.get(i);
				if (modifier.priority != 1)
					return false;
				if (modifier.valid != 0)
					return true;
			}
			return false;
		}
		
		/** Resets this part, its dependencies and its modifiers to the state where it can used to start building. */
		public void reset() {
			valid = location;
			building = built = located = 0;
			modifiers.sort(modifierComparator);
			for (final Part part : dependencies)
				part.reset();
			for (final Part part : modifiers)
				part.reset();
		}
		
		/** Set this part to invalid, causing all its modifiers and parts that depends on it also to be set invalid. */
		public void invalidate() {
			valid = 0;
			for (final Part part : reverseDependencies)
				part.invalidate();
			for (final Part part : modifiers)
				part.invalidate();
		}
		
		/** @return: the other parts, this part depends on. */
		public Iterable<Part> getDependencies() {
			return dependencies;
		}
		/** @return: the other parts that depend on this part. */
		public Iterable<Part> getReverseDependencies() {
			return reverseDependencies;
		}
		/** @return: the other parts that modify this part. */
		public Iterable<Part> getModifiers() {
			return modifiers;
		}
		
		public static Part newInput(final String name, final String code) {
			return new Part(CLAZZ, name, false, ShaderBuilder.AUTO, true, 0, 0, 0, name, true, null, 0, false, code);
		}
		
		public static Part newVariable(final String name, final String type) {
			final Part result = new Part(CLAZZ, name, false, ShaderBuilder.AUTO, true, 0, 0, 0, null, false, null, 0, true, type+" "+name+";");
			result.varying = "varying "+type+" v_"+name+";";
			result.setFromVarying = name+" = v_"+name+";";
			result.setToVarying = "v_"+name+" = "+name+";";
			return result;
		}
		
		public static Part newModifier(final String name, final Part modifies, final int priority, final String code, final Part... dependencies) {
			return new Part(CLAZZ, name, true, ShaderBuilder.AUTO, false, 0, 0, 0, null, false, modifies, priority, false, code, dependencies);
		}
		
		public static Part newOutput(final String name, final int location) {
			if (!MathUtils.isPowerOfTwo(location))
				throw new GdxRuntimeException("An output must have a defined location");
			return new Part(CLAZZ, name, true, location, false, 0, 0, 0, null, false, null, 0, true, "");
		}
	}
	
	public long materialMask = -1;
	public long vertexMask = -1;
	public long userMask = -1;
	public BaseShader shader;
	//public Renderable renderable;
	public Array<Array<Part>> programs = new Array<Array<Part>>(2);
	
	public void addPart(final Part part, final int location) {
		int idx = firstBit(location);
		while (idx >= programs.size)
			programs.add(new Array<Part>());
		programs.get(idx).add(part);
	}
	
	public ShaderProgram build(final BaseShader shader, final Part... outputs) {
		this.shader = shader;
		for (Part part : outputs)
			part.reset();
		for (Part part : outputs)
			part.validate(this);
		for (Part part : outputs)
			build(part, part.location);
		
		final int n = programs.size;
		String vertex = "void main() {}\n";
		String fragment = "void main() {}\n";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			final Array<Part> program = programs.get(i);
			if (program == null)
				continue;
			sb.setLength(0);
			for (Part part : program) {
				if (!part.inline) {
					sb.append(part.code).append("\n");
					if (!MathUtils.isPowerOfTwo(part.located))
						sb.append(part.varying).append("\n");
				}
			}
			sb.append("void main() {").append("\n");
			for (Part part : program)
				if (!part.inline && !MathUtils.isPowerOfTwo(part.located) && i != firstBit(part.located))
					sb.append("\t").append(part.setFromVarying).append("\n");
			for (Part part : program)
				if (part.inline)
					sb.append("\t").append(part.code).append("\n");
			for (Part part : program)
				if (!part.inline && !MathUtils.isPowerOfTwo(part.located) && i != lastBit(part.located))
					sb.append("\t").append(part.setToVarying).append("\n");
			sb.append("}").append("\n");
			if (i == 0)
				vertex = sb.toString();
			else
				fragment =
					"#ifdef GL_ES\n"+ 
					"#define LOWP lowp\n"+ 
					"#define MED mediump\n"+ 
					"#define HIGH highp\n"+ 
					"precision mediump float;\n"+ 
					"#else\n"+
					"#define MED\n"+
					"#define LOWP\n"+
					"#endif\n\n"+
					sb.toString();
		}
		
		Gdx.app.log("Vertex","\n"+vertex+"\n");
		Gdx.app.log("Fragment","\n"+fragment+"\n");
		
		ShaderProgram program = new ShaderProgram(vertex, fragment);
		if (!program.isCompiled())
			Gdx.app.log("Error", program.getLog());
		return program;
	}
	
	public void build(final Part part, int location) {
		if (!MathUtils.isPowerOfTwo(location))
			throw new GdxRuntimeException("Non POT location");
		if (((part.valid & location) == 0) || ((part.located & location) != 0))
			return;
		part.located |= location;
		for (final Part dependency : part.dependencies) {
			build(dependency, location);
		}
		if (part.modifies != null)
			build(part.modifies, location);
		addPart(part, location);
		if (!part.applyingModifiers) {
			part.applyingModifiers = true;
			buildModifiers(part.modifiers.iterator());
			part.applyingModifiers = false;
		}
	}
	
	public void buildModifiers(final Iterator<Part> modifiers) {
		if (!modifiers.hasNext())
			return;
		Part part = null;
		int location = 1;
		do {
			final Part next = modifiers.next();
			if (next.valid == 0)
				continue;
			if (part != null && (part.priority == 0 || next.priority != part.priority))
				build(part, location = getBestLocation(location, part.modifies, part));
			part = next;
		} while(modifiers.hasNext());
		if (part != null)
			build(part, location = getBestLocation(location, part.modifies, part));
	}
	
	private int getBestLocation(int minimum, Part part, Part modifier) {
		final int common = (part.transferable ? part.valid : part.located) & modifier.valid;
		if (common == 0 || (common > 0 && common < minimum))
			return 0;
		while ((common & minimum) == 0) minimum <<= 1;
		return minimum;
	}
	
	private final int firstBit(final int value) {
		if (value == 0)
			return -1;
		int result = 0;
		while((value & (1<<result)) == 0) result++;
		return result;
	}
	
	private final int lastBit(final int value) {
		if (value == 0)
			return -1;
		int result = 31;
		while(result >= 0 && (value & (1<<result)) == 0) result--;
		return result;
	}
	
	private final int potOfFirstBit(final int value) {
		if (value == 0)
			return 0;
		int b = 1;
		while((value & b) != b) b <<= 1;
		return b;
	}
}
