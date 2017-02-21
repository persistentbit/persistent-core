package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PMap;

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
        block,functionCall
    }
    public abstract Optional<EExpr> getExpr(String name);
    public abstract Type getType();



    private class ContextImpl extends EvalContext{
        private final EvalContext parentContext;
        private final Type type;
        private final PMap<String,EExpr> exprLookup;


        public ContextImpl(EvalContext parentContext, Type type, PMap<String, EExpr> exprLookup) {
            this.parentContext = parentContext;
            this.type = Objects.requireNonNull(type);
            this.exprLookup = Objects.requireNonNull(exprLookup);
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Optional<EExpr> getExpr(String name) {
            Optional<EExpr> res = exprLookup.getOpt(name);
            if(res.isPresent()){
                return res;
            }
            return parentContext == null
                    ? Optional.empty()
                    : parentContext.getExpr(name)
            ;
        }
    }

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
