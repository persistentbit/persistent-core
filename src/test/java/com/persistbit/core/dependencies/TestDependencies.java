package com.persistbit.core.dependencies;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.dependencies.DependencyResolver;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;
import org.junit.Test;

/**
 * @author petermuys
 * @since 25/09/16
 */
public class TestDependencies{

  @Test
  public void test() {
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
	PList<String> resolved = DependencyResolver.resolve(a, Node::getEdges).map(Node::getValue);
	System.out.println("Resolved: " + resolved);
	assert resolved.equals(PList.val("d", "e", "c", "b", "a"));

	//Check circular dependency...

	d.add(b);
	/*try {
	  DependencyResolver.resolve(a, Node::getEdges).map(Node::getValue);
	  assert false;
	} catch(CircularDependencyException ex) {
	  assert ex.getFirstNode().equals(d) || ex.getSecondNode().equals(d);
	  assert ex.getFirstNode().equals(b) || ex.getSecondNode().equals(b);
	}*/

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
}
