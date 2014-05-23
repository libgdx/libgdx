
package com.badlogic.gdx.setup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class VersionBuilder {

	public static void main (String[] args) {
		new VersionBuilder();
	}

	public VersionBuilder () {
		try {
			PrintWriter writer = new PrintWriter("versions.txt", "UTF-8");
			writer.println("Release: " + getRelease());
			writer.println("Snapshot: " + getSnapshot());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getRelease () throws IOException {
		String[] versions = new String[7];
		URL repoUrl = new URL("https://raw.githubusercontent.com/libgdx/libgdx/" + DependencyBank.libgdxVersion
			+ "/extensions/gdx-setup/src/com/badlogic/gdx/setup/DependencyBank.java");
		BufferedReader in = new BufferedReader(new InputStreamReader(repoUrl.openStream()));
		String input;
		while ((input = in.readLine()) != null) {
			if (input.contains("String roboVMVersion")) {
				versions[0] = parseVersion(input);
			}
			if (input.contains("roboVMPluginImport")) {
				versions[1] = findVersion(parseVersion(input));
			}
			if (input.contains("buildToolsVersion")) {
				versions[2] = parseVersion(input);
			}
			if (input.contains("androidAPILevel")) {
				versions[3] = parseVersion(input);
			}
			if (input.contains("androidPluginImport")) {
				versions[4] = findVersion(parseVersion(input));
			}
			if (input.contains("gwtVersion")) {
				versions[5] = parseVersion(input);
			}
			if (input.contains("gwtPluginImport")) {
				versions[6] = findVersion(parseVersion(input));
			}
		}
		in.close();

		String versionString = "[";
		for (int i = 0; i < versions.length; i++) {
			versionString += versions[i] != null ? versions[i] : "Unknown";
			if (i < versions.length - 1) {
				versionString += ":";
			}
		}
		versionString += "]";
		return versionString;
	}

	private String getSnapshot () {
		String[] versions = new String[7];
		versions[0] = DependencyBank.roboVMVersion;
		versions[1] = findVersion(DependencyBank.roboVMPluginImport);
		versions[2] = DependencyBank.buildToolsVersion;
		versions[3] = DependencyBank.androidAPILevel;
		versions[4] = findVersion(DependencyBank.androidPluginImport);
		versions[5] = DependencyBank.gwtVersion;
		versions[6] = findVersion(DependencyBank.gwtPluginImport);
		String versionString = "[";
		for (int i = 0; i < versions.length; i++) {
			versionString += versions[i] != null ? versions[i] : "Unknown";
			if (i < versions.length - 1) {
				versionString += ":";
			}
		}
		versionString += "]";
		return versionString;
	}

	private String parseVersion (String line) {
		return line.split("\"")[1];
	}

	private String findVersion (String dependency) {
		String[] split = dependency.split(":");
		return split[split.length - 1];
	}

}
