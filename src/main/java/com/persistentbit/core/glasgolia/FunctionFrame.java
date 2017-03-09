package com.persistentbit.core.glasgolia;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVal;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVar;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/03/2017
 */
public class FunctionFrame implements CompileFrame{
    private final CompileFrame parentFrame;
    private final RStack runtimeStack;

    private int nextId = 0;
    private PMap<String, Tuple2<Integer, NameDef>> names = PMap.empty();

    public FunctionFrame(CompileFrame parentFrame, RStack runtimeStack) {
        this.parentFrame = parentFrame;
        this.runtimeStack = runtimeStack;
    }

    @Override
    public RExpr bind(StrPos pos, String name) {
        Tuple2<Integer,NameDef> local = names.getOrDefault(name,null);
        if(local == null){
            return parentFrame.bind(pos,name);
        }
        if(local._2.isVal){
            return new RVal(pos,local._2.type,runtimeStack,local._1);
        } else {
            return new RVar(pos,local._2.type,runtimeStack,local._1);
        }
    }

    @Override
    public void addName(NameDef nameDef) {
        names = names.put(nameDef.name,Tuple2.of(createStackVarIndex(),nameDef));
    }

    @Override
    public int createStackVarIndex() {
        return nextId++;
    }
}
