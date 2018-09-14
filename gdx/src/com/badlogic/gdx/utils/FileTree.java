package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileTree {
	/**
	 * FileTree is a tree representing a directory structure. It is used to emulate some file system calls for internally
	 * packaged files. The index that the tree is populated with is created at compile time. See {@link FileHandle#list()} for
	 * the index specification.
	 */

	public final String path;
	public final int depth;
	private List<FileTree> children = new ArrayList<FileTree>();

	public FileTree (String path) {
		this(path, new FileTree[0]);
	}

	public FileTree (String path, FileTree[] children) {
		this.path = path;
		//Calculates directory depth by how many forward slashes there are. Subtract one from dir
		this.children.addAll(Arrays.asList(children));


		//If this is the root directory, then depth = 0
		if (path.isEmpty() || path.equals("/")) {
			depth = 0;
		} else if (isDirectory())
			this.depth = this.path.length() - this.path.replace("/", "").length();
		else
			this.depth = this.path.length() - this.path.replace("/", "").length() + 1;
	}

	/**
	 * Builds out the current FileTree given a String[] of file paths conforming to the Asset Index specification which can be
	 * in {@link FileHandle#list()}
	 */
	public void load (String[] filePaths) {
		//Return if filePaths is null, and warn user.
		if (filePaths == null) {
			Gdx.app.log("FileTree", "Failed to load FileTree, String[] was null.");
			return;
		}
		//Return if asset index is empty
		if (filePaths.length == 0)
			return;
		FileTree[] fileTrees = new FileTree[filePaths.length];
		for (int i = 0; i < fileTrees.length; i++) {
			fileTrees[i] = new FileTree(filePaths[i]);
		}
		if (isDirectory()) {
			for (FileTree fileTree : fileTrees) {
				if (fileTree.depth == this.depth + 1 && fileTree.path.startsWith(this.path)) {
					if (!fileTree.isDirectory()) {
						children.add(fileTree);
					} else {
						fileTree.load(filePaths);
						children.add(fileTree);
					}
				}
			}
		}
	}

	/**
	 * Checks if this file tree is a directory.
	 */
	public boolean isDirectory () {
		return path.endsWith("/") || path.isEmpty();
	}

	/**
	 * Checks if this {@link FileTree} contains a node with the given path. It will always return false if this FileTree's path
	 * points to a file.
	 */
	public boolean contains (String somePath) {
		if (children.size() == 0 || !isDirectory())
			return false;
		for (FileTree child : children) {
			//Extra condition in case the user types in a directory path without a terminating forward slash
			if (child.path.equals(somePath) || child.path.equals(somePath + "/"))
				return true;
		}
		return false;
	}

	/**
	 * Finds a node in the tree with a path that matches the argument. If the node can't be found it returns null.
	 */
	public FileTree find (String somePath) {
		String[] splitPath = somePath.split("/");
		//Return this file tree if the input is "/" or "". Reserves "." for listing the classpath instead of assets.
		if (splitPath.length == 0 || somePath.isEmpty())
			return this;
		//Check if we're at the right depth to search for the file or directory. Otherwise, call find on next node.
		if (splitPath.length - 1 == this.depth) {
			for (FileTree fileTree : getChildren()) {
				if (fileTree.path.equals(somePath) || fileTree.path.equals(somePath + "/"))
					return fileTree;
			}
		} else {
			String nextDir = "";
			for (int i = 0; i <= depth + 1; i++) {
				nextDir += (splitPath[i] + "/");
			}
			for (FileTree fileTree : getChildren()) {
				if (fileTree.path.equals(nextDir))
					return fileTree.find(somePath);
			}
		}
		return null;
	}

	/**
	 * Returns the children of this node after it is converted to a {@link FileHandle} array.
	 */
	public FileHandle[] list () {
		FileHandle[] fileHandles = new FileHandle[getChildren().length];
		for (int i = 0; i < fileHandles.length; i++) {
			fileHandles[i] = getChildren()[i].getHandle();
		}
		return fileHandles;
	}

	public FileHandle getHandle () {
		return new FileHandle(path);
	}

	public FileTree[] getChildren () {
		FileTree[] childrenArray = new FileTree[children.size()];
		return children.toArray(childrenArray);
	}

	public void setChildren (FileTree[] children) {
		this.children = Arrays.asList(children);
	}

	public void addChild (FileTree child) {
		children.add(child);
	}

	public void removeChild (FileTree child) {
		children.remove(child);
	}
}
