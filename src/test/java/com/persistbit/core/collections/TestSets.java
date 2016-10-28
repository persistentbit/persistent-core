package com.persistbit.core.collections;

import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.POrderedSet;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Peter Muys
 * @since 13/07/2016
 */
public class TestSets{

  @Test
  public void testPSet() {
	doAddRemove(PSet.empty());
  }

  private void doAddRemove(IPSet<Integer> empty) {
	Set<Integer>   refMap = new LinkedHashSet<>();
	IPSet<Integer> pmap   = empty;
	Random         r      = new Random(System.currentTimeMillis());

	for(int t = 0; t < 100000; t++) {
	  int key = r.nextInt();
	  refMap.add(key);
	  pmap = pmap.plus(key);
	}
	refMap.add(null);
	pmap = pmap.plus(null);
	PStream<Integer> pstreamKeys = pmap;
	//System.out.println(pstreamKeys);
	PSet<Integer> psetKeys = pstreamKeys.pset();
	//System.out.println(psetKeys);
	Set<Integer> pKeys = psetKeys.toSet();
	assert refMap.size() == pmap.size();

	if(pmap instanceof POrderedSet) {
	  //Lets check if the order is ok...
	  Iterator<Integer> refIter = refMap.iterator();
	  Iterator<Integer> pIter   = pmap.iterator();
	  while(refIter.hasNext() && pIter.hasNext()) {
		assert Objects.equals(refIter.next(), pIter.next());

	  }
	  assert refIter.hasNext() == pIter.hasNext();
	}

	if(refMap.equals(pKeys) == false) {
	  throw new RuntimeException();
	}

	for(Integer entry : refMap) {
	  if(pmap.contains(entry) == false) {
		throw new RuntimeException(entry.toString());
	  }
	}


  }

  @Test
  public void testPOrderedSet() {
	doAddRemove(POrderedSet.empty());
  }
}
