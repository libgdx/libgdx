package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by b4dt0bi on 11.06.16.
 */
public class LibGdxJsonListInMapTest {
    private static class TestClass {
        public Map<Integer, List<String>> listMap = new TreeMap<Integer, List<String>>();

        @Override
        public String toString() {
            return "TestClass{" +
                    "listMap=" + listMap +
                    '}';
        }
    }

    @Test
    public void test(){
        TestClass testClass = new TestClass();
        List<String> lst = new ArrayList<String>();
        lst.add("Test");
        testClass.listMap.put(1, lst);
        Json json = new Json();
        String jString = json.toJson(testClass);
        TestClass testClass2 = json.fromJson(TestClass.class, jString);
        Assert.assertEquals(testClass.toString(), testClass2.toString());
    }
}
