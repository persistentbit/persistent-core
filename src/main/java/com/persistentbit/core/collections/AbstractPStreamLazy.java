package com.persistentbit.core.collections;

import java.util.Iterator;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 20:30
 */
abstract class AbstractPStreamLazy<T> implements PStreamWithDefaults<T>{


  @Override
  public boolean equals(Object o) {
	if(o == this) {
	  return true;
	}
	if(o instanceof PStream == false) {
	  return false;
	}
	  PStream  other = (PStream) o;
	Iterator   i1    = iterator();
	  Iterator i2    = other.iterator();
	while(i1.hasNext() && i2.hasNext()) {
	  Object v1 = i1.next();
	  Object v2 = i2.next();
	  if(v1 == null) {
		if(v2 != null) {
		  return false;
		}
	  }
	  else {
		if(v1.equals(v2) == false) {
		  return false;
		}
	  }
	}
	return i1.hasNext() == i2.hasNext();
  }

  @Override
  public String toString() {
	return limit(100).toString("<", ", ", ">");
  }
}
