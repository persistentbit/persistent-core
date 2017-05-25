package com.persistentbit.core.tests.dependencies;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.tests.CoreTest;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.dependencies.DependencyResolver;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;

/**
 * @author petermuys
 * @since 25/09/16
 */
public class TestDependencies{
	static final TestCase testDependencies = TestCase.name("Test DependencyResolver").code(tr -> {
		Node a = new Node("a");
		Node b = new Node("b");
		Node c = new Node("c");
		Node d = new Node("d");
		Node e = new Node("e");

		a.add(b);
		a.add(d);
		b.add(c);
		b.add(e);
		c.add(d);
		c.add(e);
		PList<String> resolved = DependencyResolver.resolve(a, Node::getEdges).map(plist -> plist.map(Node::getValue)).orElseThrow();
		System.out.println("Resolved: " + resolved);
		assert resolved.equals(PList.val("d", "e", "c", "b", "a"));

		//Check circular dependency...

		d.add(b);
		tr.isFailure(DependencyResolver.resolve(a, Node::getEdges).map(plist -> plist.map(Node::getValue)));

	});

  public void testAll(){
	  CoreTest.runTests(TestDependencies.class);
  }

  public static final class Node extends BaseValueClass{

	private final String value;
	@NoEqual
	private PList<Node> edges = PList.empty();

	public Node(String value,
				PList<Node> edges
	) {
	  this.value = value;
	  this.edges = edges;
	}

	public Node(String value) {
	  this.value = value;
	}

	public Node add(Node dependency) {
	  edges = edges.plus(dependency);
	  return this;
	}

	public PList<Node> getEdges() {
	  return edges;
	}

	public String getValue() {
	  return value;
	}

	@Override
	public String toString() {
	  return "(" + value + ")";
	}
  }

	public static void main(String[] args) {
		ModuleCore.consoleLogPrint.registerAsGlobalHandler();
		new TestDependencies().testAll();
	}
}
