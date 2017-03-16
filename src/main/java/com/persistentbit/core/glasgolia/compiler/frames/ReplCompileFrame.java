package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.collections.LList;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.glasgolia.compiler.rexpr.GGReplThis;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.StrPos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public class ReplCompileFrame extends AbstractCompileFrame{


	private class ReplStack implements RStack{

		private LList<Map<Integer, Object>> frames = LList.<Map<Integer, Object>>empty().plus(new HashMap<>());


		public int getNextId() {
			return frames.head().size();
		}

		@Override
		public RStack reset() {
			return new ReplStack();
		}

		@Override
		public void addFrame(int size) {
			frames = frames.prepend(new HashMap<>());
		}

		@Override
		public void popFrame() {
			frames = frames.tail();
		}

		@Override
		public Object get(int index) {
			return frames.head().get(index);
		}

		@Override
		public String toString() {
			Map<Integer, Object> thisFrame = frames.head();
			String str = thisFrame.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
								  .map(e -> "" + e.getKey() + "=" + e.getValue()).collect(Collectors
					.joining(", "));
			return "RFrame(" + str + ")";
		}

		@Override
		public void set(int index, Object value) {
			frames.head().put(index, value);
		}


		@Override
		public void print(PrintTextWriter out) {
			out.println("ReplStack { ");
			out.indent(print(frames));
			out.println("}");

		}

		private PrintableText print(LList<Map<Integer, Object>> frames) {
			return out -> {
				if(frames.isEmpty()) {
					return;
				}
				Map<Integer, Object> thisFrame = frames.head();
				List<String> items =
					thisFrame.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
							 .map(e -> "" + e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
				items.forEach(item -> out.println(item));
				out.indent(print(frames.tail()));
			};
		}

	}


	public class ReplVar implements RExpr{

		public NameDef nameDef;
		private Object value;
		private boolean isAssigned;

		public ReplVar(NameDef nameDef) {
			this.nameDef = nameDef;
		}

		@Override
		public Class getType() {
			return nameDef.type;
		}

		@Override
		public StrPos getPos() {
			return nameDef.pos;
		}

		@Override
		public boolean isConst() {
			return nameDef.isVal && isAssigned;
		}

		@Override
		public Object get() {
			return value;
		}

		@Override
		public Object assign(Object other) {
			isAssigned = true;
			value = other;
			return other;
		}
	}
	private GGReplThis	replThis;
	private ReplStack replStack = new ReplStack();
	private PMap<String, ReplVar> nameLookup = PMap.empty();

	public ReplCompileFrame() {
		replThis = new GGReplThis(this);
	}

	@Override
	public boolean canDefineLocal(String name) {
		return nameLookup.containsKey(name) == false;
	}

	@Override
	public RExpr bind(StrPos pos, String name) {
		RExpr res = nameLookup.getOrDefault(name, null);
		if(res != null) {
			return res;
		}

		res = getFromLocalImported(name)
			.orElse(null);
		if(res != null) {
			return res;
		}
		if(name.equals("this")) {
			return replThis.asRExpr();
		}
		throw new CompileException("Can't find '" + name + "'", pos);
	}

	public Object assignChild(String name, Object value){
		ReplVar var = nameLookup.getOrDefault(name,null);
		if(var == null){
			throw new EvalException("Can't assign a value to non-existing child '" + name + "'.");
		}
		if(var.isConst() && var.isAssigned){
			throw new EvalException("Can't assign a value to val '" + name + "'.");
		}
		var.assign(value);
		return value;
	}

	@Override
	public void addName(NameDef nameDef) {
		ReplVar var = nameLookup.getOrDefault(nameDef.name, null);
		if(var != null) {
			var.nameDef = nameDef;
		}
		else {
			var = new ReplVar(nameDef);
			nameLookup = nameLookup.put(nameDef.name, var);
		}
	}

	public PList<ReplVar> getDefs() {
		return nameLookup.values().plist();
	}

	@Override
	public int createStackVarIndex() {
		return replStack.getNextId();
	}


	public RStack getStack() {
		return replStack;
	}

	@Override
	public Optional<Class> getClassForTypeName(String name) {
		return getLocalClassForTypeName(name);
	}
}