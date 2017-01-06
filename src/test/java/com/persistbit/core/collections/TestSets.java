package com.persistbit.core.collections;

import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.POrderedSet;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.testing.TestCase;

import java.util.*;

/**
 * @author Peter Muys
 * @since 13/07/2016
 */
public class TestSets{

	static final TestCase testPSet = TestCase.name("PSet").code(tr -> doAddRemove(PSet.empty()));
	static final TestCase testPOrderedSet = TestCase.name("POrderedSet").code(tr -> doAddRemove(PSet.empty()));


	private static void doAddRemove(IPSet<Integer> empty) {
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
		PSet<Integer>    psetKeys    = pstreamKeys.pset();
		Set<Integer>     pKeys       = psetKeys.toSet();
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

}
