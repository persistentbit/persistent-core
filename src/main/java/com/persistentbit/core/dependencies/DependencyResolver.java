package com.persistentbit.core.dependencies;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.doc.Component;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to resolve dependencies between nodes.
 *
 * @see #resolve(Object)
 */
@Component
public final class DependencyResolver<VALUE>{


	private final Function<VALUE, PList<VALUE>> getEdges;

	private DependencyResolver(Function<VALUE, PList<VALUE>> getEdges) {
		this.getEdges = getEdges;
	}

	/**
	 * Resolve dependencies between nodes.
	 *
	 * @param node            The node to resolve
	 * @param getDependencies Function to get the dependencies for a node
	 * @param <T>             The Node type
	 *
	 * @return An ordered list with the dependencies.
	 */
	public static <T> Result<PList<T>> resolve(T node, Function<T, PList<T>> getDependencies) {
		return Log.function(node).code(l -> {
			DependencyResolver<T> dr = new DependencyResolver<>(getDependencies);
			return dr.resolve(node);
		});

	}

	private Result<PList<VALUE>> resolve(VALUE value) {
		List<VALUE> res = new ArrayList<>();
		try {
			resolve(value, res, new HashSet<>());
		} catch(Exception e) {
			return Result.failure(e);
		}
		return Result.success(PList.from(res));
	}

	private void resolve(VALUE node, List<VALUE> resolved, Set<VALUE> seen) {
		Log.function(node).code(l -> {
			seen.add(node);
			l.info("Seen", seen);
			for(VALUE edge : getEdges.apply(node)) {
				if(resolved.contains(edge) == false) {
					if(seen.contains(edge)) {
						throw new CircularDependencyException(node, edge);
					}
					resolve(edge, resolved, seen);
				}
			}
			resolved.add(node);
			return Nothing.inst;
		});

	}


}
