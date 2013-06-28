package com.badlogic.gdx.graphics.g3d.shaders.graph;

import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderNode.ShaderNodeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

public class ShaderGraph {
	private final ShaderNodeType type;
	private final ObjectMap<String, ShaderNode> nodeTypes = new ObjectMap<String, ShaderNode>();
	private final ObjectSet<ShaderNode> nodeSet = new ObjectSet<ShaderNode>();
	private final Array<ShaderNode> nodes = new Array<ShaderNode>();
	private final Array<ShaderConnection> connections = new Array<ShaderConnection>();
	private final ObjectMap<ShaderNode, Array<ShaderConnection>> connectionMap = new ObjectMap<ShaderNode, Array<ShaderConnection>>();
	
	public ShaderGraph(ShaderNodeType type) {
		this.type = type;
	}
	
	public ShaderNodeType getType () {
		return type;
	}

	public void addNodeType(ShaderNode node) {
		if(node.getType() != type) throw new GdxRuntimeException("graph type " + type + " != node type " + node.getType());
		if(nodeTypes.get(node.getName()) != null) throw new GdxRuntimeException("Node type with name '" + node.getName() + "' already in graph");
		nodeTypes.put(node.getName(), node);
	}
	
	public ShaderNode newNode(String nodeType) {
		ShaderNode node = nodeTypes.get(nodeType);
		if(node == null) throw new GdxRuntimeException("Node type '" + nodeType + "' not in graph, add it with addNodeType() first!");
		node = node.copy();
		nodes.add(node);
		nodeSet.add(node);
		return node;
	}
	
	public void connect(ShaderNode outputNode, String outputName, ShaderNode inputNode, String inputName) {
		if(!nodeSet.contains(outputNode)) throw new GdxRuntimeException("output node not in graph");
		if(!nodeSet.contains(inputNode)) throw new GdxRuntimeException("input node not in graph");
		ShaderOutput output = outputNode.getOutput(outputName);
		if(output == null) throw new GdxRuntimeException("shader output '" + outputName + "' not in node '" + outputNode.getName() + "'");
		ShaderInput input = inputNode.getInput(inputName);
		if(input == null) throw new GdxRuntimeException("shader input '" + inputName + "' not in node '" + inputNode.getName() + "'");
		if(!output.getType().equals(input.getType())) throw new GdxRuntimeException("shader output '" + output.getName() + "' has type '" + output.getType() + "'"
																										 + ", does not match shader input '" + input.getName() + "' type '" + input.getType() + "'");
		ShaderConnection connection = new ShaderConnection(outputNode, output, inputNode, input);
		connections.add(connection);
		addConnection(outputNode, connection);
		addConnection(inputNode, connection);
	}
	
	private void addConnection(ShaderNode node, ShaderConnection connection) {
		Array<ShaderConnection> connections = connectionMap.get(node);
		if(connections == null) {
			connections = new Array<ShaderConnection>();
			connectionMap.put(node, connections);
		}
		connections.add(connection);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("types {\n");
		for(String type: nodeTypes.keys()) {
			builder.append("   {\n");
			String[] lines = nodeTypes.get(type).toString().split("\n");
			for(String line: lines) {
				builder.append("      " + line + "\n");
			}
			builder.append("   }\n");
		}
		builder.append("\n}\n");
		
		builder.append("nodes {\n");
		int i = 0;
		for(ShaderNode node: nodes) {
			builder.append("   node " + node.getName() + "_" + i + ";\n");
			++i;
		}
		builder.append("\n}\n");
		
		builder.append("connections {\n");
		for(ShaderConnection con: connections) {
			int outputIdx = nodes.indexOf(con.getOutputNode(), true);
			int inputIdx = nodes.indexOf(con.getInputNode(), true);
			builder.append("   " + con.getOutputNode().getName() + "_" + outputIdx + con.getOutput().getName() + " -> ");
			builder.append(con.getInputNode().getName() + "_" + inputIdx + con.getInput().getName() + ";\n");
			++i;
		}
		builder.append("\n}\n");
		
		return builder.toString();
	}
}
