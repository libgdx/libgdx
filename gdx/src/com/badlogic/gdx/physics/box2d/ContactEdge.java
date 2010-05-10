package com.badlogic.gdx.physics.box2d;

/**
 *  A contact edge is used to connect bodies and contacts together
 * in a contact graph where each body is a node and each contact
 * is an edge. A contact edge belongs to a doubly linked list
 * maintained in each attached body. Each contact has two contact
 * nodes, one for each attached body.
 */
public class ContactEdge 
{
	public Body other;
	public Contact contact;
}
