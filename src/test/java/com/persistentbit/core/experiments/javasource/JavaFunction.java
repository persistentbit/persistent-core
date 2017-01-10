package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaFunction extends BaseValueClass implements PrintableText {

    private final PList<PrintableText> annotations;
    private final PrintableText javaDoc;
    private final String access;
    private final boolean isAbstract;
    private final boolean isStatic;
    private final PrintableText result;
    private final String name;
    private final PrintableText params;
    private final PrintableText content;

    public JavaFunction(PList<PrintableText> annotations, PrintableText javaDoc, String access, boolean isStatic, boolean isAbstract,
                        PrintableText result,
                        String name,
                        PrintableText params,
                        PrintableText content
    ) {
        this.annotations = annotations;
        this.javaDoc = javaDoc;
        this.access = access;
        this.isStatic = isStatic;
        this.isAbstract = isAbstract;
        this.result = result;
        this.name = name;
        this.params = params;
        this.content = content;
    }

    public JavaFunction doc(Object value) {
        PrintableText pt = PrintableText.from(value);
        return copyWith("javaDoc", value);
    }

    public JavaFunction asConstructor() {
        return result(empty);
    }

    public JavaFunction result(Object value) {
        return copyWith("result", PrintableText.from(value));
    }

    public JavaFunction content(Object value) {
        return copyWith("content", PrintableText.from(value));
    }

    public JavaFunction asStatic() {
        return copyWith("isStatic", true);
    }

    public JavaFunction annotation(Object annotation){
        return copyWith("annotations",annotations.plus(PrintableText.from(annotation)));
    }


    public static JavaFunction name(String functionName) {
        return new JavaFunction(
                PList.empty(),
                empty, "public", false, false, PrintableText.fromString("void"), functionName,
                PrintableText.fromString("()"),
                empty
        );
    }

    public JavaFunction asAbstract() {
        return copyWith("isAbstract", true);
    }

    @Override
    public void print(PrintTextWriter out) {
        out.print(javaDoc);
        annotations.forEach(a -> {
            out.println(a);
        });
        out.print(access);
        out.print(isStatic ? " static" : "");
        out.print(isAbstract ? " abstract" : "");
        out.print(" " + result.printToString());
        out.print(" " + name);
        out.print(params);
        if (content != empty) {
            out.print(JavaBlock.of(content));
        } else {
            out.println(";");
        }
    }

}
