package com.persistentbit.core.dependencies;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.PLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to resolve dependencies between nodes.
 * @see #resolve(Object)
 */
public class DependencyResolver<VALUE>{
    static private final PLog   log  =PLog.get(DependencyResolver.class);
    private final Function<VALUE,PList<VALUE>> getEdges;

    private DependencyResolver(Function<VALUE,PList<VALUE>> getEdges) {
        this.getEdges = getEdges;
    }


    private PList<VALUE>  resolve(VALUE value){
        List<VALUE> res = new ArrayList<VALUE>();
        HashSet<VALUE> seen = new HashSet<VALUE>();
        resolve(value,res,seen);
        return PList.from(res);
    }

    private void resolve(VALUE node, List<VALUE> resolved, Set<VALUE> seen){
        log.trace("Resolving "  + node);
        seen.add(node);

        log.trace("Seen = " + seen);
        for(VALUE edge : getEdges.apply(node)){
            if(resolved.contains(edge) == false){
                if(seen.contains(edge)){
                    throw new CircularDependencyException(node,edge);
                }
                resolve(edge,resolved,seen);
            }
        }
        resolved.add(node);
    }

    /**
     * Resolve dependencies between nodes.
     * @param node  The node to resolve
     * @param getDependencies Function to get the dependencies for a node
     * @param <T> The Node type
     * @return An ordered list with the dependencies.
     * @throws CircularDependencyException Thrown when there is a circular dependency between 2 nodes.
     */
    static public <T>  PList<T> resolve(T node, Function<T,PList<T>> getDependencies) throws CircularDependencyException{
        DependencyResolver<T> dr = new DependencyResolver<>(getDependencies);
        return dr.resolve(node);
    }



}
