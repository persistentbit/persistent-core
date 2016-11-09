package com.persistbit.core.collections;


import org.junit.Test;

import java.util.Date;

/**
 * @author Peter Muys
 * @since 25/08/16
 */
public class TestImTools{

  @Test
  public void test1() {
	try {
	  new CaseData1(1, null, new Date(), "username");
	  throw new RuntimeException("Should have an exception for null argument");
	} catch(IllegalStateException e) {}
	CaseData1 d1 = new CaseData1(1, "Peter", null, null);
	CaseData1 d2 = new CaseData1(1, "Peter", null, null);
	CaseData1 d3 = new CaseData1(1, "Peter", null, "mup");

	assert d1.hashCode() == d2.hashCode();
	assert d1.hashCode() != d3.hashCode();
	assert d1.equals(d2);
	assert d1.equals(d3) == false;

	System.out.println(d1);
	System.out.println(d3);
  }
}
