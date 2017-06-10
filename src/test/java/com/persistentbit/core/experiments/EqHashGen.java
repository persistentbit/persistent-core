package com.persistentbit.core.experiments;

import java.util.Arrays;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/06/17
 */
public class EqHashGen{
	short s1;
	Short sS1;
	byte b1;
	Byte bB1;
	boolean bool;
	char charValue;
	int intValue;
	long longValue;
	char[][] charArray;
	Integer[] intArray;
	float f1;
	double d1;
	Float ff1;
	Double dd1;

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		EqHashGen eqHashGen = (EqHashGen) o;

		if(s1 != eqHashGen.s1) return false;
		if(b1 != eqHashGen.b1) return false;
		if(bool != eqHashGen.bool) return false;
		if(charValue != eqHashGen.charValue) return false;
		if(intValue != eqHashGen.intValue) return false;
		if(longValue != eqHashGen.longValue) return false;
		if(Float.compare(eqHashGen.f1, f1) != 0) return false;
		if(Double.compare(eqHashGen.d1, d1) != 0) return false;
		if(sS1 != null ? !sS1.equals(eqHashGen.sS1) : eqHashGen.sS1 != null) return false;
		if(bB1 != null ? !bB1.equals(eqHashGen.bB1) : eqHashGen.bB1 != null) return false;
		if(!Arrays.deepEquals(charArray, eqHashGen.charArray)) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if(!Arrays.equals(intArray, eqHashGen.intArray)) return false;
		if(ff1 != null ? !ff1.equals(eqHashGen.ff1) : eqHashGen.ff1 != null) return false;
		return dd1 != null ? dd1.equals(eqHashGen.dd1) : eqHashGen.dd1 == null;
	}

	@Override
	public int hashCode() {
		int  result;
		long temp;
		result = (int) s1;
		result = 31 * result + (sS1 != null ? sS1.hashCode() : 0);
		result = 31 * result + (int) b1;
		result = 31 * result + (bB1 != null ? bB1.hashCode() : 0);
		result = 31 * result + (bool ? 1 : 0);
		result = 31 * result + (int) charValue;
		result = 31 * result + intValue;
		result = 31 * result + (int) (longValue ^ (longValue >>> 32));
		result = 31 * result + Arrays.deepHashCode(charArray);
		result = 31 * result + Arrays.hashCode(intArray);
		result = 31 * result + (f1 != +0.0f ? Float.floatToIntBits(f1) : 0);
		temp = Double.doubleToLongBits(d1);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (ff1 != null ? ff1.hashCode() : 0);
		result = 31 * result + (dd1 != null ? dd1.hashCode() : 0);
		return result;
	}
}
