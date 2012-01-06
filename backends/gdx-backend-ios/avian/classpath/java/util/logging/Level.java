/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.logging;

public class Level {
  public static final Level FINEST = new Level("FINEST", 300);
  public static final Level FINER = new Level("FINER", 400);
  public static final Level FINE = new Level("FINE", 500);
  public static final Level INFO = new Level("INFO", 800);
  public static final Level WARNING = new Level("WARNING", 900);
  public static final Level SEVERE = new Level("SEVERE", 1000);

  private final int value;
  private final String name;

  private Level(String name, int value) {
    this.name = name;
    this.value = value;
  }

  public int intValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }
}
