/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.files.FileHandle;

/**
 * This interface encapsulates the access of internal and external files.
 * Internal files are read-only and come with the application when deployed.
 * On Android assets are internal files, on the desktop anything in the applications
 * root directory is considered to be a an internal file. External files can be read and
 * written to. On Android they are relative to the SD-card, on the desktop they are
 * relative to the user home directory.
 * 
 * @author mzechner
 *
 */
public interface Files 
{
	/**
	 * Returns an InputStream to the given internal file. On
	 * Android the filename is relative to the assets directory.
	 * On the desktop the filename is relative to the application's
	 * root directory.
	 * 
	 * @param fileName the name of the file to open.
	 * @return the InputStream or null if the file couldn't be opened.
	 */
	public InputStream readInternalFile( String fileName );
	
	/**
	 * Returns an InputStream to the given external file. On
	 * Android the filename is relative to the external storage
	 * directory, e.g. /sdcard/.  On the desktop the filename is relative to 
	 * user's home directory, e.g. /home/mzechner/ on Linux or
	 * C:/Users/mzechner/ on Windows 7.
	 * 
	 * @param fileName the name of the file to open.
	 * @return the InputStream or null if the file couldn't be opened.
	 */
	public InputStream readExternalFile( String fileName );
	
	/**
	 * Returns and OutputStream to the given external file. If
	 * the file does not exist it is created. If the file
	 * exists it will be overwritten. On Android the filename is relative 
	 * to the external storage directory, e.g. /sdcard/.  On the desktop 
	 * the filename is relative to user's home directory, e.g. 
	 * /home/mzechner/ on Linux or C:/Users/mzechner/ on Windows 7.
	 * 
	 * @param filename the name of the file to open
	 * @return the OutputStream or null if the file couldn't be opened.
	 */
	public OutputStream writeExternalFile( String filename );
	
	/**
	 * Creates a new directory or directory hierarchy on the external
	 * storage. If the directory parameter contains sub folders and 
	 * the parent folders don't exist yet they will be created. On Android the filename is relative 
	 * to the external storage directory, e.g. /sdcard/.  On the desktop 
	 * the filename is relative to user's home directory, e.g. 
	 * /home/mzechner/ on Linux or C:/Users/mzechner/ on Windows 7.  
	 * 
	 * @param directory the directory
	 * @return true in case the directory could be created, false otherwise
	 */
	public boolean makeDirectory( String directory );
	
	/**
	 * Returns a {@link FileDescriptor} object for an internal file. An internal
	 * file on Android is given relative to the asset directory. On the desktop
	 * it is given relative to the application's root directory. 
	 *  
	 * @param filename the name of the file
	 * @return the FileDescriptor or null if the descriptor could not be created
	 */
	public FileHandle getInternalFileHandle( String filename );
	
	/**
	 * Returns a {@link FileDescriptor} object for an external file. An external
	 * file on Android is given relative to the external storage device path. On the
	 * desktop it is given relative to the user's home directory.
	 * 
	 * @param filename the name of the file
	 * @return the FileDescriptor or null if the descriptor could not be created
	 */
	public FileHandle getExternalFileHandle( String filename );
}
