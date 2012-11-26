
package de.swagner.paxbritannica.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

import de.swagner.paxbritannica.PaxBritannica;

public class PaxBritannicaGwt extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration config = new GwtApplicationConfiguration( 1024, 550);
		return config;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new PaxBritannica();
	}
}
