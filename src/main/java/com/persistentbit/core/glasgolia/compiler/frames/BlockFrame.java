package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVal;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVar;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

import java.util.Objects;
import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/03/2017
 */
public class BlockFrame extends AbstractCompileFrame{

	private final RStack  runtimeStack;
    private final CompileFrame    parentFrame;
    private PMap<String,Tuple2<Integer,NameDef>> stackVars = PMap.empty();

    public BlockFrame(RStack runtimeStack, CompileFrame parentFrame) {
		this.runtimeStack = Objects.requireNonNull(runtimeStack);
		this.parentFrame = Objects.requireNonNull(parentFrame);
	}

    @Override
    public RExpr bind(StrPos pos, String name) {
        Tuple2<Integer,NameDef> existing = stackVars.getOrDefault(name,null);
        if(existing == null){
            return parentFrame.bind(pos,name);
        }
        //We have a blockvar...
        if(existing._2.isVal){
            return new RVal(pos,existing._2.type,runtimeStack,existing._1);
        } else {
            return new RVar(pos,existing._2.type,runtimeStack,existing._1);
        }
    }

    @Override
    public void addName(NameDef nameDef) {
        stackVars = stackVars.put(nameDef.name,Tuple2.of(createStackVarIndex(),nameDef));
    }

    @Override
    public int createStackVarIndex() {
        return parentFrame.createStackVarIndex();
    }

	@Override
	public boolean canDefineLocal(String name) {
		return stackVars.containsKey(name) == false;
	}

    @Override
    public Optional<Class> getClassForTypeName(String name) {
        Optional<Class> res = getLocalClassForTypeName(name);
        if(res.isPresent()){
            return res;
        }
        return parentFrame.getClassForTypeName(name);
    }
}
