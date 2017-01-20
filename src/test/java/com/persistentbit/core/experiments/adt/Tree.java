package com.persistentbit.core.experiments.adt;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/01/17
 */
public abstract class Tree<T>{

	private Tree() {}

	public abstract <U> U match(
		Function<Empty<T>, U> empty,
		Function<Node<T>, U> node,
		Function<Leaf<T>, U> leaf
	);


	static public final class Leaf<T> extends Tree<T>{

		public final T value;

		public Leaf(T value) {
			this.value = value;
		}

		@Override
		public <U> U match(
			Function<Empty<T>, U> empty,
			Function<Node<T>, U> node,
			Function<Leaf<T>, U> leaf
		) {
			return leaf.apply(this);
		}
	}

	static public final class Node<T> extends Tree<T>{

		public final Tree left;
		public final Tree right;

		public Node(Tree left, Tree right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public <U> U match(Function<Empty<T>, U> empty, Function<Node<T>, U> node, Function<Leaf<T>, U> leaf
		) {
			return node.apply(this);
		}
	}

	static public final class Empty<T> extends Tree<T>{

		@Override
		public <U> U match(Function<Empty<T>, U> empty, Function<Node<T>, U> node, Function<Leaf<T>, U> leaf
		) {
			return empty.apply(this);
		}
	}

	static <T> Leaf<T> leaf(T value) {
		return new Leaf<>(value);
	}

	static <T> Empty<T> empty() {
		return new Empty<>();
	}

	static <T> Node<T> node(Tree<T> left, Tree<T> right) {
		return new Node<>(left, right);
	}


	public static int sum(Tree<Integer> tree) {
		return tree.match(
			empty -> 0,
			node -> sum(node.left) + sum(node.right),
			leaf -> leaf.value
		);
	}

	public static int depth(Tree<?> tree) {
		return tree.match(
			empty -> 0,
			node -> Math.max(depth(node.left), depth(node.right)) + 1,
			leaf -> 1
		);
	}


	public static void main(String[] args) {
		Tree<Integer> tree = node(
			node(
				leaf(1),
				leaf(2)
			),
			node(
				node(
					node(
						leaf(3),
						leaf(4)
					),
					empty()
				),
				empty()
			)
		);

		System.out.println(depth(tree));
		System.out.println(sum(tree));
	}
}
