/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.files.FileHandle;

/**
 * This interface encapsulates the access of internal, external and absolut files.
 * Internal files are read-only and come with the application when deployed.
 * On Android assets are internal files, on the desktop anything in the applications
 * root directory is considered to be a an internal file. External files can be read and
 * written to. On Android they are relative to the SD-card, on the desktop they are
 * relative to the user home directory. Absolut files are just that, fully qualified
 * filenames.
 * 
 * @author mzechner
 *
 */
public interface Files 
{
	/**
	 * Enum describing the three file types, internal, external
	 * and absolut. Internal files are located in the asset directory
	 * on Android and are relative to the applications root directory
	 * on the desktop. External files are relative to the SD-card on Android
	 * and relative to the home directory of the current user on the Desktop.
	 * Absolut files are just that, absolut files that can point anywhere.
	 * @author mzechner
	 *
	 */
	public enum FileType
	{
		Internal,
		External,
		Absolut
	}
	
	/**
	 * Returns an InputStream to the given file. If type is equal
	 * to FileType.Internal an internal file will be opened. On Android
	 * this is relative to the assets directory, on the desktop it is 
	 * relative to the applications root directory. If type is equal to
	 * FileType.External an external file will be opened. On Android this
	 * is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to FileType.Absolut
	 * the filename is interpreted as an absolut filename. 
	 * 
	 * @param fileName the name of the file to open.
	 * @param type the type of file to open.
	 * @return the InputStream or null if the file couldn't be opened.
	 */
	public InputStream readFile( String fileName, FileType type );	
	
	/**
	 * Returns and OutputStream to the given  file. If
	 * the file does not exist it is created. If the file
	 * exists it will be overwritten. If type is equal
	 * to FileType.Internal null will be returned as on Android assets can not be written. If type is equal to
	 * FileType.External an external file will be opened. On Android this
	 * is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to FileType.Absolut
	 * the filename is interpreted as an absolut filename.
	 * 
	 * @param filename the name of the file to open
	 * @param type the type of the file to open
	 * @return the OutputStream or null if the file couldn't be opened.
	 */
	public OutputStream writeFile( String filename, FileType type );
		
	/**
	 * Creates a new directory or directory hierarchy on the external
	 * storage. If the directory parameter contains sub folders and 
	 * the parent folders don't exist yet they will be created. If type is equal
	 * to FileType.Internal false will be returned as on Android new directories in the asset directory can not be created. If type is equal to
	 * FileType.External an external directory will be created. On Android this
	 * is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to FileType.Absolut
	 * the directory is interpreted as an absolut directory name.
	 * 
	 * @param directory the directory
	 * @param type the type of the directory
	 * @return true in case the directory could be created, false otherwise
	 */
	public boolean makeDirectory( String directory, FileType type );
	
	
	/**
	 * Lists the files and directories in the given directory. If type is equal
	 * to FileType.Internal an internal directory will be listed. On Android
	 * this is relative to the assets directory, on the desktop it is 
	 * relative to the applications root directory. If type is equal to
	 * FileType.External an external directory will be listed. On Android this
	 * is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to FileType.Absolut
	 * the filename is interpreted as an absolut directory.
	 *  
	 * @param directory the directory
	 * @param type the type of the directory
	 * @return the files and directories in the given directory or null if the directory is none existant
	 */
	public String[] listDirectory( String directory, FileType type );
	
	
	/**
	 * Returns a {@link FileDescriptor} object for a file. If type is equal
	 * to FileType.Internal an internal file will be opened. On Android
	 * this is relative to the assets directory, on the desktop it is 
	 * relative to the applications root directory. If type is equal to
	 * FileType.External an external file will be opened. On Android this
	 * is relative to the SD-card, on the desktop it is relative to the
	 * current user's home directory. If type is equal to FileType.Absolut
	 * the filename is interpreted as an absolut filename. 
	 *  
	 * @param filename the name of the file
	 * @param type the type of the file
	 * @return the FileDescriptor or null if the descriptor could not be created
	 */
	public FileHandle getFileHandle( String filename, FileType type );		
}
