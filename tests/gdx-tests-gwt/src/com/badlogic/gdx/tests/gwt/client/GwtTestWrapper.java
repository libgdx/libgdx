
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.tests.AbstractTestWrapper;
import com.badlogic.gdx.tests.AccelerometerTest;
import com.badlogic.gdx.tests.ActionSequenceTest;
import com.badlogic.gdx.tests.ActionTest;
import com.badlogic.gdx.tests.AlphaTest;
import com.badlogic.gdx.tests.AnimationTest;
import com.badlogic.gdx.tests.AnnotationTest;
import com.badlogic.gdx.tests.AssetManagerTest;
import com.badlogic.gdx.tests.AtlasIssueTest;
import com.badlogic.gdx.tests.BigMeshTest;
import com.badlogic.gdx.tests.BitmapFontAlignmentTest;
import com.badlogic.gdx.tests.BitmapFontFlipTest;
import com.badlogic.gdx.tests.BitmapFontMetricsTest;
import com.badlogic.gdx.tests.BitmapFontTest;
import com.badlogic.gdx.tests.BlitTest;
import com.badlogic.gdx.tests.Box2DCharacterControllerTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.BufferUtilsTest;
import com.badlogic.gdx.tests.ClipboardTest;
import com.badlogic.gdx.tests.ColorTest;
import com.badlogic.gdx.tests.ComplexActionTest;
import com.badlogic.gdx.tests.CustomShaderSpriteBatchTest;
import com.badlogic.gdx.tests.DecalTest;
import com.badlogic.gdx.tests.DownloadTest;
import com.badlogic.gdx.tests.EdgeDetectionTest;
import com.badlogic.gdx.tests.FilesTest;
import com.badlogic.gdx.tests.FilterPerformanceTest;
import com.badlogic.gdx.tests.FrameBufferTest;
import com.badlogic.gdx.tests.FramebufferToTextureTest;
import com.badlogic.gdx.tests.GLProfilerErrorTest;
import com.badlogic.gdx.tests.GWTLossyPremultipliedAlphaTest;
import com.badlogic.gdx.tests.GestureDetectorTest;
import com.badlogic.gdx.tests.GroupCullingTest;
import com.badlogic.gdx.tests.GroupFadeTest;
import com.badlogic.gdx.tests.I18NSimpleMessageTest;
import com.badlogic.gdx.tests.ImageScaleTest;
import com.badlogic.gdx.tests.ImageTest;
import com.badlogic.gdx.tests.IndexBufferObjectShaderTest;
import com.badlogic.gdx.tests.IntegerBitmapFontTest;
import com.badlogic.gdx.tests.InterpolationTest;
import com.badlogic.gdx.tests.InverseKinematicsTest;
import com.badlogic.gdx.tests.IsometricTileTest;
import com.badlogic.gdx.tests.KinematicBodyTest;
import com.badlogic.gdx.tests.LabelScaleTest;
import com.badlogic.gdx.tests.LabelTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MipMapTest;
import com.badlogic.gdx.tests.MultitouchTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.ParallaxTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.PixmapPackerTest;
import com.badlogic.gdx.tests.PixmapTest;
import com.badlogic.gdx.tests.PreferencesTest;
import com.badlogic.gdx.tests.ProjectiveTextureTest;
import com.badlogic.gdx.tests.QuadTreeFloatNearestTest;
import com.badlogic.gdx.tests.QuadTreeFloatTest;
import com.badlogic.gdx.tests.ReflectionCorrectnessTest;
import com.badlogic.gdx.tests.ReflectionTest;
import com.badlogic.gdx.tests.RotationTest;
import com.badlogic.gdx.tests.Scene2dTest;
import com.badlogic.gdx.tests.ShapeRendererTest;
import com.badlogic.gdx.tests.SimpleAnimationTest;
import com.badlogic.gdx.tests.SimpleDecalTest;
import com.badlogic.gdx.tests.SimpleStageCullingTest;
import com.badlogic.gdx.tests.SortedSpriteTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteCacheOffsetTest;
import com.badlogic.gdx.tests.SpriteCacheTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TableTest;
import com.badlogic.gdx.tests.TextAreaTest;
import com.badlogic.gdx.tests.TextAreaTest2;
import com.badlogic.gdx.tests.TextAreaTest3;
import com.badlogic.gdx.tests.TextButtonTest;
import com.badlogic.gdx.tests.TextureAtlasTest;
import com.badlogic.gdx.tests.TiledMapAtlasAssetManagerTest;
import com.badlogic.gdx.tests.TiledMapObjectLoadingTest;
import com.badlogic.gdx.tests.TimeUtilsTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.YDownTest;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.gwt.GwtInputTest;
import com.badlogic.gdx.tests.gwt.GwtWindowModeTest;
import com.badlogic.gdx.tests.math.OctreeTest;
import com.badlogic.gdx.tests.net.OpenBrowserExample;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtTestWrapper extends AbstractTestWrapper {
	@Override
	protected AbstractTestWrapper.Instancer[] getTestList () {
		Instancer[] tests = {new GwtInstancer() {
			public GdxTest instance () {
				return new AccelerometerTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new ActionTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new ActionSequenceTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new AlphaTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new AnimationTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new AnnotationTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new AssetManagerTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new AtlasIssueTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BigMeshTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontAlignmentTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontFlipTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontMetricsTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BlitTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DCharacterControllerTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DTestCollection();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new BufferUtilsTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new ClipboardTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new ColorTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new ComplexActionTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new CustomShaderSpriteBatchTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new DecalTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new DisplayModeTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new LabelScaleTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new EdgeDetectionTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new FilesTest();
			}
		}, new GwtInstancer() {
			public GdxTest instance () {
				return new FilterPerformanceTest();
			}
		},
// new GwtInstancer() {public GdxTest instance(){return new FlickScrollPaneTest();}}, // FIXME this messes up stuff, why?
			new GwtInstancer() {
				public GdxTest instance () {
					return new FrameBufferTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new DownloadTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new FramebufferToTextureTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GestureDetectorTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GLProfilerErrorTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GroupCullingTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GroupFadeTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GwtInputTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GwtWindowModeTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new I18NSimpleMessageTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ImageScaleTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ImageTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new IndexBufferObjectShaderTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new IntegerBitmapFontTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new InterpolationTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new InverseKinematicsTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new IsometricTileTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new KinematicBodyTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new LifeCycleTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new LabelTest();
				}
			},
			// new GwtInstancer() {public GdxTest instance(){return new MatrixJNITest();}}, // No purpose
			new GwtInstancer() {
				public GdxTest instance () {
					return new MeshShaderTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new MipMapTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ModelCacheTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new MultitouchTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new MusicTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new OctreeTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new OpenBrowserExample();
				}
// }, new GwtInstancer() { public GdxTest instance () { return new NoncontinuousRenderingTest(); } // FIXME doesn't compile due to
// the use of Thread
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ParallaxTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ParticleEmitterTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new PixelsPerInchTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new PixmapPackerTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new PixmapTest();
				}
			},
			// new GwtInstancer() {public GdxTest instance(){return new PixmapBlendingTest();}}, // FIXME no idea why this doesn't
			// work
			new GwtInstancer() {
				public GdxTest instance () {
					return new PreferencesTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ProjectiveTextureTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new RotationTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ReflectionCorrectnessTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new Scene2dTest();
				}

// new GwtInstancer() {public GdxTest instance(){return new RunnablePostTest();}}, // Goes into infinite loop
// new GwtInstancer() {public GdxTest instance(){return new ScrollPaneTest();}}, // FIXME this messes up stuff, why?
// new GwtInstancer() {public GdxTest instance(){return new ShaderMultitextureTest();}}, // FIXME fucks up stuff
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ShadowMappingTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ShapeRendererTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SimpleAnimationTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SimpleDecalTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SimpleStageCullingTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SortedSpriteTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SpriteBatchShaderTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SpriteCacheOffsetTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SpriteCacheTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SoundTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new StageTest();
				}
			},
			// new GwtInstancer() {public GdxTest instance(){return new StagePerformanceTest();}}, // FIXME borks out
			new GwtInstancer() {
				public GdxTest instance () {
					return new TableTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextButtonTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextButtonTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextureAtlasTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TiledMapObjectLoadingTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new UITest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new VertexBufferObjectShaderTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new YDownTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SuperKoalio();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new ReflectionTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TiledMapAtlasAssetManagerTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TimeUtilsTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new GWTLossyPremultipliedAlphaTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new QuadTreeFloatTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new QuadTreeFloatNearestTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextAreaTest();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextAreaTest2();
				}
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new TextAreaTest3();
				}
			} // these may have issues with tab getting intercepted by the browser
		};

		return tests;
	}

	abstract static class GwtInstancer implements Instancer {
		@Override
		public String getSimpleName () {
			return instance().getClass().getSimpleName();
		}
	}
}
