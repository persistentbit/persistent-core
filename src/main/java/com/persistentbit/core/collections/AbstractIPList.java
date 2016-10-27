package com.persistentbit.core.collections;

import java.util.Iterator;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 10:48
 */
public abstract class AbstractIPList<T, IMPL extends PStream<T>> extends AbstractPStreamDirect<T, IMPL>
  implements IPList<T>{

  @Override
  public int hashCode() {
	int hashCode = 1;
	for(T v : this) {
	  hashCode = 31 * hashCode + (v == null ? 0 : v.hashCode());
	}
	return hashCode;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
	if(o == this) {
	  return true;
	}
	if(o instanceof IPList == false) {
	  return false;
	}
	IPList p = (IPList) o;

	Iterator<T> i1 = iterator();
	Iterator<T> i2 = p.iterator();

	while(i1.hasNext() && i2.hasNext()) {
	  T      o1 = i1.next();
	  Object o2 = i2.next();
	  if(!(o1 == null ? o2 == null : o1.equals(o2)))
		return false;
	}
	return !(i1.hasNext() || i2.hasNext());
  }


}
