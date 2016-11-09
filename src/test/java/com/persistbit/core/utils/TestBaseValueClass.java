package com.persistbit.core.utils;


import org.junit.Test;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class TestBaseValueClass{

  @Test
  public void test() {
	TestValue t1 = new TestValue(1234, "userX");
	TestValue t2 = new TestValue(1234, "userX");
	TestValue t3 = new TestValue(5678, "userY");
	System.out.println(t1 + ",   " + t2 + ",  " + t3);
	assert t1.hashCode() == t2.hashCode();
	assert t1.hashCode() != t3.hashCode();
	assert t1.equals(t2);
	assert t1.equals(t3) == false;
	TestValue t4 = new TestValue(5678, "userY", 1);
	assert t3.equals(t4);
  }
}
