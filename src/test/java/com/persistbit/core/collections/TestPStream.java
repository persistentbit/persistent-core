package com.persistbit.core.collections;

import com.persistentbit.core.collections.*;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.tuples.Tuple2;
import org.junit.Test;

import java.util.Collections;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 09:35
 */
@SuppressWarnings({"SizeReplaceableByIsEmpty", "AssertWithSideEffects", "OptionalGetWithoutIsPresent"})
public class TestPStream{

	private static final PStream<Integer> init = PStream.val(4, 10, 2, 8, 100, -2, 0, 1000);

	private static PList<PStream<Integer>> createStreamVersions(PStream<Integer> original) {
		PList<PStream<Integer>> nonLazy = PList.val(
			PList.<Integer>empty().plusAll(original),
			LList.<Integer>empty().plusAll(original),
			PStream.from(original)
		);
		return nonLazy.plusAll(nonLazy.map(s -> s.lazy()));
	}


	private static final TestCase headMiddleEnd = TestCase.name("headMiddleEnd").code(t -> {
		createStreamVersions(PList.val(0, 1, 2, 4)).forEach(l -> {

			PStream<Tuple2<PStream.HeadMiddleEnd, Integer>> hme = l.headMiddleEnd();
			t.assertEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.end
			));
			t.assertEquals(hme.map(i -> i._2), l);
			l = PList.val(0, 1, 2);
			hme = l.headMiddleEnd();
			t.assertEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.end
			));
			t.assertEquals(hme.map(i -> i._2), l);
			l = l.clear().plusAll(0, 1);
			hme = l.headMiddleEnd();
			t.assertEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.end
			));
			t.assertEquals(hme.map(i -> i._2), l);
			l = l.clear().plusAll(0);
			hme = l.headMiddleEnd();
			t.assertEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.headAndEnd));
			t.assertEquals(hme.map(i -> i._2), l);
		});
	});

	private static final TestCase duplicates = TestCase.name("duplicates").code(tr -> {
		createStreamVersions(PList.val(0, 1, 2, 3)).forEach(l -> tr.assertTrue(l.duplicates().isEmpty()));
		createStreamVersions(PList.val(0, 1, 2, 3, 2)).forEach(l -> tr.assertEquals(l.duplicates(), PList.val(2)));
		createStreamVersions(PList.empty()).forEach(l -> tr.assertEquals(l.duplicates(), PList.empty()));
		createStreamVersions(PList.val(0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4))
			.forEach(l -> tr.assertEquals(l.duplicates(), PList.val(1, 2, 3)));
	});

	private static final TestCase sequences = TestCase.name("sequences").code(tr -> {
		PStream<Integer> s = PStream.sequence(10, (n) -> n - 1);
		tr.assertTrue(s.isInfinite());
		tr.assertTrue(s.isEmpty() == false);
		tr.assertEquals(s.limit(10).plist(), PList.val(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
		expectInfinite(s::size);
		expectInfinite(s::plist);
		expectInfinite(s::pset);
		expectInfinite(s::toArray);
		expectInfinite(s::list);
		expectInfinite(s::dropLast);
		expectInfinite(() -> s.fold(0, (a, b) -> a));
		expectInfinite(() -> s.groupBy(i -> i));
		expectInfinite(() -> s.join((a, b) -> a));
		expectInfinite(s::llist);
		expectInfinite(s::max);
		expectInfinite(s::min);
		expectInfinite(() -> s.plus(1));
		expectInfinite(() -> s.plusAll(Collections.emptyList()));
		expectInfinite(s::reversed);
		expectInfinite(s::sorted);
		expectInfinite(s::toArray);
		expectInfinite(() -> s.toArray(null));
		expectInfinite(s::toList);
		expectInfinite(() -> s.toString(","));
		expectInfinite(() -> s.with(1, (a, b) -> 1));
		expectInfinite(() -> s.count((i) -> true));
	});

	private static final TestCase pstream = TestCase.name("PStream functions").code(tr -> {
		createStreamVersions(init).forEach(s -> testStream(tr, s));
		PSet<Integer> pi = PSet.forInt().plusAll(init);
		testStream(tr, pi);
		testStream(tr, pi.lazy());
	});


	/**
	 * Expect a stream with following elements: 4,10,2,8,100,-2,0,1000
	 *
	 * @param s The stream
	 */
	private static void testStream(TestRunner tr, PStream<Integer> s) {
		tr.assertTrue(s.size() == 8);
		tr.assertTrue(s.isEmpty() == false);
		tr.assertTrue(s.clear().size() == 0);
		tr.assertTrue(s.clear().isEmpty() == true);
		tr.assertTrue(s.contains(1000));
		tr.assertTrue(s.contains(4));
		tr.assertTrue(s.contains(8));
		tr.assertTrue(s.contains(2000) == false);
		tr.assertTrue(s.containsAll(PStream.val(2, 4, 0)));
		tr.assertTrue(s.containsAll(PStream.val(2, 4, 0, 9)) == false);
		tr.assertTrue(s.min().get() == -2);
		tr.assertTrue(s.max().get() == 1000);
		tr.assertTrue(s.clear().min().isPresent() == false);
		tr.assertTrue(s.clear().max().isPresent() == false);
		tr.assertEquals(s.limit(0).plist(), s.clear().plist());
		expectException(() -> s.limit(-1), Exception.class);
		tr.assertTrue(s.limit(100).size() == 8);
		tr.assertTrue(s.limit(6).size() == 6);
		tr.assertEquals(s.plusAll(PStream.val(3, 9)).pset(), PStream.val(4, 10, 2, 8, 100, -2, 0, 1000, 3, 9).pset());
		tr.assertEquals(s.plus(3).pset(), PStream.val(4, 10, 2, 8, 100, -2, 0, 1000, 3).pset());
		tr.assertTrue(s.plus(4).distinct().count(i -> i == 4) == 1);
		tr.assertTrue(s.plus(5).plus(5).plus(5).distinct().count(i -> i == 5) == 1);
		tr.assertTrue(s.count(i -> i <= 4) == 4);
		tr.assertTrue(s.find(i -> i == 3).isPresent() == false);
		tr.assertTrue(s.find(i -> i == 4).get() == 4);
		tr.assertTrue(s.fold(10, (a, b) -> a + b) == 1132);
		tr.assertTrue(s.clear().fold(11, (a, b) -> a + b) == 11);
		tr.assertTrue(s.clear().plus(1).fold(11, (a, b) -> a + b) == 12);
		tr.assertTrue(s.clear().plus(1).plus(8).fold(11, (a, b) -> a + b) == 20);
		tr.assertTrue(s.sorted().head() == -2);
		tr.assertTrue(s.clear().headOpt().isPresent() == false);
		tr.assertEquals(s.sorted().tail(), PStream.val(0, 2, 4, 8, 10, 100, 1000));
		expectException(() -> s.clear().tail(), Exception.class);
		expectException(() -> s.clear().head(), Exception.class);

/*
		s.list();
        s.dropLast();
        s.filter();
        s.find();
        s.fold();
        s.groupBy();
        s.head();
        s.headOpt();
        s.isEmpty();
        s.isInfinite();
        s.join();
        s.limit();
        s.llist();
        s.map();
        s.max();
        s.min();
        s.plist();
        s.plus();
        s.plusAll();
        s.pset();
        s.reversed();
        s.sorted();
        s.stream();
        s.toArray();
        s.toString();
        s.with();
        s.zipWithIndex();
        s.zip();
*/


	}


	private static final TestCase all = TestCase.name("PStream All").subTestCases(
		headMiddleEnd,
		duplicates,
		sequences,
		pstream
	);

	@Test
	public void all() {
		TestRunner.runTest(all).orElseThrow();
	}


	private static void expectInfinite(Runnable r) {
		expectException(r, InfinitePStreamException.class);
	}

	private static void expectException(Runnable r, Class<? extends Exception> exceptionCls) {
		try {
			r.run();
		} catch(Exception e) {
			if(exceptionCls.isAssignableFrom(e.getClass())) {
				return;
			}
			throw e;
		}
		throw new RuntimeException("Expected" + exceptionCls.getSimpleName() + " exception");
	}




}
