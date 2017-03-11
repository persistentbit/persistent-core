package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.frames.GGModuleCompileFrame;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public class GGModule implements GGObject{

	private final String moduleName;
	private PMap<String, GGModuleCompileFrame.ModNameDef> nameLookup;
	private final Lazy<RExpr> thisRExpr;

	@Override
	public String toString() {
		return "GGModule[" + moduleName + "]";
	}

	public GGModule(String moduleName,
					PMap<String, GGModuleCompileFrame.ModNameDef> nameLookup
	) {
		this.moduleName = moduleName;
		this.nameLookup = nameLookup;
		this.thisRExpr = new Lazy<>(() -> new RExpr(){
			@Override
			public Class getType() {
				return GGModule.class;
			}

			@Override
			public StrPos getPos() {
				return new StrPos(moduleName);
			}

			@Override
			public boolean isConst() {
				return true;
			}

			@Override
			public Object get() {
				return GGModule.this;
			}
		});
	}

	public GGModule setNameLookup(
		PMap<String, GGModuleCompileFrame.ModNameDef> nameLookup
	) {
		this.nameLookup = nameLookup;
		return this;
	}

	public RExpr asRExpr() {
		return thisRExpr.get();
	}

	public Optional<RExpr> bindChild(String name) {
		return nameLookup.getOpt(name).map(v -> v);
	}

	@Override
	public Object getChild(StrPos pos, String name) {
		GGModuleCompileFrame.ModNameDef nameDef = nameLookup.getOrDefault(name, null);
		if(nameDef == null) {
			throw new EvalException("Unknown child '" + name + "' in module '" + moduleName + "'", pos);
		}
		switch(nameDef.nameDef.access) {
			case privateAccess:
				throw new EvalException("'" + name + "' is private in module '" + moduleName + "'!", pos);
			case publicAccess:
				return nameDef.get();
			default:
				throw new ToDo("unknown: " + nameDef.nameDef.access);
		}

	}


	@Override
	public Object binOp(StrPos pos, String op, Object other) {
		throw new ToDo();
	}

	@Override
	public Object castTo(StrPos pos, Class cls) {
		throw new ToDo();
	}


	public String getModuleName() {
		return moduleName;
	}
}
