
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.graphics.Pixmap.*;
import static com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.*;

/** Test which image formats are supported on the current backend. Not all will be, so errors are normal. This is not an
 * exhaustive test! Many image formats have different modes, bit depths, support for color profiles, etc. */
public class ImageFormatTest extends GdxTest {

	private Stage stage;
	private final String pathPrefix = "data/format-test/";

	// @off
	private final String[][] filenames = {
		{ // Widely supported
			"bmp.bmp",
			"gif.gif", // Static
			"jpeg-gray.jpg",
			"jpeg-ycbcr.jpg",
			"png-gray.png",
			"png-rgb.png",
		},
		{ // libGDX and GPU formats
			"cim.cim",
			"etc1.etc1",
			"ktx-rgb.ktx",
			"zktx-rgb.zktx",
			"ktx-etc1.ktx", // Mobile hardware support
			"ktx-s3tc.ktx", // Desktop hardware support
		},
		{ // Via stb_image
			"hdr.hdr",
			"pic.pic",
			"pnm.pnm",
			"psd.psd",
			"tga.tga",
		},
		{ // Web only
			"avif.avif", // Baseline January 2024
			"cur.cur",
			"ico.ico",
			"jxl.jxl", // Support rolling out as of 2026
			"svg.svg", // Baseline January 2020
			"webp.webp", // Baseline September 2020
		},
		{ // Safari
			"heif.heif", // As of version 17
			"pdf.pdf", // Confirmed on iOS 15 (sane browsers don't treat PDFs as images)
			"tiff.tiff", // Confirmed on iOS 15
		},
	};
	// @on

	@Override
	public void create () {
		Gdx.graphics.setContinuousRendering(false);
		stage = new Stage(new FitViewport(640, 480));
		Gdx.input.setInputProcessor(stage);
		int spacePx = 6;

		Table root = new Table();
		root.align(Align.topLeft);
		root.pad(8);

		for (String[] row : filenames) {
			HorizontalGroup group = new HorizontalGroup();
			group.wrap();
			group.space(spacePx).wrapSpace(spacePx);
			group.align(Align.topLeft).rowAlign(Align.topLeft);

			for (String filename : row) {
				try {
					if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
						if (filename.contains("ktx-etc1") && !Gdx.graphics.supportsExtension("WEBGL_compressed_texture_etc1"))
							throw new GdxRuntimeException("ETC1 not supported");
						if (filename.contains("ktx-s3tc") && !Gdx.graphics.supportsExtension("WEBGL_compressed_texture_s3tc_srgb"))
							throw new GdxRuntimeException("S3TC not supported");
					}
					FileHandle handle = Gdx.files.internal(pathPrefix + filename);
					Image image = new Image(new Texture(handle, Format.RGBA8888, false));
					group.addActor(image);
				} catch (Exception e) {
					Container<Actor> gap = new Container<>();
					gap.prefSize(64);
					group.addActor(gap);
					Gdx.app.error("ImageFormatTest", e.getMessage());
				}
			}

			root.add(group).expandX().fillX().left().padBottom(spacePx).row();
		}

		ScrollPane scrollPane = new ScrollPane(root, new ScrollPaneStyle());
		scrollPane.setFillParent(true);
		stage.addActor(scrollPane);
	}

	@Override
	public void render () {
		ScreenUtils.clear(Color.GRAY);
		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height);
	}

}
