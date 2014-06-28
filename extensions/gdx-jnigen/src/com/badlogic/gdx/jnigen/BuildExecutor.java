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

package com.badlogic.gdx.jnigen;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Executes an ant script and its targets or an Android NDK build. See {@link AntScriptGenerator}.
 * @author mzechner */
public class BuildExecutor {
	/** Execute the Ant script file with the given parameters.
	 * @param buildFile
	 * @param params
	 * @return whether the Ant succeeded */
	public static boolean executeAnt (String buildFile, String params) {
		FileDescriptor build = new FileDescriptor(buildFile);
		String ant = System.getProperty("os.name").contains("Windows") ? "ant.bat" : "ant";
		String command = ant + " -f \"" + build.file().getAbsolutePath() + "\" " + params;
		System.out.println("Executing '" + command + "'");
		return startProcess(command, build.parent().file());
	}

	/** Execute ndk-build in the given directory
	 * @param directory */
	public static void executeNdk (String directory) {
		FileDescriptor build = new FileDescriptor(directory);
		String command = "ndk-build";
		startProcess(command, build.file());
	}

	private static boolean startProcess (String command, File directory) {
		try {
			final Process process = new ProcessBuilder(command.split(" ")).redirectErrorStream(true).start();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run () {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					try {
						while ((line = reader.readLine()) != null) {
							// augment output with java file line references :D
							printFileLineNumber(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				private void printFileLineNumber (String line) {
					if (line.contains("warning") || line.contains("error")) {
						try {
							String fileName = getFileName(line);
							String error = getError(line);
							int lineNumber = getLineNumber(line) - 1;
							if (fileName != null && lineNumber >= 0) {
								FileDescriptor file = new FileDescriptor(fileName);
								if (file.exists()) {
									String[] content = file.readString().split("\n");
									if (lineNumber < content.length) {
										for (int i = lineNumber; i >= 0; i--) {
											String contentLine = content[i];
											if (contentLine.startsWith("//@line:")) {
												int javaLineNumber = Integer.parseInt(contentLine.split(":")[1].trim());
												System.out.flush();
												if (line.contains("warning")) {
													System.out.println("(" + file.nameWithoutExtension() + ".java:"
														+ (javaLineNumber + (lineNumber - i) - 1) + "): " + error + ", original: " + line);
													System.out.flush();
												} else {
													System.err.println("(" + file.nameWithoutExtension() + ".java:"
														+ (javaLineNumber + (lineNumber - i) - 1) + "): " + error + ", original: " + line);
													System.err.flush();
												}
												return;
											}
										}
									}
								} else {
									System.out.println(line);
								}
							}
						} catch (Throwable t) {
							System.out.println(line);
							// silent death...
						}
					} else {
						System.out.println(line);
					}
				}

				private String getFileName (String line) {
					Pattern pattern = Pattern.compile("(.*):([0-9])+:[0-9]+:");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					String fileName = matcher.groupCount() >= 2 ? matcher.group(1).trim() : null;
					if (fileName == null) return null;
					int index = fileName.indexOf(" ");
					if (index != -1)
						return fileName.substring(index).trim();
					else
						return fileName;
				}

				private String getError (String line) {
					Pattern pattern = Pattern.compile(":[0-9]+:[0-9]+:(.+)");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					return matcher.groupCount() >= 1 ? matcher.group(1).trim() : null;
				}

				private int getLineNumber (String line) {
					Pattern pattern = Pattern.compile(":([0-9]+):[0-9]+:");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					return matcher.groupCount() >= 1 ? Integer.parseInt(matcher.group(1)) : -1;
				}
			});
			t.setDaemon(true);
			t.start();
			process.waitFor();
			return process.exitValue() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
