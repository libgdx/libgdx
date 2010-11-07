/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx;

import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * <p>This interface encapsulates the access of internal, external and absolute
 * files.</p> 
 * 
 * <p>
 * Internal files are read-only and are bundled with the application. On
 * Android internal files map to assets. On the desktop the classpath is first
 * searched for the file. If this fails the root directory of the application
 * is searched.
 * </p>
 * 
 * <p>
 * External files can be read and written to. On Android they are relative to
 * the SD-card, on the desktop they are relative to the user home directory.
 * </p>
 * 
 * <p>
 * Absolute files are just that, fully qualified filenames. To ensure
 * portability across platforms use absolute files only when absolutely
 * necessary.
 * </p>
 * 
 * @author mzechner
 * 
 */
public interface Files {
	/**
	 * Enum describing the three file types, internal, external and absolute.
	 * Internal files are located in the asset directory on Android and are
	 * relative to the root of the classpath or application's root directory on
	 * the desktop. External files are relative to the SD-card on Android and
	 * relative to the home directory of the current user on the desktop.
	 * Absolute files are just that, absolute files that can point anywhere.
	 * 
	 * @author mzechner
	 * 
	 */
	public enum FileType {
		Internal, External, Absolute
	}

	/**
	 * Returns an {@link InputStream} to the given file. If type is equal to
	 * {@link FileType#Internal} an internal file will be opened. On Android
	 * this is relative to the assets directory, on the desktop it is relative
	 * to the applications classpath or if that fails to the root directory. If 
	 * type is equal to {@link FileType#External} an external file will be opened. On Android
	 * this is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to
	 * {@link FileType#Absolute} the filename is interpreted as an absolute
	 * filename.
	 * 
	 * @param fileName
	 *            the name of the file to open.
	 * @param type
	 *            the type of file to open.
	 * @return the InputStream
	 * @throws GdxRuntimeException
	 *             in case the file could not be opened.
	 */
	public InputStream readFile(String fileName, FileType type);

	/**
	 * Returns and {@link OutputStream} to the given file. If the file does not
	 * exist it is created. If the file exists it will be overwritten. If type
	 * is equal to {@link FileType#Internal} an exception is thrown. If type is 
	 * equal to {@link FileType#External} an external file will be opened. On Android this is relative to the
	 * SD-card, on the desktop it is relative to the current user's home
	 * directory. If type is equal to {@link FileType#Absolute} the filename is
	 * interpreted as an absolute filename.
	 * 
	 * @param filename
	 *            the name of the file to open
	 * @param type
	 *            the type of the file to open
	 * @return the OutputStream
	 * @throws GdxRuntimeException
	 *             in case the file could not be opened or type equals {@link FileType#Internal}
	 */
	public OutputStream writeFile(String filename, FileType type);

	/**
	 * Creates a new directory or directory hierarchy on the external storage.
	 * If the directory parameter contains sub folders and the parent folders
	 * don't exist yet they will be created. If type is equal to
	 * {@link FileType#Internal} false will be returned. If type is equal
	 * to {@link FileType#External} an external directory will be created. On
	 * Android this is relative to the SD-card, on the desktop it is relative to
	 * the current user's home directory. If type is equal to
	 * {@link FileType#Absolute} the directory is interpreted as an absolute
	 * directory name.
	 * 
	 * @param directory
	 *            the directory
	 * @param type
	 *            the type of the directory
	 * @return true in case the directory could be created, false otherwise
	 */
	public boolean makeDirectory(String directory, FileType type);

	/**
	 * Lists the files and directories in the given directory. If type is equal
	 * to {@link FileType#Internal} an internal directory will be listed. On
	 * Android this is relative to the assets directory, on the desktop it is
	 * relative to the applications root directory. If type is equal to
	 * {@link FileType#External} an external directory will be listed. On
	 * Android this is relative to the SD-card, on the desktop it is relative to
	 * the current user's home directory. If type is equal to
	 * {@link FileType#Absolute} the filename is interpreted as an absolute
	 * directory.
	 * 
	 * @param directory
	 *            the directory
	 * @param type
	 *            the type of the directory
	 * @return the files and directories in the given directory
	 * @throws GdxRuntimeException
	 *             if the directory does not exist
	 */
	public String[] listDirectory(String directory, FileType type);

	/**
	 * Returns a {@link FileHandle} object for a file. If type is equal to
	 * {@link FileType#Internal} an internal file will be opened. On Android
	 * this is relative to the assets directory, on the desktop it is relative
	 * to the application's classpath or if that false to the root directory. If type is equal to
	 * {@link FileType#External} an external file will be opened. On Android
	 * this is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to
	 * {@link FileType#Absolute} the filename is interpreted as an absolute
	 * filename.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param type
	 *            the type of the file
	 * @return the FileDescriptor or null if the descriptor could not be created
	 * @throws GdxRuntimeException
	 *             if the file does not exist
	 */
	public FileHandle getFileHandle(String filename, FileType type);
}
