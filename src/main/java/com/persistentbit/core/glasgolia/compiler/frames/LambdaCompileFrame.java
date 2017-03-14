package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.glasgolia.compiler.rexpr.*;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/03/17
 */
public class LambdaCompileFrame extends AbstractCompileFrame{

	private final CompileFrame parentFrame;
	private final RStack runtimeStack;

	private int nextId;
	private PMap<String, Tuple2<Integer, NameDef>> names = PMap.empty();
	private PSet<String> undeclared = PSet.empty();

	public LambdaCompileFrame(CompileFrame parentFrame, RStack runtimeStack) {
		this.parentFrame = parentFrame;
		this.runtimeStack = runtimeStack;
	}

	@Override
	public RExpr bind(StrPos pos, String name) {
		Tuple2<Integer, NameDef> local = names.getOrDefault(name, null);
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
		addName(new NameDef(pos, name, false, GGAccess.publicAccess, Object.class));
		undeclared = undeclared.plus(name);
		return bind(pos, name);
	}

	@Override
	public void addName(NameDef nameDef) {
		names = names.put(nameDef.name, Tuple2.of(createStackVarIndex(), nameDef));
	}

	@Override
	public int createStackVarIndex() {
		return nextId++;
	}

	@Override
	public boolean canDefineLocal(String name) {
		return names.containsKey(name) == false;
	}

	public RExpr createLambdaCreate(StrPos pos, int paramSize, String[] paramNames, Class[] paramTypes, RExpr code) {
		//undeclared.forEach(u -> ctx.addVal(false,u.name,u.type));
		//code = compile(g.code);
		int                           frameSize    = nextId;
		PList<Tuple2<Integer, RExpr>> initFreeList = PList.empty();
		for(String undeclaredName : undeclared) {
			//boolean isParam = g.params.find(v -> v.name.equals(vv.name)).isPresent();
			//if(isParam == false) {
			//initFreeList = initFreeList.plus(Tuple2.of(vv.id, ctx.bindName(g.getPos(), runtimeStack, vv.name)));
			//}
			Tuple2<Integer, NameDef> idAndNameDef = names.get(undeclaredName);
			initFreeList =
				initFreeList.plus(Tuple2.of(idAndNameDef._1, parentFrame.bind(code.getPos(), undeclaredName)));
		}
		return new RLambdaCreate(pos, paramSize, paramNames, paramTypes, initFreeList, frameSize, code, runtimeStack);
	}
}
