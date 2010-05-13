package com.badlogic.gdx.physics.box2d;

import java.util.ArrayList;

/**
 * A contact list stores all contacts that are currently found in the
 * world. It is used by the {@link World} class to record what's going on.
 * Internally we use a simple pool of Contacts so we don't allocate more
 * objects than are needed. 
 * 
 * @author mzechner
 *
 */
public class ContactList 
{
	/** contact pool **/
	private final ArrayList<Contact> freeContacts = new ArrayList<Contact>( );
	/** currently active contacts **/
	private final ArrayList<Contact> contacts = new ArrayList<Contact>( );
	/** index of the next free contact **/
	private int freeIdx = 0;
	
	protected void add( long addr )
	{
		
	}
	
	protected void remove( long addr )
	{
		
	}
	
	/**
	 * @return the number of currently active contacts
	 */
	public int size( )
	{
		return contacts.size();
	}
	
	/**
	 * @param index the index of the contact
	 * @return the contact
	 */
	public Contact get( int index )
	{
		return contacts.get(index);
	}
}
