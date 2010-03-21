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
package com.badlogic.gdx.audio.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a threshold function based on the spectral flux.  
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class ThresholdFunction 
{
	/** the history size **/
	private final int historySize;
	
	/** the average multiplier **/
	private final float multiplier;
	
	/**
	 * Consturctor, sets the history size in number of spectra
	 * to take into account to calculate the average spectral flux
	 * at a specific position. Also sets the multiplier to 
	 * multiply the average with.
	 * 
	 * @param historySize The history size.
	 * @param multiplier The average multiplier.
	 */
	public ThresholdFunction( int historySize, float multiplier )
	{
		this.historySize = historySize;
		this.multiplier = multiplier;
	}
	
	/**
	 * Returns the threshold function for a given 
	 * spectral flux function.
	 * 
	 * @return The threshold function.
	 */
	public List<Float> calculate( List<Float> spectralFlux )
	{
		ArrayList<Float> thresholds = new ArrayList<Float>( spectralFlux.size() );
		
		for( int i = 0; i < spectralFlux.size(); i++ )
		{
			float sum = 0;
			int start = Math.max( 0, i - historySize / 2);
			int end = Math.min( spectralFlux.size()-1, i + historySize / 2 );
			for( int j = start; j <= end; j++ )
				sum += spectralFlux.get(j);
			sum /= (end-start);
			sum *= multiplier;
			thresholds.add( sum );
		}
		
		return thresholds;
	}
}
