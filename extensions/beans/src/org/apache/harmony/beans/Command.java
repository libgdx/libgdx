/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.harmony.beans.internal.nls.Messages;
import org.xml.sax.Attributes;

import com.badlogic.gdx.beans.BeanInfo;
import com.badlogic.gdx.beans.Expression;
import com.badlogic.gdx.beans.IndexedPropertyDescriptor;
import com.badlogic.gdx.beans.IntrospectionException;
import com.badlogic.gdx.beans.Introspector;
import com.badlogic.gdx.beans.PropertyDescriptor;
import com.badlogic.gdx.beans.Statement;
import com.badlogic.gdx.beans.XMLDecoder;

public class Command {

	private static final int INITIALIZED = 0;

	private static final int CHILDREN_FILTERED = 1;

	private static final int COMMAND_EXECUTED = 2;

	private static final int CHILDREN_PROCESSED = 3;

	private String tagName; // tag name

	private Map<String, String> attrs; // set of attrs

	private String data; // string data

	// inner commands
	private Vector<Command> commands = new Vector<Command>();

	// arguments
	private Vector<Command> arguments = new Vector<Command>();

	// operations
	private Vector<Command> operations = new Vector<Command>();

	// additional arguments placed before others
	private Vector<Argument> auxArguments = new Vector<Argument>();

	private Argument result; // result argument

	private Object target; // target to execute a command on

	private String methodName; // method name

	private Command ctx; // context for command

	private int status; // commands

	private XMLDecoder decoder;

	// private int tabCount = 0;

	public Command (String tagName, Map<String, String> attrs) {
		this.tagName = tagName;
		this.attrs = attrs;
		this.status = initializeStatus(tagName);
	}

	public Command (XMLDecoder decoder, String tagName, Map<String, String> attrs) {
		this.decoder = decoder;
		this.tagName = tagName;
		this.attrs = attrs;
		this.status = initializeStatus(tagName);
	}

	// set data for command
	public void setData (String data) {
		this.data = data;
	}

	// set tab count to display log messages
	// public void setTabCount(int tabCount) {
	// this.tabCount = tabCount;
	// }

	// set context - upper level command
	public void setContext (Command ctx) {
		this.ctx = ctx;
	}

	// add child command
	public void addChild (Command cmd) {
		if (cmd != null) {
			cmd.setContext(this);
			commands.add(cmd);
		}
	}

	// remove child command
	public void removeChild (Command cmd) {
		if ((cmd != null) && commands.remove(cmd)) {
			cmd.setContext(null);
		}
	}

	// command status
	public int getStatus () {
		return status;
	}

	// check if one of arguments or operations is unresolved
	private boolean isResolved () {
		if (getStatus() < Command.CHILDREN_PROCESSED) {
			return false;
		}
		for (int i = 0; i < arguments.size(); ++i) {
			Command arg = arguments.elementAt(i);

			if (!arg.isResolved()) {
				return false;
			}
		}
		for (int j = 0; j < operations.size(); ++j) {
			Command opr = operations.elementAt(j);

			if (!opr.isResolved()) {
				return false;
			}
		}
		return true;
	}

	// execute command and return execution flags
	public int exec (Map<String, Command> references) throws Exception {
		// System.out.println("in exec() status = " + translateStatus(status) +
		// "...");
		if (status < Command.CHILDREN_PROCESSED) {
			if (status < Command.COMMAND_EXECUTED) {
				if (status < Command.CHILDREN_FILTERED) {
					status = doBeforeRun(references);
					// System.out.println("after doBeforeRun() status = " +
					// translateStatus(status));
				}
				if (status == Command.CHILDREN_FILTERED) {
					status = doRun(references);
					// System.out.println("after doRun() status = " +
					// translateStatus(status));
				}
			}
			if (status == Command.COMMAND_EXECUTED) {
				status = doAfterRun(references);
				// System.out.println("after doAfterRun() status = " +
				// translateStatus(status));
			}
		}
		// System.out.println("...out of exec() status = " +
		// translateStatus(status));
		return status;
	}

	// execute commands in backtrack order
	public boolean backtrack (Map<String, Command> references) throws Exception {
		for (int i = 0; i < arguments.size(); ++i) {
			Command arg = arguments.elementAt(i);
			arg.backtrack(references);
		}
		for (int i = 0; i < operations.size(); ++i) {
			Command opr = operations.elementAt(i);
			opr.backtrack(references);
		}
		if (status == Command.CHILDREN_FILTERED) {
			status = doRun(references);
		}
		if (status == Command.COMMAND_EXECUTED) {
			status = doAfterRun(references);
			return (getStatus() == Command.CHILDREN_PROCESSED);
		}
		return false;
	}

	// put command in one of two collections - arguments or operations
	private int doBeforeRun (Map<String, Command> references) throws Exception {
		if (status == Command.INITIALIZED) {
			for (int i = 0; i < commands.size(); ++i) {
				Command cmd = commands.elementAt(i);

				// XXX is this correct?
				if (cmd.isExecutable()) {
					arguments.add(cmd);
				} else {
					operations.add(cmd);
				}
			}
			return Command.CHILDREN_FILTERED;
		}
		return status;
	}

	// run command
	private int doRun (Map<String, Command> references) throws Exception {
		if (status == Command.CHILDREN_FILTERED) {
			if (isRoot()) {
				result = new Argument(decoder);
				// System.out.println("doRun(): result is decoder...");
				return Command.COMMAND_EXECUTED;
			}

			if (isNull()) {
				result = new Argument(null);
				// System.out.println("doRun(): result is null...");
				return Command.COMMAND_EXECUTED;
			}

			if (ctx != null && ctx.isArray() && (ctx.getResultValue() == null) && !isExecutable()) {
				// System.out.println("doRun(): context is array...");
				return status;
			}

			Object target = getTarget(references);
			if (target == null) {
				// System.out.println("doRun(): target is null...");
				return status;
			}
			// if (target instanceof Class) {
			// System.out.println("doRun(): target = " +
			// ((Class)target).getName());
			// } else {
			// System.out.println("doRun(): target = " +
			// target.getClass().getName());
			// }
			if (isReference()) {
				result = getReferencedArgument(references);
				// System.out.println("doRun(): reference - result is " +
				// result.getType());
			} else {
				String methodName = getMethodName(references);
				// System.out.println("doRun(): methodName = " +
				// methodName);
				Argument[] arguments = getArguments();
				if (arguments == null) {
					return status;
				}
				// for (Argument element : arguments) {
				// if (element != null) {
				// System.out.println("doRun(): arg [" + i + "] = "
				// + arguments[i].getType());
				// } else {
				// System.out.println("doRun(): arg [" + i + "] =
				// null");
				// }
				// }

				Expression expr = new Expression(target, methodName, getArgumentsValues());
				result = new Argument(expr.getValue());

				if (isPrimitiveClassName(getTagName())) {
					result.setType(getPrimitiveClass(tagName));
				}

				// System.out.println("doRun(): method call - result is " +
				// result.getType());
			}
			return Command.COMMAND_EXECUTED;
		}
		return status;
	}

	// run child commands
	private int doAfterRun (Map<String, Command> references) throws Exception {
		if (status == Command.COMMAND_EXECUTED) {
			// System.out.println("doAfterRun(): command " + getResultType() + "
			// processed...");
			Vector<Command> toBeRemoved = new Vector<Command>();
			try {
				Statement[] statements = null;

				for (int i = 0; i < operations.size(); ++i) {
					Command cmd = operations.elementAt(i);

					if (cmd.isArrayElement()) {

						if (cmd.isResolved()) {
							if (statements == null) {
								statements = new Statement[operations.size()];
							}
							statements[i] = new Statement(getResultValue(), "set", new Object[] {Integer.valueOf(i), //$NON-NLS-1$
								cmd.getResultValue()});
							if ((i + 1) == operations.size()) {
								for (int j = 0; j < operations.size(); ++j) {
									statements[j].execute();
								}
								toBeRemoved.addAll(operations);
							}
						} else {
							break;
						}

					} else {
						// since the call is Array.set()
						if (!isArray()) {
							cmd.setTarget(getResultValue());
						}
						cmd.exec(references);

						if (cmd.isResolved()) {
							// System.out.println("doAfterRun(): cmd = " +
							// cmd.methodName + " is resolved");
							toBeRemoved.add(cmd);
						} else {
							// System.out.println("doAfterRun(): cmd = " +
							// cmd.methodName + " is unresolved");
							break;
						}

					}

				}
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				operations.removeAll(toBeRemoved);
			}

			// if (operations.size() == 0) {
			// System.out.println("doAfterRun(): command " + getResultType()
			// + " completely processed.");
			// } else {
			// System.out.println("doAfterRun(): command " + getResultType()
			// + " contains incomplete " +
			// operations.size() + " commands.");
			// }
			return (operations.size() == 0) ? Command.CHILDREN_PROCESSED : status;
		}
		return status;
	}

	// Result accessors

	// Return result - Argument class
	public Argument getResult () {
		return result;
	}

	// Returns result value
	public Object getResultValue () {
		return (result != null) ? result.getValue() : null;
	}

	// Returns result type
	public Class<?> getResultType () throws ClassNotFoundException {
		return (result != null) ? result.getType() : null;
	}

	// accessors to XML tags and attrs
	public boolean hasAttr (String name) {
		return attrs.get(name) != null;
	}

	public String getAttr (String name) {
		return attrs.get(name);
	}

	public boolean isTag (String name) {
		return tagName.equals(name);
	}

	public boolean hasAttr (String name, String value) {
		return value.equals(attrs.get(name));
	}

	public String getTagName () {
		return tagName;
	}

	// Checks if the object is primitive - int, float, etc...
	private boolean isPrimitive () {
		return isPrimitiveClassName(tagName);
	}

	// Checks if the object is constructor
	private boolean isConstructor () {
		return isPrimitive() || !isStaticMethod() && !isMethod() && !isProperty() && !isField() && !isArray() && !isReference();
	}

	// Checks if the command is static method
	private boolean isStaticMethod () {
		return isTag("object") && hasAttr("method") || isTag("class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	// Checks if the command is public method
	private boolean isMethod () {
		return isTag("void") && (hasAttr("method") || hasAttr("index")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	// Check if the command relates to property - getter ot setter depends on
	// number of args
	private boolean isProperty () {
		return isTag("void") && hasAttr("property"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// Check if the command is field accessor
	private boolean isField () {
		return isTag("object") && hasAttr("field"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// Check if the command is array
	private boolean isArray () {
		return isTag("array"); //$NON-NLS-1$
	}

	// Check if the command is array element
	private boolean isArrayElement () {
		return (ctx != null) && (ctx.isArray()) && isExecutable();
	}

	private boolean isReference () {
		return hasAttr("idref"); //$NON-NLS-1$
	}

	// Check if the command is root object
	private boolean isRoot () {
		return isTag("java"); //$NON-NLS-1$
	}

	// Check if the command is null
	private boolean isNull () {
		return isTag("null"); //$NON-NLS-1$
	}

	// Checks if the command could generate object
	public boolean isExecutable () {
		boolean result = isTag("object") || isTag("void") && hasAttr("class") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			&& hasAttr("method") || isTag("array") || isPrimitive() //$NON-NLS-1$ //$NON-NLS-2$
			|| isTag("class") || isTag("null"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	private Argument getReferencedArgument (Map<String, Command> references) {
		return references.get(getAttr("idref")).getResult(); //$NON-NLS-1$
	}

	// get a target through tag and attrs analysis
	private Object getTarget (Map<String, Command> references) throws Exception {
		if (target == null) {
			if (isReference()) {
				Command cmd = references.get(getAttr("idref")); //$NON-NLS-1$
				target = (cmd != null) ? cmd.getResultValue() : null;
			} else if (isExecutable()) {
				String className = null;

				if (isPrimitive()) {
					className = getPrimitiveClassName(tagName);
				} else if (isTag("class")) { //$NON-NLS-1$
					className = getPrimitiveClassName(tagName);
				} else if (isConstructor() || isStaticMethod() || isField()) {
					className = getAttr("class"); //$NON-NLS-1$
				} else if (isArray()) {
					className = getAttr("class"); //$NON-NLS-1$
					Class<?> componentType = isPrimitiveClassName(className) ? getPrimitiveClass(className) : Class.forName(className,
						true, Thread.currentThread().getContextClassLoader());
					className = Array.newInstance(componentType, 0).getClass().getName();
				}

				if (className != null) {
					try {
						target = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
					} catch (ClassNotFoundException e) {
						target = Class.forName(className);
					}

					if (isField()) {
						String fieldName = getAttr("field"); //$NON-NLS-1$
						target = ((Class<?>)target).getField(fieldName);
					}
				} else {
					throw new Exception(Messages.getString("beans.42", className)); //$NON-NLS-1$
				}
			} else if (ctx.isArray()) {
				// target = ctx.getTarget(references);
				target = Class.forName("java.lang.reflect.Array"); //$NON-NLS-1$
			}
		}
		return target;
	}

	// set target to execute command on
	private void setTarget (Object target) {
		this.target = target;
	}

	// Return a method name of command
	private String getMethodName (Map<String, Command> references) throws NoSuchMethodException, IntrospectionException, Exception {
		if (methodName == null) {
			String methodValue = null;
			if (isTag("class")) { //$NON-NLS-1$
				addArgument(new Argument(String.class, data), 0);
				methodValue = "forName"; //$NON-NLS-1$
			} else if (isPrimitive()) {
				if (isTag("char")) { //$NON-NLS-1$
					if (data.length() != 1) {
						throw new IntrospectionException(Messages.getString("beans.43", //$NON-NLS-1$
							data));
					}
					addArgument(new Argument(char.class, Character.valueOf(data.charAt(0))), 0);
				} else {
					addArgument(new Argument(String.class, data), 0);
				}
				methodValue = "new"; //$NON-NLS-1$
			} else if (isConstructor() || hasAttr("method", "new")) { //$NON-NLS-1$ //$NON-NLS-2$
				methodValue = "new"; //$NON-NLS-1$
			} else if (isArray()) {
				methodValue = "new"; //$NON-NLS-1$
				int length = hasAttr("length") ? Integer //$NON-NLS-1$
					.parseInt(getAttr("length")) : getArgumentsNumber(); //$NON-NLS-1$
				copyArgumentsToCommands();
				addArgument(new Argument(int.class, Integer.valueOf(length)), 0);
			} else if (hasAttr("property")) { //$NON-NLS-1$
				String propertyValue = getAttr("property"); //$NON-NLS-1$
				if (hasAttr("index")) { //$NON-NLS-1$
					addArgument(new Argument(int.class, new Integer(getAttr("index"))), 0); //$NON-NLS-1$
				}

				BeanInfo beanInfo = Introspector.getBeanInfo(getTarget(references).getClass());
				PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

				boolean methodFound = false;
				Method method = null;
				for (PropertyDescriptor pd : pds) {
					if (propertyValue.equals(pd.getName())) {
						int argsNum = getArgumentsNumber();
						if (hasAttr("index")) { //$NON-NLS-1$
							IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor)pd;
							if (argsNum == 1) {
								method = ipd.getIndexedReadMethod();
							} else if (argsNum == 0) {
								method = ipd.getReadMethod();
							}
						} else {
							method = pd.getReadMethod();
						}

						if (method != null) {
							methodFound = matchMethodParams(method, references);
						}

						if (methodFound == false) {
							if (hasAttr("index")) { //$NON-NLS-1$
								IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor)pd;
								if (argsNum == 2) {
									method = ipd.getIndexedWriteMethod();
								} else if (argsNum == 1) {
									method = ipd.getWriteMethod();
								}
							} else {
								method = pd.getWriteMethod();
							}
						}

						if (method != null) {
							methodFound = matchMethodParams(method, references);
						}
					}
				}

				if (method == null) {
					throw new NoSuchMethodException(Messages.getString("beans.44", //$NON-NLS-1$
						propertyValue));
				}
				methodValue = method.getName();
			} else if (hasAttr("method")) { //$NON-NLS-1$
				if (hasAttr("index")) { //$NON-NLS-1$
					addArgument(new Argument(int.class, Integer.valueOf(getAttr("index"))), 0); //$NON-NLS-1$
				}
				methodValue = getAttr("method"); //$NON-NLS-1$
			} else if (hasAttr("index")) { //$NON-NLS-1$
				addArgument(new Argument(int.class, Integer.valueOf(getAttr("index"))), 0); //$NON-NLS-1$
				methodValue = getArgumentsNumber() > 1 ? "set" : "get"; //$NON-NLS-1$ //$NON-NLS-2$
				if (ctx.isArray()) {
					addArgument(ctx.getResult(), 0);
				}
			} else if (hasAttr("field")) { //$NON-NLS-1$
				addArgument(new Argument(Class.forName(getAttr("class"), true, //$NON-NLS-1$
					Thread.currentThread().getContextClassLoader())), 0);

				methodValue = "get"; //$NON-NLS-1$
			} else {
				throw new Exception(Messages.getString("beans.45")); //$NON-NLS-1$
			}
			methodName = methodValue;
		}
		return methodName;
	}

	// return a list of arguments as of Argument type
	private Argument[] getArguments () {
		Argument[] args = new Argument[auxArguments.size() + arguments.size()];

		for (int i = 0; i < auxArguments.size(); ++i) {
			args[i] = auxArguments.elementAt(i);
		}
		for (int j = 0; j < arguments.size(); ++j) {
			Command cmd = arguments.elementAt(j);

			if (cmd.getStatus() >= Command.COMMAND_EXECUTED) {
				args[auxArguments.size() + j] = cmd.getResult();
			} else {
				// System.out.println("arg: " + cmd.getResultValue());
				args = null;
				break;
			}
		}
		return args;
	}

	// return argument values
	private Object[] getArgumentsValues () {
		Argument[] args = getArguments();
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			result[i] = args[i].getValue();
		}
		return result;
	}

	// copy arguments to treat as commands
	private void copyArgumentsToCommands () {
		Iterator<Command> i = arguments.iterator();
		while (i.hasNext()) {
			Command cmd = i.next();
			cmd.status = Command.CHILDREN_FILTERED;
			operations.add(cmd);
		}
		arguments.clear();
	}

	// return number of arguments
	private int getArgumentsNumber () {
		return auxArguments.size() + arguments.size();
	}

	// return number of commands
	// private int getOperationsNumber() {
	// return operations.size();
	// }

	// add argument to the beginning of arguments
	private void addArgument (Argument argument, int idx) {
		auxArguments.insertElementAt(argument, idx);
	}

	// Check if the name of class is primitive
	public static boolean isPrimitiveClassName (String className) {
		return className.equalsIgnoreCase("boolean") //$NON-NLS-1$
			|| className.equalsIgnoreCase("byte") //$NON-NLS-1$
			|| className.equalsIgnoreCase("char") //$NON-NLS-1$
			|| className.equalsIgnoreCase("short") //$NON-NLS-1$
			|| className.equalsIgnoreCase("int") //$NON-NLS-1$
			|| className.equalsIgnoreCase("long") //$NON-NLS-1$
			|| className.equalsIgnoreCase("float") //$NON-NLS-1$
			|| className.equalsIgnoreCase("double") //$NON-NLS-1$
			|| className.equalsIgnoreCase("string"); //$NON-NLS-1$
	}

	// Transforms a primitive class name
	private String getPrimitiveClassName (String data) {
		String shortClassName = null;
		if (data.equals("int")) { //$NON-NLS-1$
			shortClassName = "Integer"; //$NON-NLS-1$
		} else if (data.equals("char")) { //$NON-NLS-1$
			shortClassName = "Character"; //$NON-NLS-1$
		} else {
			shortClassName = data.substring(0, 1).toUpperCase() + data.substring(1, data.length());
		}
		return "java.lang." + shortClassName; //$NON-NLS-1$
	}

	public static Class<?> getPrimitiveClass (String className) {
		Class<?> result = null;
		if (className.equals("boolean")) { //$NON-NLS-1$
			result = boolean.class;
		} else if (className.equals("byte")) { //$NON-NLS-1$
			result = byte.class;
		} else if (className.equals("char")) { //$NON-NLS-1$
			result = char.class;
		} else if (className.equals("short")) { //$NON-NLS-1$
			result = short.class;
		} else if (className.equals("int")) { //$NON-NLS-1$
			result = int.class;
		} else if (className.equals("long")) { //$NON-NLS-1$
			result = long.class;
		} else if (className.equals("float")) { //$NON-NLS-1$
			result = float.class;
		} else if (className.equals("double")) { //$NON-NLS-1$
			result = double.class;
		} else if (className.equals("string")) { //$NON-NLS-1$
			result = String.class;
		}
		return result;
	}

	private boolean matchMethodParams (Method method, Map<String, Command> references) {
		Class<?>[] paramTypes = method.getParameterTypes();
		Argument[] args = getArguments();
		if (args == null) {
			return false;
		}
		boolean result = true;
		if (paramTypes.length == args.length) {
			for (int j = 0; j < paramTypes.length; ++j) {
				// System.out.println("paramTypes[j] = " + paramTypes[j]);
				// System.out.println("args[j] = " + args[j].getType());

				boolean isAssignable = (args[j].getType() == null) ? !paramTypes[j].isPrimitive() : paramTypes[j]
					.isAssignableFrom(args[j].getType());

				// System.out.println("args[j] = " + args[j].getType());

				if (!isAssignable) {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	public static Map<String, String> parseAttrs (String tagName, Attributes attrs) {
		Map<String, String> result = new HashMap<String, String>();
		if (tagName.equals("object")) { //$NON-NLS-1$
			for (String name : objectAttrNames) {
				String value = attrs.getValue(name);
				if (value != null) {
					result.put(name, value);
				}
			}
		} else if (tagName.equals("void")) { //$NON-NLS-1$
			for (String name : voidAttrNames) {
				String value = attrs.getValue(name);
				if (value != null) {
					result.put(name, value);
				}
			}
		} else if (tagName.equals("array")) { //$NON-NLS-1$
			for (String name : arrayAttrNames) {
				String value = attrs.getValue(name);
				if (value != null) {
					result.put(name, value);
				}
			}
		} else if (tagName.equals("java")) { //$NON-NLS-1$
			for (String name : javaAttrNames) {
				String value = attrs.getValue(name);
				if (value != null) {
					result.put(name, value);
				}
			}
		}
		return result;
	}

	// Auxiliary logging with tabs functions
	public static void pr (String msg) {
		// System.out.print(msg);
	}

	public static void pr (int tabCount, String msg) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < tabCount; ++i) {
			result += '\t';
		}
		result += msg;
		// System.out.print(result);
	}

	public static void prn (String msg) {
		// System.out.println(msg);
	}

	public static void prn (int tabCount, String msg) {
		String result = ""; //$NON-NLS-1$
		for (int i = 0; i < tabCount; ++i) {
			result += '\t';
		}
		result += msg;
		// System.out.println(result);
	}

	public static void printAttrs (int tabCount, String tagName, Attributes attrs) {
		pr(tabCount, tabCount + ">in <" + tagName); //$NON-NLS-1$
		for (int i = 0; i < attrs.getLength(); ++i) {
			String attrName = attrs.getQName(i);
			String attrValue = attrs.getValue(i);
			pr(" " + attrName + "=" + attrValue); //$NON-NLS-1$ //$NON-NLS-2$
		}
		prn(">"); //$NON-NLS-1$
	}

	private static int initializeStatus (String tagName) {
		// return tagName.equals("java") ? Command.COMMAND_EXECUTED :
		// Command.INITIALIZED;
		return Command.INITIALIZED;
	}

	// private static String translateStatus(int status) {
	// String result = "unknown";
	// if(status == Command.INITIALIZED) {
	// result = "initialized";
	// } else if(status == Command.CHILDREN_FILTERED) {
	// result = "children filtered";
	// } else if(status == Command.COMMAND_EXECUTED) {
	// result = "executed";
	// } else if(status == Command.CHILDREN_PROCESSED) {
	// result = "children processed";
	// }
	// return result;
	// }

	private static final String[] objectAttrNames = {"id", "idref", "class", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"field", "method", "property", "index"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private static final String[] voidAttrNames = {"id", "class", "method", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"property", "index"}; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String[] arrayAttrNames = {"id", "class", "length"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private static final String[] javaAttrNames = {"version", "class"}; //$NON-NLS-1$ //$NON-NLS-2$
}
