package com.badlogic.gdx;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Given some parameters, this will create or updates all the projects for
 * a libgdx application targeting multiple platforms. Fetches
 * the latest nightlies from the build server.
 * 
 * @author mzechner
 */
public class GdxSetup {
	static final String REPL_PROJECT_NAME = "${name}";
	static final String REPL_PACKAGE_NAME = "${package}";
	static final String REPL_MAIN_CLASS = "${main}";
	static final String NIGHTLIES = "http://libgdx.badlogicgames.com/nightlies/dist/";
	
	String workspace;
	String projectName;
	String packageName;
	String mainClass;
	
	public GdxSetup(String workspace, String projectName, String packageName, String mainClass) {
		this.workspace = workspace.replace('\\', '/');
		this.workspace = this.workspace.endsWith("/")? this.workspace: this.workspace + "/";
		this.projectName = projectName;
		this.packageName = packageName;
		this.mainClass = mainClass;
	}
	
	public void create() {
		log("Creating core project");
		String corePath = workspace + projectName;
		String desktopPath = workspace + projectName + "-desktop";
		String androidPath = workspace + projectName + "-android";
		String htmlPath = workspace + projectName + "-html";
		
		executeTasks(new Task[] {
			new MkDir(corePath),
			new CopyResource("core/.classpath", corePath + "/.classpath"),
			new CopyResource("core/.project", corePath + "/.project"),
			new MkDir(corePath + "/.settings"),
			new CopyResource("core/org.eclipse.jdt.core.prefs", corePath + "/.settings/org.eclipse.jdt.core.prefs"),
			new MkDir(corePath + "/src/" + packageName.replace('.', '/')),
			new CopyResource("core/Demo.java.res", corePath + "/src/" + packageName.replace('.', '/') + "/" + mainClass + ".java"),
			new CopyResource("core/Demo.gwt.xml", corePath + "/src/" + packageName.replace('.', '/') + "/" + mainClass + ".gwt.xml"),
			new MkDir(corePath + "/lib"),
			new Download(NIGHTLIES + "gdx.jar", corePath + "/lib/gdx.jar"),
			new Download(NIGHTLIES + "gdx-natives.jar", corePath + "/lib/gdx-natives.jar"),
			new Download(NIGHTLIES + "/sources/gdx-sources.jar", corePath + "/lib/gdx-sources.jar"),
			new Download(NIGHTLIES + "gdx-backend-lwjgl.jar", corePath + "/lib/gdx-backend-lwjgl.jar"),
			new Download(NIGHTLIES + "gdx-backend-lwjgl-natives.jar", corePath + "/lib/gdx-backend-lwjgl-natives.jar"),
			new Download(NIGHTLIES + "gdx-backend-android.jar", corePath + "/lib/gdx-backend-android.jar"),
			new Download(NIGHTLIES + "gdx-backend-gwt.jar", corePath + "/lib/gdx-backend-gwt.jar"),
			new Download(NIGHTLIES + "/sources/gdx-backend-gwt-sources.jar", corePath + "/lib/gdx-backend-gwt-sources.jar"),
		});
		
		executeTasks(new Task[] {
			new MkDir(desktopPath),
			new CopyResource("desktop/.classpath", desktopPath + "/.classpath"),
			new CopyResource("desktop/.project", desktopPath + "/.project"),
			new MkDir(desktopPath + "/.settings"),
			new CopyResource("desktop/org.eclipse.jdt.core.prefs", desktopPath + "/.settings/org.eclipse.jdt.core.prefs"),
			new MkDir(desktopPath + "/src/" + packageName.replace('.', '/')),
			new CopyResource("desktop/DemoDesktop.java.res", desktopPath + "/src/" + packageName.replace('.', '/') + "/" + mainClass + "Desktop.java"),
		});
		
		executeTasks(new Task[] {
			new MkDir(androidPath),
			new CopyResource("android/.classpath", androidPath + "/.classpath"),
			new CopyResource("android/.project", androidPath + "/.project"),
			new CopyResource("android/AndroidManifest.xml", androidPath + "/AndroidManifest.xml"),
			new CopyResource("android/proguard-project.txt", androidPath + "/proguard-project.txt"),
			new CopyResource("android/project.properties", androidPath + "/project.properties"),
			new MkDir(androidPath + "/assets"),
			new MkDir(androidPath + "/libs/armeabi"),
			new MkDir(androidPath + "/libs/armeabi-v7a"),
			new Download(NIGHTLIES + "/armeabi/libgdx.so", androidPath + "/libs/armeabi/libgdx.so"),
			new Download(NIGHTLIES + "/armeabi/libandroidgl20.so", androidPath + "/libs/armeabi/libandroidgl20.so"),
			new Download(NIGHTLIES + "/armeabi-v7a/libgdx.so", androidPath + "/libs/armeabi-v7a/libgdx.so"),
			new Download(NIGHTLIES + "/armeabi-v7a/libandroidgl20.so", androidPath + "/libs/armeabi-v7a/libandroidgl20.so"),
			new MkDir(androidPath + "/src/" + packageName.replace('.', '/')),
			new CopyResource("android/DemoAndroid.java.res", androidPath + "/src/" + packageName.replace('.', '/') + "/" + mainClass + "Android.java"),
			new MkDir(androidPath + "/res/layout"),
			new CopyResource("android/layout/main.xml", androidPath + "/res/layout/main.xml"),
			new MkDir(androidPath + "/res/values"),
			new CopyResource("android/values/strings.xml", androidPath + "/res/values/strings.xml"),
			new MkDir(androidPath + "/res/drawable-ldpi"),
			new CopyResource("android/drawable-ldpi/ic_launcher.png", androidPath + "/res/drawable-ldpi/ic_launcher.png"),
			new MkDir(androidPath + "/res/drawable-mdpi"),
			new CopyResource("android/drawable-mdpi/ic_launcher.png", androidPath + "/res/drawable-mdpi/ic_launcher.png"),
			new MkDir(androidPath + "/res/drawable-hdpi"),
			new CopyResource("android/drawable-hdpi/ic_launcher.png", androidPath + "/res/drawable-hdpi/ic_launcher.png"),
			new MkDir(androidPath + "/res/drawable-xhdpi"),
			new CopyResource("android/drawable-xhdpi/ic_launcher.png", androidPath + "/res/drawable-xhdpi/ic_launcher.png"),
		});
		
		executeTasks(new Task[] {
			new MkDir(htmlPath),
			new CopyResource("html/.classpath", htmlPath + "/.classpath"),
			new CopyResource("html/.project", htmlPath + "/.project"),
			new MkDir(htmlPath + "/.settings"),
			new CopyResource("html/com.google.gdt.eclipse.core.prefs", htmlPath + "/.settings/com.google.gdt.eclipse.core.prefs"),
			new CopyResource("html/com.google.gwt.eclipse.core.prefs", htmlPath + "/.settings/com.google.gwt.eclipse.core.prefs"),
			new MkDir(htmlPath + "/src/" + packageName.replace('.', '/') + "/client"),
			new CopyResource("html/DemoHtml.gwt.xml", htmlPath + "/src/" + packageName.replace('.', '/') + "/" + mainClass + "Html.gwt.xml"),
			new CopyResource("html/DemoHtml.java.res", htmlPath + "/src/" + packageName.replace('.', '/') + "/client/" + mainClass + "Html.java"),
			new MkDir(htmlPath + "/war/"),
			new MkDir(htmlPath + "/war/WEB-INF/lib"),
			new CopyResource("html/web.xml", htmlPath + "/war/WEB-INF/web.xml"),
			new CopyResource("html/assets.txt", htmlPath + "/war/assets.txt"),
			new CopyResource("html/index.html", htmlPath + "/war/index.html"),
		});
	}
	
	public void update() {
		log("Updating core project");
		String corePath = workspace + projectName;
		String androidPath = workspace + projectName + "-android";
		
		executeTasks(new Task[] {
			new Download(NIGHTLIES + "gdx.jar", corePath + "/lib/gdx.jar"),
			new Download(NIGHTLIES + "gdx-natives.jar", corePath + "/lib/gdx-natives.jar"),
			new Download(NIGHTLIES + "/sources/gdx-sources.jar", corePath + "/lib/gdx-sources.jar"),
			new Download(NIGHTLIES + "gdx-backend-lwjgl.jar", corePath + "/lib/gdx-backend-lwjgl.jar"),
			new Download(NIGHTLIES + "gdx-backend-lwjgl-natives.jar", corePath + "/lib/gdx-backend-lwjgl-natives.jar"),
			new Download(NIGHTLIES + "gdx-backend-android.jar", corePath + "/lib/gdx-backend-android.jar"),
			new Download(NIGHTLIES + "gdx-backend-gwt.jar", corePath + "/lib/gdx-backend-gwt.jar"),
			new Download(NIGHTLIES + "/sources/gdx-backend-gwt-sources.jar", corePath + "/lib/gdx-backend-gwt-sources.jar"),
		});
		
		log("Updating android project");
		executeTasks(new Task[] {
			new Download(NIGHTLIES + "/armeabi/libgdx.so", androidPath + "/libs/armeabi/libgdx.so"),
			new Download(NIGHTLIES + "/armeabi/libandroidgl20.so", androidPath + "/libs/armeabi/libandroidgl20.so"),
			new Download(NIGHTLIES + "/armeabi-v7a/libgdx.so", androidPath + "/libs/armeabi-v7a/libgdx.so"),
			new Download(NIGHTLIES + "/armeabi-v7a/libandroidgl20.so", androidPath + "/libs/armeabi-v7a/libandroidgl20.so"),
		});
	}
	
	private void executeTasks(Task[] tasks) {
		for(Task task: tasks) {
			task.execute();
		}
	}
	
	interface Task {
		public void execute();
	}
	
	class CopyResource implements Task {
		static final String classpath = "/com/badlogic/gdx/resources/";
		public String source;
		public String target;
		
		public CopyResource(String source, String target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void execute () {
			log("Copying " + source + " to " + target);
			try {
				if (this.source.endsWith(".png")) {
					write(new File(target), GdxSetup.class.getResourceAsStream(classpath + source));
				} else {
					String resource = read(GdxSetup.class.getResourceAsStream(classpath + source));
					resource = resource.replace(REPL_MAIN_CLASS, mainClass);
					resource = resource.replace(REPL_PACKAGE_NAME, packageName);
					resource = resource.replace(REPL_PROJECT_NAME, projectName);
					write(new File(target), new ByteArrayInputStream(resource.getBytes("UTF-8")));					
				}

			} catch (Throwable e) {
				throw new RuntimeException("Couldn't copy resource " + source + " to " + target, e);
			}
		}
		
		public String read (InputStream in) {
			StringBuilder output = new StringBuilder();
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(in, "UTF-8");
				char[] buffer = new char[256];
				while (true) {
					int length = reader.read(buffer);
					if (length == -1) break;
					output.append(buffer, 0, length);
				}
			} catch (IOException ex) {
				throw new RuntimeException("Error reading layout file: " + this, ex);
			} finally {
				try {
					if (reader != null) reader.close();
				} catch (IOException ignored) {
				}
			}
			return output.toString();
		}
		
		private void write (File file, InputStream input) {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buffer = new byte[4096];
				while (true) {
					int length = input.read(buffer);
					if (length == -1) break;
					output.write(buffer, 0, length);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Error stream writing to file: " + file, ex);
			} finally {
				try {
					if (input != null) input.close();
				} catch (Exception ignored) {
				}
				try {
					if (output != null) output.close();
				} catch (Exception ignored) {
				}
			}
		}
	}
	
	class MkDir implements Task {
		public String target;
		
		public MkDir(String target) {
			this.target = target;
		}

		@Override
		public void execute () {
			log("Creating directory " + target);
			File file = new File(target);
			if(file.exists()) return;
			if(!file.mkdirs()) throw new RuntimeException("Couldn't create directory " + target);
 		}
	}
	
	class Download implements Task {
		public String source;
		public String target;
		
		public Download (String source, String target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void execute () {
			log("Downloading " + source + " to " + target);
			File targetFile = new File(target);
			if(targetFile.isDirectory()) throw new RuntimeException("Target file " + target + " is a directory");
			HttpUtils.downloadFile(source, targetFile);
		}
	}
	
	private static void log(String message) {
		System.out.println(message);
	}
	
	private static void printUsage() {
		log("Usage: java -jar gdx-setup.jar COMMAND [...]");
		log("COMMANDs are: ");
		log("    create");
		log("      Mandatory arguments:");
		log("        -workspace <path-to-workspace> ... the workspace directory to create the projects in.");
		log("        -project <project-name>        ... the name of the project");
		log("        -package <package-name>        ... the package name");
		log("        -mainclass <main-class-name>   ... the name of the main class ");		
		log("    update");
		log("      Mandatory arguments:");
		log("        -workspace <path-to-workspace> ... the workspace directory to update the projects in.");
		log("        -project <project-name>        ... the name of the project to update");
	}
	
	static String[] knownArgsCreate = { "-workspace", "-project", "-package", "-mainclass" };
	static String[] knownArgsUpdate = { "-workspace", "-project" };
	
	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> parsedArgs = new HashMap<String, String>();
		if (args.length > 0 && args[0].equals("create"))
		{
			for(int i = 1; i < args.length;) {
				String token = args[i];
				boolean known = false;
				for(String knownArg: knownArgsCreate) {
					if(token.equals(knownArg)) {
						if(args.length == i + 1) {
							log("expected value for argument '" + knownArg + "'");
							printUsage();
							System.exit(-1);
						}
						parsedArgs.put(knownArg, args[i+1]);
						known = true;
						i+=2;
						break;
					}
				}
				if(!known) {
					log("Unkown argument " + token);
					printUsage();
					System.exit(-1);
				}
			}
			
			for(String arg: knownArgsCreate) {
				if(!parsedArgs.containsKey(arg)) {
					log("missing argument " + arg);
					printUsage();
					System.exit(-1);
				}
			}		
		}
		else
		if (args.length > 0 && args[0].equals("update")) {
			for(int i = 1; i < args.length;) {
				String token = args[i];
				boolean known = false;
				for(String knownArg: knownArgsUpdate) {
					if(token.equals(knownArg)) {
						if(args.length == i + 1) {
							log("expected value for argument '" + knownArg + "'");
							printUsage();
							System.exit(-1);
						}
						parsedArgs.put(knownArg, args[i+1]);
						known = true;
						i+=2;
						break;
					}
				}
				if(!known) {
					log("Unkown argument " + token);
					printUsage();
					System.exit(-1);
				}
			}
			
			for(String arg: knownArgsUpdate) {
				if(!parsedArgs.containsKey(arg)) {
					log("missing argument " + arg);
					printUsage();
					System.exit(-1);
				}
			}
		}
		else {
			printUsage();
			System.exit(-1);
		}
		return parsedArgs;
	}
	
	public static void main (String[] args) {
		Map<String, String> parsedArgs = parseArgs(args);		
		if (args[0].equals("create")) {
			GdxSetup setup = new GdxSetup(parsedArgs.get("-workspace"), parsedArgs.get("-project"), parsedArgs.get("-package"), parsedArgs.get("-mainclass"));
			setup.create();
		}
		else
		if (args[0].equals("update")) {
			GdxSetup setup = new GdxSetup(parsedArgs.get("-workspace"), parsedArgs.get("-project"), null, null);
			setup.update();			
		}
	}
}