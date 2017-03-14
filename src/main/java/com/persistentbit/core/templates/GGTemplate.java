package com.persistentbit.core.templates;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.parser.source.Source;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 14/03/2017
 */
public class GGTemplate {
    private final Source source;
    private final PMap<String,Object> values;
	private final Lazy<PList<TemplateBlock>> blocks;

	private GGTemplate(Source source, PMap<String, Object> values) {
		this.source = source;
        this.values = values;
		this.blocks = new Lazy<>(() -> TemplateBlock.parse(source.position, source.rest()));
	}

	public static GGTemplate template(String template) {
		return new GGTemplate(Source.asSource(template), PMap.empty());
	}

	public static GGTemplate source(Source source) {
		return new GGTemplate(source, PMap.empty());
	}

	public GGTemplate param(String name, Object value) {
		return new GGTemplate(source, values.put(name, value));
	}

	public GGTemplate param(String n1, Object v1, String n2, Object v2) {
		return param(n1, v1).param(n2, v2);
	}

	public GGTemplate param(String n1, Object v1, String n2, Object v2, String n3, Object v3) {
		return param(n1, v1, n2, v2).param(n3, v3);
	}

	public GGTemplate param(String n1, Object v1, String n2, Object v2, String n3, Object v3, String n4, Object v4) {
		return param(n1, v1, n2, v2, n3, v3).param(n4, v4);
	}

    public String eval(){

        GlasgoliaCompiler compiler = GlasgoliaCompiler.replCompiler();
        values.forEach(t -> compiler.addConstValue(t._1,t._2));
		String codeBlocks = blocks.get().map(b -> {
			switch(b.getType()) {
				case string:
					return "\"\"\"" + b.getContent() + "\"\"\"";
				case code:
					return b.getContent();
				default:
					throw new RuntimeException("Unknown: " + b.getType());
			}
		}).toString(" + ");
		Object result = compiler.compileCode(codeBlocks).map(re -> re.get()).orElseThrow();
		return result.toString();
	}

}
