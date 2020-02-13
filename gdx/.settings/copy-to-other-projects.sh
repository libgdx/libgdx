#!/bin/bash

files=(org.eclipse.jdt.core.prefs org.eclipse.jdt.ui.prefs)
cp -t ../../backends/gdx-backend-headless/.settings $files
cp -t ../../backends/gdx-backend-lwjgl/.settings $files
cp -t ../../backends/gdx-backend-lwjgl3/.settings $files
cp -t ../../extensions/gdx-box2d/gdx-box2d/.settings $files
cp -t ../../extensions/gdx-bullet/.settings $files
cp -t ../../extensions/gdx-controllers/gdx-controllers/.settings $files
cp -t ../../extensions/gdx-controllers/gdx-controllers-desktop/.settings $files
cp -t ../../extensions/gdx-controllers/gdx-controllers-lwjgl3/.settings $files
cp -t ../../extensions/gdx-freetype/.settings $files
cp -t ../../extensions/gdx-jnigen/.settings $files
cp -t ../../extensions/gdx-tools/.settings $files
cp -t ../../tests/gdx-tests/.settings $files
cp -t ../../tests/gdx-tests-lwjgl/.settings $files
cp -t ../../tests/gdx-tests-lwjgl3/.settings $files
