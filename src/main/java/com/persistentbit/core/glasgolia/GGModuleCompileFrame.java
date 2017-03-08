package com.persistentbit.core.glasgolia;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public class GGModuleCompileFrame implements CompileFrame{

	public static class ModNameDef implements RExpr{

		public final GGModule module;
		public final int id;
		public NameDef nameDef;
		public boolean isDeclared;
		public boolean isInitialized;
		public Object value;

		public ModNameDef(GGModule module, int id, NameDef nameDef, boolean isDeclared) {
			this.module = module;
			this.id = id;
			this.nameDef = nameDef;
			this.isDeclared = isDeclared;
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
			return nameDef.isVal && isInitialized;
		}

		@Override
		public Object get() {
			if(isInitialized == false) {
				throw new EvalException("Can't get '" + nameDef.name + "' in module " + module + ": not initialized.", nameDef.pos);
			}
			return value;
		}

		public Object assign(Object other) {
			if(isInitialized) {
				throw new EvalException("Can't set '" + nameDef.name + "' in module " + module + ": already  initialized.", nameDef.pos);
			}
			value = other;
			return other;
		}
	}

	private final GGModule module;
	private final CompileFrame parentFrame;
	private int nextId;
	private PMap<String, ModNameDef> nameLookup;

	public GGModuleCompileFrame(GGModule module, CompileFrame parentFrame, int nextId,
								PMap<String, ModNameDef> nameLookup
	) {
		this.module = module;
		this.parentFrame = parentFrame;
		this.nextId = nextId;
		this.nameLookup = nameLookup;
	}

	@Override
	public RExpr bind(StrPos pos, String name) {
		throw new ToDo();
	}

	@Override
	public void addName(NameDef nameDef) {
		throw new ToDo();
	}
}
