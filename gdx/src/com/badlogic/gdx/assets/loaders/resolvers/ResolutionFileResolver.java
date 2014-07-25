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

package com.badlogic.gdx.assets.loaders.resolvers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/** File resolver based on screen resolution. Supported matching strategy @see {@link MatchStrategy}. Supported @link
 * {@link Resolution} {@link Metric} are Pixels and Density Pixels */
public class ResolutionFileResolver implements FileHandleResolver {

	/** Resolution metric, Either PX:resolution in pixels, or DP resolution in density pixels
	 * @see <a
	 *      href="http://stackoverflow.com/questions/7587854/is-there-a-list-of-screen-resolutions-for-all-android-based-phones-and-tablets">list-of-screen-resolutions</a> */
	public static enum Metric {
		PX, DP
	};

	public static class Resolution {
		public final int portraitWidth;
		public final int portraitHeight;
		public final String suffix;

		public Resolution (int portraitWidth, int portraitHeight, String suffix) {
			this.portraitWidth = portraitWidth;
			this.portraitHeight = portraitHeight;
			this.suffix = suffix;
		}

	}

	/** defines how to choose the matching resolution */
	public interface MatchStrategy {
		public abstract Resolution choose (int w, int h, Resolution... descriptors);

		/** chooses the closest descriptor dimension to the given screen width and height */
		public static MatchStrategy bestMatch = new MatchStrategy() {

			@Override
			public Resolution choose (int w, int h, Resolution... descriptors) {
				if (descriptors == null) throw new IllegalArgumentException("descriptors cannot be null.");

				if (w > h) { // landscape mode
					// swap width and height
					w = w + h;
					h = w - h;
					w = w - h;
				}

				Resolution best = descriptors[0];
				double bestDifference = (1 << 25); // any large number

				for (int i = 0, n = descriptors.length; i < n; i++) {
					Resolution other = descriptors[i];

					// normalized difference between current dimensions and
					// descriptor dimensions
					double normalizedDiff = Math.abs(other.portraitWidth - w) / (1f * w) + Math.abs(other.portraitHeight - h)
						/ (1f * h);

					if (normalizedDiff < bestDifference) {
						bestDifference = normalizedDiff;
						best = descriptors[i];
					}
				}
				return best;
			}
		};

		/** chooses the closest descriptor that is lower than the given screen width and height */
		public static MatchStrategy lowerMatch = new MatchStrategy() {

			@Override
			public Resolution choose (int w, int h, Resolution... descriptors) {
				if (descriptors == null) throw new IllegalArgumentException("descriptors cannot be null.");

				if (w > h) { // landscape mode
					// swap width and height
					w = w + h;
					h = w - h;
					w = w - h;
				}

				Resolution best = descriptors[0];

				for (int i = 0, n = descriptors.length; i < n; i++) {
					Resolution other = descriptors[i];
					if (w <= other.portraitWidth && other.portraitWidth <= best.portraitWidth && h <= other.portraitHeight
						&& other.portraitHeight <= best.portraitHeight) best = descriptors[i];
				}
				return best;
			}
		};

		/** chooses the closest descriptor that is higher than the given screen width and height */
		public static MatchStrategy higherMatch = new MatchStrategy() {

			@Override
			public Resolution choose (int w, int h, Resolution... descriptors) {
				if (descriptors == null) throw new IllegalArgumentException("descriptors cannot be null.");

				if (w > h) { // landscape mode
					// swap width and height
					w = w + h;
					h = w - h;
					w = w - h;
				}

				Resolution best = descriptors[0];

				for (int i = 0, n = descriptors.length; i < n; i++) {
					Resolution other = descriptors[i];
					if (w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth && h >= other.portraitHeight
						&& other.portraitHeight >= best.portraitHeight) best = descriptors[i];
				}
				return best;
			}
		};

	}

	protected final FileHandleResolver baseResolver;
	protected final Resolution[] descriptors;
	protected final MatchStrategy matchStrategy;
	protected final Metric metric;

	/** @param baseResolver
	 * @param descriptors Chooses the matching resources size based on the given {@link MatchStrategy}. Default strategy is
	 *           {@link MatchStrategy#bestMatch} and default {@link Metric} is PX (pixel resolution) */
	public ResolutionFileResolver (FileHandleResolver baseResolver, Resolution... descriptors) {
		this(baseResolver, MatchStrategy.bestMatch, Metric.PX, descriptors);
	}

	/** @param baseResolver
	 * @param matchStrategy
	 * @param descriptors Chooses the matching resources size based on the given {@link MatchStrategy}. Default {@link Metric} is
	 *           PX (pixel resolution) */
	public ResolutionFileResolver (FileHandleResolver baseResolver, MatchStrategy matchStrategy, Resolution... descriptors) {
		this(baseResolver, matchStrategy, Metric.PX, descriptors);
	}

	/** @param baseResolver
	 * @param metric
	 * @param descriptors
	 * 
	 *           Chooses the matching resources size based on the given {@link MatchStrategy}. Default {@link Metric} is PX (pixel
	 *           resolution) */
	public ResolutionFileResolver (FileHandleResolver baseResolver, Metric metric, Resolution... descriptors) {
		this(baseResolver, MatchStrategy.bestMatch, Metric.PX, descriptors);
	}

	/** @param baseResolver
	 * @param matchStragey
	 * @param metric
	 * @param descriptors
	 * 
	 *           Chooses the matching resources size based on the given {@link Metric}. Default strategy is
	 *           {@link MatchStrategy#bestMatch}. */
	public ResolutionFileResolver (FileHandleResolver baseResolver, MatchStrategy matchStragey, Metric metric,
		Resolution... descriptors) {
		this.baseResolver = baseResolver;
		this.matchStrategy = matchStragey;
		this.metric = metric;
		this.descriptors = descriptors;
	}

	@Override
	public FileHandle resolve (String fileName) {
		int w, h;
		if (metric.equals(Metric.PX)) {
			w = Gdx.graphics.getWidth();
			h = Gdx.graphics.getHeight();
		} else if (metric.equals(Metric.DP)) {
			w = (int)Gdx.graphics.getPpiX();
			h = (int)Gdx.graphics.getPpiY();
		} else {
			throw new IllegalStateException("Provided metric is not supported, Supported metrics are PX and DP inch");
		}

		Resolution bestDesc = matchStrategy.choose(w, h, descriptors);

// System.out.println(bestDesc.portraitWidth+" "+bestDesc.portraitHeight);

		FileHandle originalHandle = new FileHandle(fileName);
		FileHandle handle = baseResolver.resolve(resolve(originalHandle, bestDesc.suffix));
		if (!handle.exists()) handle = baseResolver.resolve(fileName);
		return handle;
	}

	protected String resolve (FileHandle originalHandle, String suffix) {
		String parentString = "";
		FileHandle parent = originalHandle.parent();
		if (parent != null && !parent.name().equals("")) {
			parentString = parent + "/";
		}
		return parentString + suffix + "/" + originalHandle.name();
	}

}
