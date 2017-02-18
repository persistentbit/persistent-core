package com.persistentbit.core.scripts;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.result.Result;

import javax.script.*;
import java.util.Objects;

/**
 * TODOC
 *
 * @author petermuys
 * @since 7/02/17
 */
public class ScriptEngineEval implements ScriptEval{

	private final ScriptEngine scriptEngine;
	private final PMap<String, Object> context;
	private final Lazy<Bindings> bindings;
	private ScriptEngineEval(ScriptEngine scriptEngine, PMap<String, Object> context) {
		this.scriptEngine = Objects.requireNonNull(scriptEngine);
		this.context = Objects.requireNonNull(context);
		this.bindings = new Lazy<>(() -> {
			Bindings engineScope = new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
			context.forEach(keyValue -> engineScope.put(keyValue._1, keyValue._2));
			return engineScope;
		});
	}

	public static ScriptEngineEval javascript() {
		return new ScriptEngineEval(new ScriptEngineManager().getEngineByExtension("js"), PMap.empty());
	}

	public ScriptEngineEval add(String name, Object value) {
		return new ScriptEngineEval(scriptEngine, context.put(name, value));
	}

	public ScriptEngineEval addAll(PMap<String, Object> contextValues) {
		return new ScriptEngineEval(scriptEngine, context.plusAll(contextValues));
	}


	@Override
	public Result<Object> apply(String script) {
		return Result.function(script).code(l ->
			Result.result(scriptEngine.eval(script, bindings.get()))
		);
	}
}
