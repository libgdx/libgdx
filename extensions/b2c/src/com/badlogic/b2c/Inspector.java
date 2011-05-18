package com.badlogic.b2c;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Inspector {
	public void inspect(String className) throws IOException {		
		ClassReader reader = new ClassReader("com/badlogic/b2c/test/SimpleBean");
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
	}
	
	public static void main(String[] argv) throws IOException {		
		ClassReader reader = new ClassReader("com/badlogic/b2c/test/SimpleBean");
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		System.out.println(node.toString());
	}
}
