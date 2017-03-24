package com.persistentbit.core.tests.collections;

import com.persistentbit.core.tests.CoreTest;
import com.persistentbit.core.collections.*;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.tuples.Tuple2;

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

	static final TestCase limit = TestCase.name("Test limit").code(tr -> {
		tr.isEquals(init.limitOnPreviousValue(t -> t == 2), PStream.val(4, 10, 2));
		tr.isEquals(init.limitOnPreviousValue(t -> t == 1001), init);
		tr.isEquals(init.limitOnPreviousValue(t -> t == 1000), init);
		tr.isEquals(init.limitOnPreviousValue(t -> t == 4), PStream.val(4));
		tr.isNotEquals(init.limitOnPreviousValue(t -> t == 4), PStream.val(5));
	});

	static final TestCase testPList = TestCase.name("PList").code(tr -> {
		PList<Integer> l = new PList<>();
		for(int t = 0; t < 100000; t++) {
			l = l.plus(t);
		}
		PList<Integer> l2 = l;
		for(int t = 0; t < l.size(); t++) {
			if(l.get(t) != t) {
				throw new RuntimeException();
			}
			l2 = l2.put(t, -t);
		}
		for(int t = 0; t < l2.size(); t++) {
			if(l2.get(t) != -t) {
				throw new RuntimeException("t=" + t + ", value=" + l2.get(t));
			}
		}
		PList<Integer> p = new PList<>();
		p = p.plusAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		tr.isEquals(p, PList.val(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		tr.isEquals(p.dropLast(), PList.val(1, 2, 3, 4, 5, 6, 7, 8, 9));
		tr.isEquals(p.subList(0, 1), PList.val(1));
		tr.isEquals(p.map(i -> "(" + i + ")").toString(", "), "(1), (2), (3), (4), (5), (6), (7), (8), (9), (10)");
		tr.isEquals(p.filter(i -> i % 2 == 0).map(i -> "(" + i + ")").toString(", "), "(2), (4), (6), (8), (10)");

		p = new PList<Integer>().plusAll(4, 1, 2, 7, 0, 3, 10, -5);
		PStream<Tuple2<Integer, Integer>> p2 = p.sorted().zipWithIndex().plist().reversed();
		tr.isEquals(p2.toString(", "), "(7,10), (6,7), (5,4), (4,3), (3,2), (2,1), (1,0), (0,-5)");
		tr.isEquals(p2.plist().toString(", "), "(7,10), (6,7), (5,4), (4,3), (3,2), (2,1), (1,0), (0,-5)");

	});


	static final TestCase headMiddleEnd = TestCase.name("headMiddleEnd").code(t -> {
		createStreamVersions(PList.val(0, 1, 2, 4)).forEach(l -> {

			PStream<Tuple2<PStream.HeadMiddleEnd, Integer>> hme = l.headMiddleEnd();
			t.isEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.end
			));
			t.isEquals(hme.map(i -> i._2), l);
			l = PList.val(0, 1, 2);
			hme = l.headMiddleEnd();
			t.isEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.middle,
				PStream.HeadMiddleEnd.end
			));
			t.isEquals(hme.map(i -> i._2), l);
			l = l.clear().plusAll(0, 1);
			hme = l.headMiddleEnd();
			t.isEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.head,
				PStream.HeadMiddleEnd.end
			));
			t.isEquals(hme.map(i -> i._2), l);
			l = l.clear().plusAll(0);
			hme = l.headMiddleEnd();
			t.isEquals(hme.map(i -> i._1), PList.val(
				PStream.HeadMiddleEnd.headAndEnd));
			t.isEquals(hme.map(i -> i._2), l);
		});
	});

	static final TestCase duplicates = TestCase.name("duplicates").code(tr -> {
		createStreamVersions(PList.val(0, 1, 2, 3)).forEach(l -> tr.isTrue(l.duplicates().isEmpty()));
		createStreamVersions(PList.val(0, 1, 2, 3, 2)).forEach(l -> tr.isEquals(l.duplicates(), PList.val(2)));
		createStreamVersions(PList.empty()).forEach(l -> tr.isEquals(l.duplicates(), PList.empty()));
		createStreamVersions(PList.val(0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4))
			.forEach(l -> tr.isEquals(l.duplicates(), PList.val(1, 2, 3)));
	});

	static final TestCase sequences = TestCase.name("sequences").code(tr -> {
		PStream<Integer> s = PStream.sequence(10, (n) -> n - 1);
		tr.isTrue(s.isInfinite());
		tr.isTrue(s.isEmpty() == false);
		tr.isEquals(s.limit(10).plist(), PList.val(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
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

	static final TestCase pstream = TestCase.name("PStream functions").code(tr -> {
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
		tr.isTrue(s.size() == 8);
		tr.isTrue(s.isEmpty() == false);
		tr.isTrue(s.clear().size() == 0);
		tr.isTrue(s.clear().isEmpty() == true);
		tr.isTrue(s.contains(1000));
		tr.isTrue(s.contains(4));
		tr.isTrue(s.contains(8));
		tr.isTrue(s.contains(2000) == false);
		tr.isTrue(s.containsAll(PStream.val(2, 4, 0)));
		tr.isTrue(s.containsAll(PStream.val(2, 4, 0, 9)) == false);
		tr.isTrue(s.min().get() == -2);
		tr.isTrue(s.max().get() == 1000);
		tr.isTrue(s.clear().min().isPresent() == false);
		tr.isTrue(s.clear().max().isPresent() == false);
		tr.isEquals(s.limit(0).plist(), s.clear().plist());
		expectException(() -> s.limit(-1), Exception.class);
		tr.isTrue(s.limit(100).size() == 8);
		tr.isTrue(s.limit(6).size() == 6);
		tr.isEquals(s.plusAll(PStream.val(3, 9)).pset(), PStream.val(4, 10, 2, 8, 100, -2, 0, 1000, 3, 9).pset());
		tr.isEquals(s.plus(3).pset(), PStream.val(4, 10, 2, 8, 100, -2, 0, 1000, 3).pset());
		tr.isTrue(s.plus(4).distinct().count(i -> i == 4) == 1);
		tr.isTrue(s.plus(5).plus(5).plus(5).distinct().count(i -> i == 5) == 1);
		tr.isTrue(s.count(i -> i <= 4) == 4);
		tr.isTrue(s.find(i -> i == 3).isPresent() == false);
		tr.isTrue(s.find(i -> i == 4).get() == 4);
		tr.isTrue(s.fold(10, (a, b) -> a + b) == 1132);
		tr.isTrue(s.clear().fold(11, (a, b) -> a + b) == 11);
		tr.isTrue(s.clear().plus(1).fold(11, (a, b) -> a + b) == 12);
		tr.isTrue(s.clear().plus(1).plus(8).fold(11, (a, b) -> a + b) == 20);
		tr.isTrue(s.sorted().head() == -2);
		tr.isTrue(s.clear().headOpt().isPresent() == false);
		tr.isEquals(s.sorted().tail(), PStream.val(0, 2, 4, 8, 10, 100, 1000));
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



	public void testAll() {
		CoreTest.runTests(TestPStream.class);
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


	public static void main(String[] args) {
		CoreTest.runTests(TestPStream.class);
	}


}
