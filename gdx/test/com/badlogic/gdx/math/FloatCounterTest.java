package com.badlogic.gdx.math;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import static java.lang.Math.pow;

public class FloatCounterTest {
    private FloatCounter counter1;
    private FloatCounter counter2;

    @Before
    public void setup() {
        counter1=new FloatCounter(5);
        counter2=new FloatCounter(-16);
    }

    @Test
    public void testNormalCases() {
        // Tests with a Windowed mean
        counter1.reset();
        Assert.assertEquals(0,counter1.count);
        Assert.assertEquals(0,counter1.total,0);
        Assert.assertEquals(Float.MAX_VALUE,counter1.min,0);
        Assert.assertEquals(-Float.MAX_VALUE,counter1.max,0);
        Assert.assertEquals(0,counter1.average,0);
        Assert.assertEquals(0,counter1.latest,0);
        Assert.assertEquals(0,counter1.value,0);
        Assert.assertEquals(0,counter1.mean.getMean(),0);
        Assert.assertEquals(0,counter1.mean.getValueCount(),0);
        Assert.assertEquals(0,counter1.mean.getLatest(),0);
        Assert.assertEquals(5,counter1.mean.getWindowSize());
        Assert.assertArrayEquals(new float[]{},counter1.mean.getWindowValues(),0);
        counter1.put(1.0f);
        counter1.put(4.0f);
        Assert.assertEquals(2.5f, counter1.average, 0);
        Assert.assertEquals(0.0f, counter1.value, 0);
        Assert.assertEquals(4.0f, counter1.latest, 0);
        Assert.assertEquals(5.0f, counter1.total, 0);
        Assert.assertEquals(2, counter1.count);
        Assert.assertEquals(-Float.MAX_VALUE, counter1.max, 0);
        Assert.assertEquals(Float.MAX_VALUE, counter1.min, 0);
        counter1.put(-40.0f);
        counter1.put(3.0f);
        counter1.put(6.0f);
        Assert.assertEquals(-26.0f, counter1.total, 0);
        Assert.assertEquals(5, counter1.count);
        Assert.assertEquals(6.0f, counter1.latest, 0);
        Assert.assertEquals(-5.2f, counter1.average, 0);
        Assert.assertEquals(-5.2f, counter1.value, 0);
        Assert.assertEquals(-5.2f, counter1.min, 0);
        Assert.assertEquals(-5.2f, counter1.max, 0);
        counter1.put(12.0f);
        Assert.assertEquals(-14.0f, counter1.total, 0);
        Assert.assertEquals(6, counter1.count);
        Assert.assertEquals(12.0f, counter1.latest, 0);
        Assert.assertEquals(-2.333333333f, counter1.average, 0.000000001f);
        Assert.assertEquals(-3.0f, counter1.value, 0);
        Assert.assertEquals(-5.2f, counter1.min, 0);
        Assert.assertEquals(-3.0f, counter1.max, 0);
        counter1.reset();
        Assert.assertEquals(0,counter1.count);
        Assert.assertEquals(0,counter1.total,0);
        Assert.assertEquals(Float.MAX_VALUE,counter1.min,0);
        Assert.assertEquals(-Float.MAX_VALUE,counter1.max,0);
        Assert.assertEquals(0,counter1.average,0);
        Assert.assertEquals(0,counter1.latest,0);
        Assert.assertEquals(0,counter1.value,0);
        Assert.assertEquals(0,counter1.mean.getMean(),0);
        Assert.assertEquals(0,counter1.mean.getValueCount(),0);
        Assert.assertEquals(0,counter1.mean.getLatest(),0);
        Assert.assertEquals(5,counter1.mean.getWindowSize());
        Assert.assertArrayEquals(new float[]{},counter1.mean.getWindowValues(),0);

        // Tests without a Windowed mean
        counter2.reset();
        Assert.assertNull(counter2.mean);
        Assert.assertEquals(0, counter2.count);
        Assert.assertEquals(0.0f, counter2.total,0);
        Assert.assertEquals(Float.MAX_VALUE, counter2.min,0);
        Assert.assertEquals(-Float.MAX_VALUE, counter2.max,0);
        Assert.assertEquals(0.0f, counter2.average,0);
        Assert.assertEquals(0.0f, counter2.latest,0);
        Assert.assertEquals(0.0f, counter2.value,0);
        counter2.put(1.0f);
        Assert.assertEquals(1.0f, counter2.latest, 0);
        Assert.assertEquals(1.0f, counter2.total, 0);
        Assert.assertEquals(1.0f, counter2.average, 0);
        Assert.assertEquals(1.0f, counter2.value, 0);
        Assert.assertEquals(1, counter2.count);
        Assert.assertEquals(1.0f, counter2.max, 0);
        Assert.assertEquals(1.0f, counter2.min, 0);
        counter2.put(34.0f);
        counter2.put(15.24f);
        counter2.put(43.62f);
        Assert.assertEquals(43.62f, counter2.latest, 0);
        Assert.assertEquals(93.86f, counter2.total, 0);
        Assert.assertEquals(23.465f, counter2.average, 0);
        Assert.assertEquals(43.62f, counter2.value, 0);
        Assert.assertEquals(4, counter2.count);
        Assert.assertEquals(43.62f, counter2.max, 0);
        Assert.assertEquals(1.0f, counter2.min, 0);
    }

    @Test
    public void testLimitCases() {
        // Tests with a Windowed mean
        counter1.reset();
        counter1.put(Float.MIN_NORMAL);
        Assert.assertEquals(1.17549435E-38f, counter1.latest, 0);
        counter1.put(10000.85f);
        Assert.assertEquals(10000.850000000000000000000000000000000000117549435f, counter1.total, 0);
        counter1.put(75.15f);
        counter1.put(7E18f);
        Assert.assertEquals(1750000000000002519.00000000000000000000000000000000000002938735875f, counter1.average, 0);
        counter1.put(5f);
        Assert.assertEquals(1400000000000002013.80000000000000000000000000000000000002350988700f, counter1.mean.getMean(), 0);

        // Tests without a Windowed mean
        counter2.reset();
        try {
            counter2.put(Float.NaN);
            Assert.fail();
        } catch (RuntimeException e) {
            // This should throw an exception as a FloatCounter cannot handle a NaN
        }
        counter2.put(0.000000000000001f);
        counter2.put(1e14f);
        Assert.assertEquals(100000000000000.000000000000001f, counter2.total, 0);
        Assert.assertEquals(50000000000000.0000000000000005f, counter2.average, 0);
        counter2.put(Float.MAX_VALUE);
        Assert.assertEquals(Float.MAX_VALUE, counter2.total,0);
        counter2.put(-Float.MAX_VALUE);
        /*
         Because we put the max value and the total cannot be more than max value (except infinity),
         the new total will be 0 (should we prohibit MAX_VALUE and INFINITY to avoid that?)
         */
        Assert.assertEquals(0f, counter2.total, 0);
        counter2.put(Float.POSITIVE_INFINITY);
        Assert.assertEquals(Float.POSITIVE_INFINITY, counter2.total, 0);
        counter2.put(Float.NEGATIVE_INFINITY);
        /*
        After implementing this next test, I realized that putting POSITIVE_INFINITY then NEGATIVE_INFINITY,
        the total was not a number (same as before, should this be prohibited ?)
         */
        Assert.assertEquals(Float.NaN, counter2.total, 0);
        /*
        Should there be modifications to the FloatCounter class, I will be changing these last tests to try and
        catch clauses with the different errors that could be thrown
         */
    }
}
