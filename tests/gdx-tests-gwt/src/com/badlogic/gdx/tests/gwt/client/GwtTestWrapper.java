
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.MultipleRenderTargetTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.g3d.TextureArrayTest;
import com.badlogic.gdx.tests.gles2.VertexArrayTest;
import com.badlogic.gdx.tests.gles3.GL30Texture3DTest;
import com.badlogic.gdx.tests.gles3.NonPowerOfTwoTest;
import com.badlogic.gdx.tests.gles3.UniformBufferObjectsTest;
import com.badlogic.gdx.tests.gles3.InstancedRenderingTest;
import com.badlogic.gdx.tests.gwt.GwtInputTest;
import com.badlogic.gdx.tests.gwt.GwtWindowModeTest;
import com.badlogic.gdx.tests.math.CollisionPlaygroundTest;
import com.badlogic.gdx.tests.math.OctreeTest;
import com.badlogic.gdx.tests.math.collision.OrientedBoundingBoxTest;
import com.badlogic.gdx.tests.net.OpenBrowserExample;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;

import java.util.ArrayList;

public class GwtTestWrapper extends AbstractTestWrapper {
	@Override
	protected AbstractTestWrapper.Instancer[] getTestList () {
		ArrayList<Instancer> tests = new ArrayList<>();

		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AccelerometerTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ActionTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ActionSequenceTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AlphaTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AnimationTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AnnotationTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AssetManagerTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new AtlasIssueTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BigMeshTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontAlignmentTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontFlipTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BitmapFontMetricsTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BlitTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DCharacterControllerTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new Box2DTestCollection();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new BufferUtilsTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ClipboardTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ColorTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new CollisionPlaygroundTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ComplexActionTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new CustomShaderSpriteBatchTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new DecalTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new DisplayModeTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new LabelScaleTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new EdgeDetectionTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new FilesTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new FilterPerformanceTest();
			}
		});
// new GwtInstancer() {public GdxTest instance(){return new FlickScrollPaneTest();}}, // FIXME this messes up stuff, why?
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new FrameBufferTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new DownloadTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new FramebufferToTextureTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GestureDetectorTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GLProfilerErrorTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GroupCullingTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GroupFadeTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GwtInputTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GwtWindowModeTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new I18NSimpleMessageTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ImageScaleTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ImageTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new IndexBufferObjectShaderTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new IntegerBitmapFontTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new InterpolationTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new InverseKinematicsTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new IsometricTileTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new KinematicBodyTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new LifeCycleTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new LabelTest();
			}
		});
		// new GwtInstancer() {public GdxTest instance(){return new MatrixJNITest();}}, // No purpose
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new MeshShaderTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new MeshWithCustomAttributesTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new MipMapTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ModelCacheTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new MultitouchTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new MusicTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new OctreeTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new OpenBrowserExample();
			}
		});
// tests.add(new GwtInstancer() { public GdxTest instance () { return new NoncontinuousRenderingTest(); } // FIXME doesn't compile
// due to
// the use of Thread
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new OrientedBoundingBoxTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ParallaxTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ParticleEmitterTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new PixelsPerInchTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new PixmapPackerTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new PixmapTest();
			}
		});
		// new GwtInstancer() {public GdxTest instance(){return new PixmapBlendingTest();}}, // FIXME no idea why this doesn't
		// work
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new PreferencesTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ProjectiveTextureTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new RotationTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ReflectionCorrectnessTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new Scene2dTest();
			}
		});

// new GwtInstancer() {public GdxTest instance(){return new RunnablePostTest();}}, // Goes into infinite loop
// new GwtInstancer() {public GdxTest instance(){return new ScrollPaneTest();}}, // FIXME this messes up stuff, why?
// new GwtInstancer() {public GdxTest instance(){return new ShaderMultitextureTest();}}, // FIXME fucks up stuff
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ShadowMappingTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ShapeRendererTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SimpleAnimationTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SimpleDecalTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SimpleStageCullingTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SortedSpriteTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SpriteBatchShaderTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SpriteCacheOffsetTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SpriteCacheTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SoundTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new StageTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SystemCursorTest();
			}
		});
		// new GwtInstancer() {public GdxTest instance(){return new StagePerformanceTest();}}, // FIXME borks out
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TableTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextButtonTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextButtonTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextureAtlasTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TiledMapObjectLoadingTest();
			}
		});
		tests.add(new GwtInstancer() {
			@Override
			public GdxTest instance() {
				return new TiledMapObjectPropertyTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new UITest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new VertexBufferObjectShaderTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new YDownTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new SuperKoalio();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new ReflectionTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TiledMapAtlasAssetManagerTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TimeUtilsTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new GWTLossyPremultipliedAlphaTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new QuadTreeFloatTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new QuadTreeFloatNearestTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextAreaTest();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextAreaTest2();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new TextAreaTest3();
			}
		});
		tests.add(new GwtInstancer() {
			public GdxTest instance () {
				return new VertexArrayTest();
			}
		});
		// these may have issues with tab getting intercepted by the browser

		// Add the GL30 tests if applicable
		if (Gdx.graphics.isGL30Available()) {
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new FloatTextureTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new GL30Texture3DTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new InstancedRenderingTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new MultipleRenderTargetTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new NonPowerOfTwoTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new TextureArrayTest();
				}
			});
			tests.add(new GwtInstancer() {
				public GdxTest instance () {
					return new UniformBufferObjectsTest();
				}
			});
		}

		Instancer[] testArr = new Instancer[tests.size()];
		tests.toArray(testArr);

		return testArr;
	}

	abstract static class GwtInstancer implements AbstractTestWrapper.Instancer {
		@Override
		public String getSimpleName () {
			return instance().getClass().getSimpleName();
		}
	}
}
