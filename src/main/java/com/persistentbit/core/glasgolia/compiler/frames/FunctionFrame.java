package com.persistentbit.core.glasgolia.compiler.frames;

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
public class FunctionFrame extends AbstractCompileFrame{

	private final CompileFrame parentFrame;
    private final RStack runtimeStack;

	private int nextId;
	private PMap<String, Tuple2<Integer, NameDef>> names = PMap.empty();

    public FunctionFrame(CompileFrame parentFrame, RStack runtimeStack) {
        this.parentFrame = parentFrame;
        this.runtimeStack = runtimeStack;
    }

    @Override
    public RExpr bind(StrPos pos, String name) {
        Tuple2<Integer,NameDef> local = names.getOrDefault(name,null);
		if(local != null) {
			if(local._2.isVal) {
				return new RVal(pos, local._2.type, runtimeStack, local._1);
			}
			else {
				return new RVar(pos, local._2.type, runtimeStack, local._1);
			}
		}
		RExpr imported = getFromLocalImported(name).orElse(null);
		if(imported != null) {
			return imported;
		}
		return parentFrame.bind(pos, name);
	}

    @Override
    public void addName(NameDef nameDef) {
        names = names.put(nameDef.name,Tuple2.of(createStackVarIndex(),nameDef));
    }

    @Override
    public int createStackVarIndex() {
        return nextId++;
    }

	@Override
	public boolean canDefineLocal(String name) {
		return names.containsKey(name) == false;
	}
}
