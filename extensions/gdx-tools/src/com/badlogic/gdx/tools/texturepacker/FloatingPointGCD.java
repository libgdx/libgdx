/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.tools.texturepacker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates a psuedo-greatest common divisor for floating point numbers, given a maximum allowable error.
 * <p> Based on algorithm described <a href="http://stackoverflow.com/a/479028/506796">here</a>.
 * @author Darren Keese
 */
public class FloatingPointGCD {

	public static float findFloatingPointGCD(Float[] input, float maxError) {

		//Sanitize input
		Set<Float> inputSet = new HashSet<Float>(input.length);
		for (Float f : input) {
			if (f!=0) inputSet.add(Math.abs(f));
		}

		if (inputSet.size() <= 1)
			return Math.abs(input[0]);

		float lowestValue = input[0];
		float highestValue = lowestValue;
		for (int i=1; i<input.length; i++){
			float value = input[i];
			if (value<lowestValue) lowestValue = value;
			if (value>highestValue) highestValue = value;
		}

		//Find fast worst case GCD to limit upcoming rational fraction tree search
		int[] errorScaledInput = new int[input.length];
		for (int i = 0; i < input.length; i++) {
			errorScaledInput[i] = Math.round(input[i] / maxError);
		}
		float worstCaseGCD = gcd(errorScaledInput) * maxError;

		//Express input set as ratios to lowest value. If these ratios can be expressed as
		//rational fractions that all have the same denominator, then that denominator represents
		//a potential solution when the lowest value is divided by it.
		inputSet.remove(lowestValue);
		List<Float> scaledInput = new ArrayList<Float>(inputSet.size());
		for (Float f : inputSet)
			scaledInput.add(f/lowestValue);

		//Find set of all possible fractions that might produce better result than worstCaseGCD
		int maxNumerator = Math.round(highestValue / worstCaseGCD);
		int maxDenominator = Math.round(lowestValue / worstCaseGCD);
		Set<Rational> tree = generateSternBrocotTree(maxNumerator, maxDenominator);

		//Find subset of rational representations of each member of input set that don't violate
		//max error.
		List<Set<Rational>> validRationals = new ArrayList<Set<Rational>>();
		for (Float f : scaledInput){
			float unscaledF = f*lowestValue;
			Set<Rational> valids = new HashSet<Rational>();
			for (Rational rat : tree){
				float gcd = lowestValue/rat.den;
				float error = Math.abs(rat.num*gcd - unscaledF);
				if (error <= maxError)
					valids.add(rat);
			}
			validRationals.add(valids);
		}

		//Find all denominators that are shared among all solutions sets. Score each solution
		//by summing all the numerators and the denominator.
		int bestSolutionSum = -1;
		int bestSolution = -1;
		for (int den = 1; den < maxDenominator+1; den++) {
			boolean denIsValidForAll = true;
			int solutionSum = den;
			for (Set<Rational> floatsSet : validRationals){
				boolean denIsValidForFloat = false;
				for (Rational rat : floatsSet){
					if (rat.den == den) {
						solutionSum += rat.den;
						denIsValidForFloat = true;
						break;
					}
				}
				if (!denIsValidForFloat){
					denIsValidForAll = false;
					break;
				}
			}
			if (denIsValidForAll && (bestSolutionSum <0 || solutionSum<bestSolutionSum)){
				bestSolutionSum = solutionSum;
				bestSolution = den;
			}
		}

		float gcd = bestSolution > 0 ? lowestValue/bestSolution : worstCaseGCD;

		//Minimize error by calculating linear regression
		inputSet.add(lowestValue); //was removed earlier
		Map<Float, Float> points = new HashMap<Float, Float>(inputSet.size());
		for (Float f : inputSet){
			points.put(f, (float)Math.round(f/gcd));
		}
		gcd = calculateRegressionSlope(points);

		return gcd;
	}

	private static class Rational {
		public int num, den;
		public Rational(int num, int den){
			this.num = num; this.den = den;
		}
		public static Rational mediant(Rational a, Rational b){
			return new Rational(a.num + b.num, a.den + b.den);
		}
		public boolean equals(Object obj){
			if (obj instanceof Rational){
				Rational rat = (Rational)obj;
				return rat.num==num && rat.den==den;
			}
			return false;
		}
		public String toString(){
			return num + ":" + den;
		}
	}

	private static Set<Rational> generateSternBrocotTree(int maxNumerator, int maxDenominator){
		Set<Rational> tree = new HashSet<Rational>();
		Rational left = new Rational(0,1);
		Rational right = new Rational(1,0);
		addSternBrocotBranch(tree, left, right, maxNumerator, maxDenominator);
		return tree;
	}

	private static void addSternBrocotBranch(Set<Rational> tree, Rational left, Rational right, int maxNumerator, int maxDenominator){
		Rational mediant = Rational.mediant(left, right);
		if (mediant.num > maxNumerator || mediant.den > maxDenominator)
			return;
		tree.add(mediant);
		addSternBrocotBranch(tree, left, mediant, maxNumerator, maxDenominator); //add left branch
		addSternBrocotBranch(tree, mediant, right, maxNumerator, maxDenominator); //add right branch
	}

	private static int gcd(int[] in){
		if (in.length==1)
			return in[0];
		int gcd = in[0];
		for (int i=1; i<in.length; i++){
			gcd = gcd(gcd, in[i]);
		}
		return gcd;
	}

	private static int gcd(int a, int b){
		if(a == 0 || b == 0) return a+b;
		return gcd(b,a%b);
	}

	private static float calculateRegressionSlope(Map<Float, Float> points){
		float meanX = 0, meanY = 0;
		int numPoints = points.size();
		for(Map.Entry<Float, Float> entry : points.entrySet()){
			meanX += entry.getKey();
			meanY += entry.getValue();
		}
		meanX /= numPoints;
		meanY /= numPoints;

		float stDevX = 0, stDevY = 0, r = 0;
		for(Map.Entry<Float, Float> entry : points.entrySet()){
			float xDiff = entry.getKey()-meanX;
			float yDiff = entry.getValue()-meanY;
			stDevX += xDiff*xDiff;
			stDevY += yDiff*yDiff;
			r += xDiff*yDiff;
		}
		r /= (float)Math.sqrt(stDevX*stDevY);
		stDevX = (float)Math.sqrt(stDevX/numPoints);
		stDevY = (float)Math.sqrt(stDevY/numPoints);

		return r * stDevX / stDevY;
	}

}
