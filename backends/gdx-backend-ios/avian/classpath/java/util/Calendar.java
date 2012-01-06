/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public abstract class Calendar {
  public static final int AM = 0;
  public static final int AM_PM = 9;
  public static final int DAY_OF_MONTH = 5;
  public static final int DAY_OF_WEEK = 7;
  public static final int HOUR = 10;
  public static final int HOUR_OF_DAY = 11;
  public static final int MINUTE = 12;
  public static final int MONTH = 2;
  public static final int PM = 1;
  public static final int SECOND = 13;
  public static final int YEAR = 1;

  public static final int FIELD_COUNT = 17;

  protected long time;
  protected boolean isTimeSet;
  protected int[] fields = new int[FIELD_COUNT];
  protected boolean areFieldsSet;
  protected boolean[] isSet = new boolean[FIELD_COUNT];

  protected Calendar() { }
 
  public static Calendar getInstance() {
    return new MyCalendar(System.currentTimeMillis());
  }

  public int get(int field) {
    return fields[field];
  }

  public void set(int field, int value) {
    fields[field] = value;
  }

  public void set(int year, int month, int date) {
    set(YEAR, year);
    set(MONTH, month);
    set(DAY_OF_MONTH, date);
  }

  public void setTime(Date date) {
    time = date.getTime();
  }

  public abstract void roll(int field, boolean up);
  public abstract void add(int field, int amount);

  public void roll(int field, int amount) {
    boolean up = amount >= 0;
    if (! up) {
      amount = - amount;
    }
    for (int i = 0; i < amount; ++i) {
      roll(field, up);
    } 
  }

  public abstract int getMinimum(int field);

  public abstract int getMaximum(int field);
 
  public abstract int getActualMinimum(int field);

  public abstract int getActualMaximum(int field);

  private static class MyCalendar extends Calendar {
    private static final long MILLIS_PER_DAY = 86400000;
    private static final int MILLIS_PER_HOUR = 3600000;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int MILLIS_PER_SECOND = 1000;

    private static final int EPOCH_YEAR = 1970;
    private static final int EPOCH_LEAP_YEAR = 1968;
    private static final int DAYS_TO_EPOCH = 731;

    private static final int[][] DAYS_IN_MONTH = new int[][] {
      { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
      { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }
    };

    public MyCalendar(long time) {
      this.time = time;
      this.isTimeSet = true;
      parseIntoFields(time);
    }

    public void setTime(Date date) {
      super.setTime(date);
      parseIntoFields(this.time);
    }

    private static boolean isLeapYear(int year) {
      return (year%4 == 0) && (year%100 != 0) || (year%400 == 0);
    }
    
    private void parseIntoFields(long timeInMillis) {
      long days = timeInMillis / MILLIS_PER_DAY;
      /* convert days since Jan 1, 1970 to days since Jan 1, 1968 */
      days += DAYS_TO_EPOCH;
      long years = 4 * days / 1461; /* days/365.25 = 4*days/(4*365.25) */
      int year = (int)(EPOCH_LEAP_YEAR + years);
      days -= 365 * years + years / 4;
      if (!isLeapYear(year)) days--;
      
      int month=0;
      int leapIndex = isLeapYear(year) ? 1 : 0;
      while (days >= DAYS_IN_MONTH[leapIndex][month]) {
        days -= DAYS_IN_MONTH[leapIndex][month++];
      }
      days++;

      int remainder = (int)(timeInMillis % MILLIS_PER_DAY);
      int hour = remainder / MILLIS_PER_HOUR;
      remainder = remainder % MILLIS_PER_HOUR;
      int minute = remainder / MILLIS_PER_MINUTE;
      remainder = remainder % MILLIS_PER_MINUTE;
      int second = remainder / MILLIS_PER_SECOND;
      fields[YEAR] = year;
      fields[MONTH] = month;
      fields[DAY_OF_MONTH] = (int)days;
      fields[HOUR_OF_DAY] = hour;
      fields[MINUTE] = minute;
      fields[SECOND] = second;
    }
    
    public void roll(int field, boolean up) {
      // todo
    }

    public void add(int fild, int amount) {
      // todo
    }

    public int getMinimum(int field) {
      // todo
      return 0;
    }

    public int getMaximum(int field) {
      // todo
      return 0;
    }
 
    public int getActualMinimum(int field) {
      // todo
      return 0;
    }

    public int getActualMaximum(int field) {
      // todo
      return 0;
    }
  }
}
