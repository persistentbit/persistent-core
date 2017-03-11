package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.glasgolia.compiler.rexpr.GGModule;
import com.persistentbit.core.glasgolia.compiler.rexpr.RConst;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public class GGModuleCompileFrame extends AbstractCompileFrame{

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
			if(isInitialized && nameDef.isVal) {
				throw new EvalException("Can't set '" + nameDef.name + "' in module " + module + ": already  initialized.", nameDef.pos);
			}
			isInitialized = true;
			value = other;
			return other;
		}
	}

	private final GGModule module;
	private PMap<String, ModNameDef> nameLookup;
	public int nextChildId;
	public int nextStackId;

	public GGModuleCompileFrame(GGModule module,
								PMap<String, ModNameDef> nameLookup
	) {
		this.module = module;
		this.nameLookup = nameLookup;
	}

	public GGModuleCompileFrame(GGModule module) {
		this(module, PMap.empty());
	}

	@Override
	public RExpr bind(StrPos pos, String name) {
		RExpr localName = nameLookup.getOrDefault(name, null);
		if(localName != null) {
			return localName;
		}
		if(name.equals("this")) {
			return new RConst(pos, GGModule.class, module);
		}
		RExpr importedExpr = getFromLocalImported(name).orElse(null);
		if(importedExpr != null) {
			return importedExpr;
		}
		throw new CompileException("Can't bind name '" + name + "' in module '" + module.getModuleName() + "'");
	}

	@Override
	public void addName(NameDef nameDef) {
		ModNameDef modDef = nameLookup.getOrDefault(nameDef.name, null);
		if(modDef == null) {
			//A new var... add a new ModNameDef
			modDef = new ModNameDef(module, nextChildId++, nameDef, true);
			nameLookup = nameLookup.put(nameDef.name, modDef);
		}
		else {
			//An existing var... change the definition
			modDef.nameDef = nameDef;
		}
	}

	@Override
	public int createStackVarIndex() {
		return nextStackId++;
	}

	public GGModule createModule(RStack stack, RExpr initCode) {
		ModNameDef undeclared = nameLookup.values().find(modDef -> modDef.isDeclared == false).orElse(null);
		if(undeclared != null) {
			throw new CompileException("Undefined '" + undeclared.nameDef.name + "' in module '" + module
				.getModuleName() + "'");
		}
		stack.addFrame(nextChildId);
		try {
			module.setNameLookup(nameLookup);
			initCode.get();
			return module;
		} catch(Exception e) {
			throw new EvalException("Error initializing module '" + module.getModuleName() + "' ", e);
		} finally {
			stack.popFrame();
		}
	}

	@Override
	public boolean canDefineLocal(String name) {
		return nameLookup.containsKey(name) == false;
	}
}
