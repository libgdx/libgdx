/*
 * Copyright 2024 See AUTHORS file
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

public class LongArrayTest {
    @Test
    public void addTest() {
        /*
        Test of the classic add(long value) method, it should be adding a number at the first available place.
         */
        LongArray longArray1=new LongArray(3);
        longArray1.add(3);
        Assert.assertArrayEquals(new long[]{3},longArray1.toArray());

        /*
        Test of the add(long value1,long value2) method, it should be adding these numbers in the array,
        when it is possible.
         */
        LongArray longArray2=new LongArray();
        longArray2.add(1,2);
        Assert.assertArrayEquals(new long[]{1,2},longArray2.toArray());

        /*
        Some test with the addAll(LongArray array) and addAll (long... array) methods. The second call should resize
        longArray3 to a size of 17.
         */
        LongArray longArray3=new LongArray();
        longArray3.addAll(longArray2);
        Assert.assertArrayEquals(longArray2.toArray(),longArray3.toArray());
        longArray3.addAll(longArray1);
        Assert.assertArrayEquals(new long[]{1,2,3},longArray3.toArray());
        longArray3.addAll(new long[]{4,5,6,2,8,10,1,6,2,3,30,31,25,20});
        Assert.assertEquals(17,longArray3.size);
        Assert.assertArrayEquals(new long[]{1,2,3,4,5,6,2,8,10,1,6,2,3,30,31,25,20},longArray3.toArray());

        /*
        Test of the method addAll(long[] array, int offset, int length) that adds the numbers contained between
        index offset to offset+length in array to longArray4.
         */
        LongArray longArray4=new LongArray();
        longArray4.addAll(new long[]{4,5,6,2,21,45,78},3,3);
        Assert.assertArrayEquals(new long[]{2,21,45},longArray4.toArray());
    }

    @Test
    public void getTest() {
        LongArray longArray=new LongArray();
        longArray.add(3,4,5,1);
        Assert.assertEquals(3,longArray.get(0));
        try {
            longArray.get(9);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // We should get here as we want to get an index that's out of bounds.
        }
    }

    @Test
    public void setTest() {
        LongArray longArray=new LongArray(new long[]{3,4,5,7});
        longArray.set(1,51);
        Assert.assertEquals(51,longArray.get(1));
        try {
            longArray.set(5,8);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // We should get here as we are trying to set the value of an index that's out of bounds.
        }
    }

    @Test
    public void incrTest() {
        LongArray longArray=new LongArray(new long[]{3,4,5,1,56,32});
        longArray.incr(3,45);
        Assert.assertEquals(46,longArray.get(3));
        longArray.incr(3);
        Assert.assertArrayEquals(new long[]{6,7,8,49,59,35},longArray.toArray());
        try {
            longArray.incr(28,4);
            Assert.fail();
        } catch(IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to increase at an index out of bounds
        }
    }

    @Test
    public void mulTest() {
        LongArray longArray=new LongArray(new long[]{3,4,5,1,56,32});
        longArray.mul(1,3);
        Assert.assertEquals(12,longArray.get(1));
        longArray.mul(2);
        Assert.assertArrayEquals(new long[]{6,24,10,2,112,64},longArray.toArray());
        try {
            longArray.mul(17,8);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to multiply at an index out of bounds
        }
    }

    @Test
    public void insertTest() {
        // With an ordered array
        LongArray longArray1=new LongArray();
        longArray1.addAll(new long[]{1,3,4,5,6});
        longArray1.insert(1,2);
        Assert.assertArrayEquals(new long[]{1,2,3,4,5,6},longArray1.toArray());
        longArray1.insertRange(2,3);
        Assert.assertArrayEquals(new long[]{1,2,3,4,5,3,4,5,6},longArray1.toArray());
        try {
            longArray1.insertRange(400,4);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to insert at an index out of bounds
        }

        // With an unordered array
        LongArray longArray2=new LongArray(false,16);
        longArray2.addAll(new long[]{1,3,4,5,6});
        longArray2.insert(1,2);
        Assert.assertArrayEquals(new long[]{1,2,4,5,6,3},longArray2.toArray());
        try {
            longArray2.insert(2783,3);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to insert at an index out of bounds
        }
    }

    @Test
    public void swapTest() {
        LongArray longArray1=new LongArray(new long[]{1,3,4,5,6});
        longArray1.swap(1,4);
        Assert.assertArrayEquals(new long[]{1,6,4,5,3},longArray1.toArray());
        try {
            longArray1.swap(100,3);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to swap at an index out of bounds
        }
        try {
            longArray1.swap(3,100);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to swap at an index out of bounds
        }
    }

    @Test
    public void containsTest() {
        LongArray longArray1=new LongArray(new long[]{1,3,4,5,6});
        Assert.assertTrue(longArray1.contains(3));
        Assert.assertFalse(longArray1.contains(100));
    }

    @Test
    public void hashCodeTest() {
        /* Testing if the hash code of an ordered long array is in fact the same provided as the hashCode() method
         * from Object.
         */
        Object longArrayUnordered=new LongArray(false,16);
        ((LongArray) longArrayUnordered).add(2,1,-6,2);
        ((LongArray) longArrayUnordered).add(5,50,34,42);
        Assert.assertEquals(longArrayUnordered.hashCode(),((LongArray)longArrayUnordered).hashCode());

        LongArray longArrayOrdered=new LongArray();
        longArrayOrdered.add(10,20,40,50);
        longArrayOrdered.add(100,200,400,500);

        /* Expected hash calculation:
         * 1. Start with h = 1
         * 2. For each value in the array, compute (item ^ (item >>> 32))
         *    and add it to h as: h = h * 31 + (int)(item ^ (item >>> 32))
         *    Detailed steps:
         *    - Iteration 1 (item = 10): h = 1 * 31 + 10 = 41
         *    - Iteration 2 (item = 20): h = 41 * 31 + 20 = 1 291
         *    - Iteration 3 (item = 40): h = 1 291 * 31 + 40 = 40 061
         *    - Iteration 4 (item = 50): h = 40 061 * 31 + 50 = 1 241 941
         *    - Iteration 5 (item = 100): h = 1 241 941 * 31 + 100 = 38 500 271
         *    - Iteration 6 (item = 200): h = 38 500 271 * 31 + 200 = 1 193 508 601
         *    - The next operation will overflow because h is an int and can't be calculated if >= 2 147 483 647
         *      (because negative numbers are defined by the first bit
         *    - The result of 1 193 508 601 * 31 + 400 is 36 998 767 031 (1000 1001 1101 0100 1100 0110 0001 1011 0111) which is,
         *    truncated to 32 bits, -1 655 938 633 (1001 1101 0100 1100 0110 0001 1011 0111)
         *    - Iteration 7 (item = 400): h = - 1 655 938 633
         *    - Same here, without redetailing the operation, we get, truncated to 32 bits, 205 510 429
         *    - Iteration 8 (item = 500): h = 205 510 429
         *
         * Assert the calculated hash matches the expected value
         */

        int expectedHash = 205510429;
        Assert.assertEquals(expectedHash, longArrayOrdered.hashCode());
    }

    @Test
    public void indexOfTest() {
        LongArray longArray1=new LongArray(new long[]{1,3,4,5,6,6,3,9});
        Assert.assertEquals(-1,longArray1.indexOf(100));
        Assert.assertEquals(1,longArray1.indexOf(3));
        Assert.assertEquals(6,longArray1.lastIndexOf(3));
        Assert.assertEquals(-1,longArray1.lastIndexOf(100));
    }

    /**
     * Test of all remove methods (removeValue, removeIndex, removeRange, removeAll)
     */
    @Test
    public void removeTest() {
        // removeValue test
        LongArray longArray1=new LongArray(new long[]{1,3,4,5,6,6,3,9});
        Assert.assertTrue(longArray1.removeValue(3));
        Assert.assertArrayEquals(new long[]{1,4,5,6,6,3,9},longArray1.toArray());
        Assert.assertEquals(7,longArray1.size);
        Assert.assertFalse(longArray1.removeValue(99));
        // removeIndex test
        Assert.assertEquals(4,longArray1.removeIndex(1));
        Assert.assertArrayEquals(new long[]{1,5,6,6,3,9},longArray1.toArray());
        Assert.assertEquals(6,longArray1.size);
        try {
            longArray1.removeIndex(56);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to remove at an index that is out of bounds
        }

        // removeRange test
        LongArray longArray2=new LongArray();
        longArray2.addAll(new long[]{1,10,25,2,23,345});
        longArray2.removeRange(2,5);
        Assert.assertArrayEquals(new long[]{1,10},longArray2.toArray());
        try {
            longArray2.removeRange(3,4);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as we are trying to remove at a range that is out of bounds
        }
        try {
            longArray2.removeRange(1,0);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            // This should throw an exception as the starting index is > than ending index
        }

        // removeAll test
        LongArray longArray3=new LongArray();
        longArray3.addAll(new long[]{1,10,25,35,50,40});
        LongArray toBeRemoved=new LongArray(new long[]{1,25,35});
        Assert.assertTrue(longArray3.removeAll(toBeRemoved));
        Assert.assertArrayEquals(new long[]{10,50,40},longArray3.toArray());
        Assert.assertFalse(longArray3.removeAll(toBeRemoved));
        toBeRemoved=new LongArray(new long[]{10,30,22});
        Assert.assertTrue(longArray3.removeAll(toBeRemoved));
        Assert.assertArrayEquals(new long[]{50,40},longArray3.toArray());
    }

    @Test
    public void popPeekFirstTest() {
        LongArray longArray=new LongArray(new long[]{1,2,3,4,5,6,7,8,9,10});
        LongArray emptyLongArray=new LongArray();
        Assert.assertEquals(1,longArray.first());
        Assert.assertEquals(10,longArray.peek());
        Assert.assertEquals(10,longArray.pop());
        Assert.assertArrayEquals(new long[]{1,2,3,4,5,6,7,8,9},longArray.toArray());
        try {
            long first=emptyLongArray.first();
            Assert.fail();
        } catch (IllegalStateException e) {
            // This should throw an exception as we can't take the first item of an empty array
        }
        try {
            long last=emptyLongArray.pop();
            Assert.fail();
        } catch (IllegalStateException e) {
            // This should throw an exception as we can't take the last item of an empty array
        }
        try {
            long last=emptyLongArray.peek();
            Assert.fail();
        } catch (IllegalStateException e) {
            // This should throw an exception as we can't take the last item of an empty array
        }
    }

    @Test
    public void emptyTest() {
        Assert.assertTrue((new LongArray()).isEmpty());
        Assert.assertFalse((new LongArray(new long[]{1})).isEmpty());
        Assert.assertFalse((new LongArray()).notEmpty());
        Assert.assertTrue((new LongArray(new long[]{1})).notEmpty());
    }

    @Test
    public void clearTest() {
        LongArray longArray=new LongArray(new long[]{1});
        longArray.clear();
        Assert.assertTrue(longArray.isEmpty());
    }

    @Test
    public void shrinkTest() {
        LongArray longArray=new LongArray(); // This LongArray will have an "items" attribute of length 16 by default
        longArray.add(1,2,3);
        Assert.assertArrayEquals(new long[]{1,2,3},longArray.shrink());
        Assert.assertEquals(3,longArray.items.length);
    }

    @Test
    public void ensureCapacityTest() {
        LongArray longArray1=new LongArray(new long[]{1,2,4,6,32,53,564,53,2,1,89,0,10,389,8,392,4,27346,2,234,12});
        LongArray longArray2=new LongArray(new long[]{1,2,3});
        Assert.assertArrayEquals(new long[]{1,2,3,0,0,0,0,0},longArray2.ensureCapacity(2));
        Assert.assertArrayEquals(new long[]{1,2,4,6,32,53,564,53,2,1,89,0,10,389,8,392,4,27346,2,234,12,
                        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                longArray1.ensureCapacity(18));
        try {
            longArray1.ensureCapacity(-6);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // This should throw an exception as it is not allowed to have a negative integer as ensureCapacity argument
        }
    }

    @Test
    public void setSizeTest() {
        LongArray longArray1=new LongArray(new long[]{1,2,4,6,32,53,564,53,2,1,89,90,10,389,8,392,4,27346,2,234,12});
        longArray1.setSize(23);
        Assert.assertEquals(23,longArray1.size);
        longArray1.setSize(10);
        Assert.assertArrayEquals(new long[]{1,2,4,6,32,53,564,53,2,1},longArray1.toArray());
        LongArray longArray2=new LongArray(new long[]{1,2,3});
        Assert.assertArrayEquals(new long[]{1,2,3,0,0,0,0,0},longArray2.setSize(5));
        try {
            longArray1.setSize(-3);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // This should throw an exception as it is not allowed to have a negative integer as setSize argument
        }
    }

    @Test
    public void resizeTest() {
        LongArray longArray1=new LongArray(new long[]{1,2,4,6,32,53,564,53,2,1,89,90,10,389,8,392,4,27346,2,234,12});
        Assert.assertArrayEquals(new long[]{1,2,4,6,32,53,564,53,2,1,89,90,10,389,8,392,4,27346,2,234,12,0,0},
                longArray1.resize(23));
    }

    @Test
    public void sortAndReverseTest() {
        LongArray longArray1=new LongArray(new long[]{1,2,4,6,32,53,564,53,2,1,89,90,10,389,8,392,4,27346,2,234,12});
        longArray1.sort();
        Assert.assertArrayEquals(new long[]{1,1,2,2,2,4,4,6,8,10,12,32,53,53,89,90,234,389,392,564,27346},
                longArray1.toArray());
        longArray1.reverse();
        Assert.assertArrayEquals(new long[]{27346,564,392,389,234,90,89,53,53,32,12,10,8,6,4,4,2,2,2,1,1},
                longArray1.toArray());
    }

    @Test
    public void equalsTest() {
        LongArray longArray1=new LongArray();
        LongArray longArray2=new LongArray();
        longArray1.add(1,2);
        longArray2.add(1,2);

        Assert.assertTrue(longArray1.equals(longArray2));

        ArrayMap<Integer,Integer> o=new ArrayMap<Integer,Integer>(); // Random object of a different class
        Assert.assertFalse(longArray1.equals(o));

        LongArray longArray3=new LongArray(false,16);
        longArray3.add(1,2);
        Assert.assertFalse(longArray1.equals(longArray3));

        LongArray longArray4=new LongArray(true,12);
        longArray4.add(1,2);
        Assert.assertTrue(longArray1.equals(longArray4));

        longArray1.add(3);
        Assert.assertFalse(longArray1.equals(longArray2));
    }

    @Test
    public void toStringTest() {
        LongArray emptyLongArray=new LongArray();
        LongArray longArray=new LongArray(new long[]{3,4,5});
        Assert.assertEquals("[]",emptyLongArray.toString());
        Assert.assertEquals("[3, 4, 5]",longArray.toString());
        Assert.assertEquals("",emptyLongArray.toString("; "));
        Assert.assertEquals("3; 4; 5",longArray.toString("; "));
    }
}
