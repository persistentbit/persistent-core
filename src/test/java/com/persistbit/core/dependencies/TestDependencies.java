package com.persistbit.core.dependencies;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.dependencies.DependencyNode;
import org.testng.annotations.Test;

/**
 * Created by petermuys on 25/09/16.
 */
public class TestDependencies {

    @Test
    public void test() {
        DependencyNode<String> a = new DependencyNode<String>("a");
        DependencyNode<String> b = new DependencyNode<String>("b");
        DependencyNode<String> c = new DependencyNode<String>("c");
        DependencyNode<String> d = new DependencyNode<String>("d");
        DependencyNode<String> e = new DependencyNode<String>("e");

        a.addDependency(b);
        a.addDependency(d);
        b.addDependency(c);
        b.addDependency(e);
        c.addDependency(d);
        c.addDependency(e);
        System.out.println("Resolved: " + a.resolve());
        assert a.resolve().equals(PList.val("d","e","c","b","a"));
    }
}
