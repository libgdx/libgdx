/* Copyright (c) 2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.jar;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class JarFile extends ZipFile {
  public JarFile(String name) throws IOException {
    super(name);
  }

  public JarFile(File file) throws IOException {
    super(file);
  }

  public Enumeration<JarEntry> entries() {
    return (Enumeration<JarEntry>) makeEnumeration(JarEntryFactory.Instance);
  }

  public JarEntry getJarEntry(String name) {
    return (JarEntry) getEntry(JarEntryFactory.Instance, name);
  }

  private static class MyJarEntry extends JarEntry implements MyEntry {
    public final Window window;
    public final int pointer;

    public MyJarEntry(Window window, int pointer) {
      this.window = window;
      this.pointer = pointer;
    }

    public String getName() {
      try {
        return entryName(window, pointer);
      } catch (IOException e) {
        return null;
      }
    }

    public int getCompressedSize() {
      try {
        return compressedSize(window, pointer);
      } catch (IOException e) {
        return 0;
      }
    }

    public int getSize() {
      try {
        return uncompressedSize(window, pointer);
      } catch (IOException e) {
        return 0;
      }
    }

    public int pointer() {
      return pointer;
    }
  }

  private static class JarEntryFactory implements EntryFactory {
    public static final JarEntryFactory Instance = new JarEntryFactory();

    public ZipEntry makeEntry(Window window, int pointer) {
      return new MyJarEntry(window, pointer);
    }
  }
}
