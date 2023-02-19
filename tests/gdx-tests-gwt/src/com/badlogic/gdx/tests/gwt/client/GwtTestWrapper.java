
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.gles2.VertexArrayTest;
import com.badlogic.gdx.tests.gwt.GwtInputTest;
import com.badlogic.gdx.tests.gwt.GwtWindowModeTest;
import com.badlogic.gdx.tests.math.CollisionPlaygroundTest;
import com.badlogic.gdx.tests.math.OctreeTest;
import com.badlogic.gdx.tests.math.collision.OrientedBoundingBoxTest;
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
				return new CollisionPlaygroundTest();
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
				return new DecalAlphaTest();
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
					return new MeshWithCustomAttributesTest();
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
					return new OrientedBoundingBoxTest();
				}
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
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new SystemCursorTest();
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
			}, new GwtInstancer() {
				public GdxTest instance () {
					return new VertexArrayTest();
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
