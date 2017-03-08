package com.persistentbit.core.glasgolia;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public class GGModule implements GGObject{

	private final String moduleName;
	private final PMap<String, GGModuleCompileFrame.ModNameDef> nameLookup;


	public GGModule(String moduleName,
					PMap<String, GGModuleCompileFrame.ModNameDef> nameLookup
	) {
		this.moduleName = moduleName;
		this.nameLookup = nameLookup;

	}

	@Override
	public Object getChild(StrPos pos, String name) {
		GGModuleCompileFrame.ModNameDef nameDef = nameLookup.getOrDefault(name, null);
		if(nameDef == null) {
			throw new EvalException("Unknown child '" + name + "' in module '" + moduleName + "'", pos);
		}
		return nameDef.get();
	}


	@Override
	public Object binOp(StrPos pos, String op, Object other) {
		throw new ToDo();
	}

	@Override
	public Object castTo(StrPos pos, Class cls) {
		throw new ToDo();
	}


}
