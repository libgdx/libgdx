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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.PerformanceCounter;

import java.util.Comparator;

/** For testing and benchmarking of gdx.utils.Select and its associated algorithms/classes
 * @author Jon Renner */
public class SelectTest extends GdxTest {
	static PerformanceCounter perf = new PerformanceCounter("bench");
	static boolean verify; // verify and report the results of each selection
	private static boolean quiet;

	@Override
	public void create () {
		int n = 100;
		player = createDummies(n);
		enemy = createDummies(n);

		int runs = 100;
		// run correctness first to warm up the JIT and other black magic
		quiet = true;
		allRandom();
		print("VERIFY CORRECTNESS FIND LOWEST RANKED");
		correctnessTest(runs, 1);
		print("VERIFY CORRECTNESS FIND MIDDLE RANKED");
		correctnessTest(runs, enemy.size / 2);
		print("VERIFY CORRECTNESS FIND HIGHEST RANKED");
		correctnessTest(runs, enemy.size);

		runs = 1000;
		quiet = true;
		print("BENCHMARK FIND LOWEST RANKED");
		performanceTest(runs, 1);
		print("BENCHMARK FIND MIDDLE RANKED");
		performanceTest(runs, enemy.size / 2);
		print("BENCHMARK FIND HIGHEST RANKED");
		performanceTest(runs, enemy.size);

		print("TEST CONSISTENCY FOR LOWEST RANKED");
		consistencyTest(runs, 1);
		print("TEST CONSISTENCY FOR MIDDLE RANKED");
		consistencyTest(runs, enemy.size / 2);
		print("TEST CONSISTENCY FOR HIGHEST RANKED");
		consistencyTest(runs, enemy.size);

		// test that selectRanked and selectRankedIndex return the same
		print("TEST selectRanked AND selectRankedIndex RETURN MATCHING RESULTS - LOWEST RANKED");
		testValueMatchesIndex(runs, 1);
		print("TEST selectRanked AND selectRankedIndex RETURN MATCHING RESULTS - MIDDLE RANKED");
		testValueMatchesIndex(runs, enemy.size / 2);
		print("TEST selectRanked AND selectRankedIndex RETURN MATCHING RESULTS - HIGHEST RANKED");
		testValueMatchesIndex(runs, enemy.size);

		print("ALL TESTS PASSED");
	}

	public static void correctnessTest (int runs, int k) {
		String msg = String.format("[%d runs with %dx%d dummy game units] - ", runs, player.size, enemy.size);
		verify = true;
		test(runs, k);
		print(msg + "VERIFIED");
	}

	public static void performanceTest (int runs, int k) {
		verify = false;
		test(runs, k);
		String msg = String.format("[%d runs with %dx%d dummy game units] - ", runs, player.size, enemy.size);
		print(msg
			+ String.format("avg: %.5f, min/max: %.4f/%.4f, total time: %.3f (ms), made %d comparisons", allPerf.time.min,
				allPerf.time.max, allPerf.time.average * 1000, allPerf.time.total * 1000, comparisonsMade));
	}

	public static void consistencyTest (int runs, int k) {
		verify = false;
		Dummy test = player.get(0);
		Dummy lastFound = null;
		allRandom();
		for (int i = 0; i < runs; i++) {
			Dummy found = test.getKthNearestEnemy(k);
			if (lastFound == null) {
				lastFound = found;
			} else {
				if (!(lastFound.equals(found))) {
					print("CONSISTENCY TEST FAILED");
					print("lastFound: " + lastFound);
					print("justFound: " + found);
					throw new GdxRuntimeException("test failed");
				}
			}
		}
	}

	public static void testValueMatchesIndex (int runs, int k) {
		verify = false;
		for (int i = 0; i < runs; i++) {
			allRandom();
			player.shuffle();
			enemy.shuffle();
			originDummy = player.random();
			int idx = enemy.selectRankedIndex(distComp, k);
			Dummy indexDummy = enemy.get(idx);
			Dummy valueDummy = enemy.selectRanked(distComp, k);
			if (!(indexDummy.equals(valueDummy))) {
				throw new GdxRuntimeException("results of selectRankedIndex and selectRanked do not return the same object\n"
					+ "selectRankedIndex -> " + indexDummy + "\n" + "selectRanked      -> " + valueDummy);
			}

		}
	}

	public static void test (int runs, int k) {
		// k = kth order statistic
		comparisonsMade = 0;
		perf.reset();
		allPerf.reset();
		allRandom();
		enemy.shuffle();
		player.shuffle();
		for (int i = 0; i < runs; i++) {
			getKthNearestEnemy(quiet, k);
		}
	}

	public static void allRandom () {
		for (Dummy d : player) {
			d.setRandomPos();
		}
		for (Dummy d : enemy) {
			d.setRandomPos();
		}
	}

	private static PerformanceCounter allPerf = new PerformanceCounter("all");

	public static void getKthNearestEnemy (boolean silent, int k) {
		Dummy kthDummy = null;
		perf.reset();
		allPerf.start();
		for (Dummy d : player) {
			Dummy found = d.getKthNearestEnemy(k);
		}
		allPerf.stop();
		allPerf.tick();
		if (silent) return;
		print(String.format("found nearest. min: %.4f, max: %.4f, avg: %.4f, total: %.3f ms", perf.time.min * 1000,
			perf.time.max * 1000, perf.time.average * 1000, perf.time.total * 1000));
	}

	public static void verifyCorrectness (Dummy d, int k) {
		enemy.sort(distComp);
		int idx = enemy.indexOf(d, true);
		// remember that k = min value = 0 position in the array, therefore k - 1
		if (enemy.get(idx) != enemy.get(k - 1)) {
			System.out.println("origin dummy: " + originDummy);
			System.out.println("TEST FAILURE: " + "idx: " + idx + " does not equal (k - 1): " + (k - 1));
			throw new GdxRuntimeException("test failed");
		}
	}

	static class Dummy {
		public Vector2 pos;
		public int id;

		public Dummy () {
			// set the position manually
		}

		@Override
		public boolean equals (Object obj) {
			if (!(obj instanceof Dummy)) {
				throw new GdxRuntimeException("do not compare to anything but other Dummy objects");
			}
			Dummy d = (Dummy)obj;
			// we only care about position/distance
			float epsilon = 0.0001f;
			float diff = Math.abs(d.pos.x - this.pos.x) + Math.abs(d.pos.y - this.pos.y);
			if (diff > epsilon) return false;
			return true;

		}

		public Dummy getKthNearestEnemy (int k) {
			perf.start();
			originDummy = this;
			Dummy found = enemy.selectRanked(distComp, k);
			// print(this + " found enemy: " + found);
			perf.stop();
			perf.tick();
			if (verify) {
				verifyCorrectness(found, k);
			}
			return found;
		}

		public void setRandomPos () {
			float max = 100;
			this.pos.x = -max + MathUtils.random(max * 2);
			this.pos.y = -max + MathUtils.random(max * 2);
			float xShift = 100;
			if (player.contains(this, true)) {
				this.pos.x -= xShift;
			} else if (enemy.contains(this, true)) {
				this.pos.x += xShift;
			} else {
				throw new RuntimeException("unhandled");
			}
		}

		@Override
		public String toString () {
			return String.format("Dummy at: %.2f, %.2f", pos.x, pos.y);
		}
	}

	public static int nextID = 1;
	public static Array<Dummy> player;
	public static Array<Dummy> enemy;

	public static Array<Dummy> createDummies (int n) {
		float variance = 20;
		Array<Dummy> dummies = new Array<Dummy>();
		for (int i = 0; i < n; i++) {
			Dummy d = new Dummy();
			dummies.add(d);
			d.pos = new Vector2();
			d.id = nextID++;
		}
		return dummies;
	}

	static Dummy originDummy;
	static long comparisonsMade = 0;
	static Comparator<Dummy> distComp = new Comparator<Dummy>() {
		@Override
		public int compare (Dummy o1, Dummy o2) {
			comparisonsMade++;
			float d1 = originDummy.pos.dst2(o1.pos);
			float d2 = originDummy.pos.dst2(o2.pos);
			float diff = d1 - d2;
			if (diff < 0) return -1;
			if (diff > 0) return 1;
			return 0;
		}
	};

	public static void print (Object... objs) {
		for (Object o : objs) {
			System.out.print(o);
		}
		System.out.println();
	}
}
