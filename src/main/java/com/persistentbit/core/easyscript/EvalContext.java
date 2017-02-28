package com.persistentbit.core.easyscript;

import com.persistentbit.core.OK;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/02/2017
 */
public abstract class EvalContext {
    enum Type {
		block, functionCall, javaContext
	}

	public abstract Optional<Object> getValue(String name);

	public abstract Optional<Object> getClass(String name);


	public abstract EvalContext addImport(String name);

	public abstract Type getType();

	public abstract EvalContext withValue(String name, Object value);

	public abstract boolean hasLocalValue(String name);

	public abstract boolean hasValue(String name);

	public abstract EvalContext subContext(Type type);

	public abstract EvalContext withParentContext(EvalContext context);

	public abstract EvalContext	getLocalContext();

	private static class ContextImpl extends EvalContext{

		private final EvalContext parentContext;
        private final Type type;
		private Map<String, Object> valueLookup;
		private final JavaImports imports;


		public ContextImpl(EvalContext parentContext, Type type, Map<String, Object> valueLookup, JavaImports imports
		) {
			this.parentContext = parentContext;
            this.type = Objects.requireNonNull(type);
			this.valueLookup = valueLookup;
			this.imports = imports;
		}

		@Override
		public Optional<Object> getClass(String name) {
			return imports.getClass(name);
		}

		@Override
		public EvalContext addImport(String name) {
			return new ContextImpl(parentContext,type,valueLookup,imports.add(name));
		}

		@Override
		public Type getType() {
            return type;
        }

        @Override
		public Optional<Object> getValue(String name) {

			if(valueLookup != null) {
				Object value = valueLookup.getOrDefault(name, OK.inst);
				if(value != OK.inst) {
					return Optional.ofNullable(value);
				}
			}

			Optional<Object> res = imports.getClass(name);
			if(res.isPresent()){
				return res;
			}
            return parentContext == null
                    ? Optional.empty()
				: parentContext.getValue(name)
				;
        }

		@Override
		public EvalContext withValue(String name, Object value) {
			//return new ContextImpl(parentContext, type, valueLookup.put(name, value), imports);
			if(valueLookup == null) {
				valueLookup = new HashMap<>();
			}
			valueLookup.put(name, value);
			return this;
		}

		@Override
		public boolean hasLocalValue(String name) {
			return valueLookup != null && valueLookup.containsKey(name);
		}

		@Override
		public boolean hasValue(String name) {
			if(valueLookup != null && valueLookup.containsKey(name)) {
				return true;
			}
			if(imports.getClass(name).isPresent()){
				return true;
			}
			return (parentContext != null && parentContext.hasValue(name));
		}

		@Override
		public EvalContext getLocalContext() {
			return new ContextImpl(this, type, null, imports);
		}

		@Override
		public EvalContext withParentContext(EvalContext context) {
			if(parentContext != null){
				return parentContext.withParentContext(context);
			}
			return new ContextImpl(context, type, valueLookup, imports);
		}

		@Override
		public EvalContext subContext(Type type) {
			return new ContextImpl(this, type, null, imports);
		}
	}


	public static final EvalContext inst = new ContextImpl(null, Type.block, null, new JavaImports());

/*
    public static String script(String template){
        PList<TemplateBlock> blocks = TemplateBlock.parse(template);
        Source source = blocks.map(tb ->
                tb.getType()== TemplateBlock.Type.string
                        ? Source.asSource(tb.getPos(), "\"" + StringUtils.escapeToJavaString(tb.getContent()) + "\"")
                        : Source.asSource(tb.getPos(),tb.getContent())
        ).fold(Source.asSource("\"\""),left->right->left.plus(Source.asSource("+")).plus(right));
        return source.rest();
    }


    public static void main(String... args) throws Exception {
        String s = "\r\n<<start>>blabla<<var2>>\r\n<<var3>>end de rest<>!";
        String script = script(s);

        System.out.println(script);
    } */
}
