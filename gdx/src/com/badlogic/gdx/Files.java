package com.badlogic.gdx;

import java.io.InputStream;
import java.io.OutputStream;

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
}
