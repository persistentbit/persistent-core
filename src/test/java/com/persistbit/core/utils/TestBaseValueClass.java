package com.persistbit.core.utils;

import org.testng.annotations.Test;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class TestBaseValueClass {
    @Test
    void test() {
        TestValue t1 = new TestValue(1234,"userx");
        TestValue t2 = new TestValue(1234,"userx");
        TestValue t3 = new TestValue(5678,"usery");
        System.out.println(t1 + ",   " + t2 + ",  " + t3);
        assert t1.hashCode()==t2.hashCode();
        assert t1.hashCode()!=t3.hashCode();
        assert t1.equals(t2);
        assert t1.equals(t3) == false;
        TestValue t4 = new TestValue(5678,"usery",1);
        assert t3.equals(t4);
    }
}
