package com.season.myapplication;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        byte[] b = new byte[2];
        b[0] = 13;
        b[1] = 10;
        System.out.println("--");
        System.out.println(new String(b));
        System.out.println("--");
        assertEquals(4, 2 + 2);
    }
}