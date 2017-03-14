package com.persistentbit.core.templates;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.utils.ToDo;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 14/03/2017
 */
public class GGTemplate {
    private final Source source;
    private final PMap<String,Object> values;
    private final PList<TemplateBlock> blocks;

    public GGTemplate(Source source, PMap<String, Object> values,PList<TemplateBlock> blocks) {
        this.source = source;
        this.values = values;
        this.blocks = blocks;
    }

    public String eval(){

        GlasgoliaCompiler compiler = GlasgoliaCompiler.replCompiler();
        values.forEach(t -> compiler.addConstValue(t._1,t._2));
        throw new ToDo();
    }

}
