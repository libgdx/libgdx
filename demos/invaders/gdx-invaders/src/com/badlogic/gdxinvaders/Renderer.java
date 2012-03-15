package com.badlogic.gdxinvaders;

import com.badlogic.gdxinvaders.simulation.Simulation;

public interface Renderer {
	public void render(Simulation sim, float delta);
	public void dispose();
}
