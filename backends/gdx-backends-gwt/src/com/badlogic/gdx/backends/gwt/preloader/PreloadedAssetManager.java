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

package com.badlogic.gdx.backends.gwt.preloader;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.gwt.GwtFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.dom.client.ImageElement;

import java.io.*;

public class PreloadedAssetManager {

    public ObjectMap<String, Void> directories = new ObjectMap<String, Void>();
    public ObjectMap<String, ImageElement> images = new ObjectMap<String, ImageElement>();
    public ObjectMap<String, Void> audio = new ObjectMap<String, Void>();
    public ObjectMap<String, String> texts = new ObjectMap<String, String>();
    public ObjectMap<String, Blob> binaries = new ObjectMap<String, Blob>();

    public InputStream read (String url) {
        if (texts.containsKey(url)) {
            try {
                return new ByteArrayInputStream(texts.get(url).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        if (images.containsKey(url)) {
            return new ByteArrayInputStream(new byte[1]); // FIXME, sensible?
        }
        if (binaries.containsKey(url)) {
            return binaries.get(url).read();
        }
        if (audio.containsKey(url)) {
            return new ByteArrayInputStream(new byte[1]); // FIXME, sensible?
        }
        return null;
    }

    public boolean contains (String url) {
        return texts.containsKey(url) || images.containsKey(url) || binaries.containsKey(url) || audio.containsKey(url) || directories.containsKey(url);
    }

    public boolean isText (String url) {
        return texts.containsKey(url);
    }

    public boolean isImage (String url) {
        return images.containsKey(url);
    }

    public boolean isBinary (String url) {
        return binaries.containsKey(url);
    }

    public boolean isAudio (String url) {
        return audio.containsKey(url);
    }

    public boolean isDirectory (String url) {
        return directories.containsKey(url);
    }

    private boolean isChild(String path, String url) {
        return path.startsWith(url) && (path.indexOf('/', url.length() + 1) < 0);
    }

    public FileHandle[] list (String url) {
        Array<FileHandle> files = new Array<FileHandle>();
        for (String path : texts.keys()) {
            if (isChild(path, url)) {
                files.add(new GwtFileHandle(this, path, Files.FileType.Internal));
            }
        }
        FileHandle[] list = new FileHandle[files.size];
        System.arraycopy(files.items, 0, list, 0, list.length);
        return list;
    }

    public FileHandle[] list (String url, FileFilter filter) {
        Array<FileHandle> files = new Array<FileHandle>();
        for (String path : texts.keys()) {
            if (isChild(path, url) && filter.accept(new File(path))) {
                files.add(new GwtFileHandle(this, path, Files.FileType.Internal));
            }
        }
        FileHandle[] list = new FileHandle[files.size];
        System.arraycopy(files.items, 0, list, 0, list.length);
        return list;
    }

    public FileHandle[] list (String url, FilenameFilter filter) {
        Array<FileHandle> files = new Array<FileHandle>();
        for (String path : texts.keys()) {
            if (isChild(path, url) && filter.accept(new File(url), path.substring(url.length() + 1))) {
                files.add(new GwtFileHandle(this, path, Files.FileType.Internal));
            }
        }
        FileHandle[] list = new FileHandle[files.size];
        System.arraycopy(files.items, 0, list, 0, list.length);
        return list;
    }

    public FileHandle[] list (String url, String suffix) {
        Array<FileHandle> files = new Array<FileHandle>();
        for (String path : texts.keys()) {
            if (isChild(path, url) && path.endsWith(suffix)) {
                files.add(new GwtFileHandle(this, path, Files.FileType.Internal));
            }
        }
        FileHandle[] list = new FileHandle[files.size];
        System.arraycopy(files.items, 0, list, 0, list.length);
        return list;
    }

    public long length (String url) {
        if (texts.containsKey(url)) {
            try {
                return texts.get(url).getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                return texts.get(url).getBytes().length;
            }
        }
        if (images.containsKey(url)) {
            return 1; // FIXME, sensible?
        }
        if (binaries.containsKey(url)) {
            return binaries.get(url).length();
        }
        if (audio.containsKey(url)) {
            return 1; // FIXME sensible?
        }
        return 0;
    }
}
