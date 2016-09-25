package com.persistentbit.core.dependencies;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 25/09/16.
 */
public class DependencyNode<VALUE> extends BaseValueClass{
    private final VALUE value;
    private PList<DependencyNode<VALUE>> edges;

    public DependencyNode(VALUE value, PList<DependencyNode<VALUE>> edges) {
        this.value = value;
        this.edges = edges;
    }

    public DependencyNode(VALUE value){
        this(value,PList.empty());
    }

    public VALUE getValue() {
        return value;
    }

    public PList<DependencyNode<VALUE>> getEdges() {
        return edges;
    }

    public DependencyNode<VALUE>    addDependency(DependencyNode<VALUE> dependency){
        edges = edges.plus(dependency);
        return this;
    }

    public PList<VALUE>  resolve(){
        return resolve(PList.empty(),PSet.empty());
    }

    public PList<VALUE> resolve(PList<VALUE> resolved,PSet<VALUE> seen){
        seen = seen.plus(getValue());
        for(DependencyNode edge : getEdges()){
            if(resolved.contains(edge.getValue()) == false){
                if(seen.contains(edge)){
                    throw new RuntimeException("Circular dependency between " + getValue() + " and " + edge.getValue());
                }
                resolved = resolved.plusAll(edge.resolve(resolved,seen));
            }
        }
        resolved = resolved.plus(getValue());
        return resolved;
    }




}
