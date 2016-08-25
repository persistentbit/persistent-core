package com.persistbit.core.collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * User: petermuys
 * Date: 25/08/16
 * Time: 18:24
 */
public class TestImTools {

    @Test
    public void test1(){
        try {
            new CaseData1(1, null, new Date(), "username");
            throw new RuntimeException("Should have an exception for null argument");
        }catch (IllegalStateException e){}
        CaseData1  d1 = new CaseData1(1,"Peter",null,null);
        CaseData1  d2 = new CaseData1(1,"Peter",null,null);
        CaseData1  d3 = new CaseData1(1,"Peter",null,"mup");

        assert d1.hashCode() == d2.hashCode();
        assert d1.hashCode() != d3.hashCode();
        assert d1.equals(d2);
        assert d1.equals(d3) == false;

        System.out.println(d1.toString());
        System.out.println(d3.toString());
    }
}
