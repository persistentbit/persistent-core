package com.persistentbit.core.config;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.ImTools;

import java.util.Objects;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/05/17
 */
public class ConfigGroup extends BaseValueClass{
	private PMap<String,ConfigVar<?>> vars;

	public ConfigGroup(
		PMap<String, ConfigVar<?>> vars
	) {
		this.vars = Objects.requireNonNull(vars);
	}
	public ConfigGroup(){
		this(PMap.empty());
	}

	public ConfigGroup add(ConfigVar var){
		return copyWith(var.getName(),var);
	}
	public ConfigGroup addFields(Object obj){
		if(obj == null){
			return this;
		}
		Class<?> cls = obj.getClass();
		PMap<String,ConfigVar<?>> extra = ImTools.get(cls).getFieldGetters()
			   .filter(g -> ConfigVar.class.isAssignableFrom(g.field.getType()))
			   .map(g -> (ConfigVar<?>)g.getter.get(obj))
			   .groupByOneValue(ConfigVar::getName, var -> var);
		return copyWith("vars",vars.plusAll(extra));
	}

	public ConfigGroup add(ConfigGroup group) {
		return new ConfigGroup(vars.plusAll(group.vars));
	}

	public Result<ConfigVar<?>> getVar(String name){
		return Result.fromOpt(vars.getOpt(name));
	}

	@Override
	public String toString() {
		return "ConfigGroup{" +
				vars.values().map(var -> var.getName() + "->" + var.get()).toString(",") +
				'}';
	}
}
