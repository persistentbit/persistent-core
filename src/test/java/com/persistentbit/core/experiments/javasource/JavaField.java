package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public class JavaField extends BaseValueClass implements PrintableText{

    private final PrintableText javaDoc;
    private final String        access;
    private final boolean       isStatic;
    private final PrintableText type;
    private final String        name;
    private final PrintableText init;
    private final PList<PrintableText> annotations;

    public JavaField(PList<PrintableText> annotations,PrintableText javaDoc, String access, boolean isStatic, PrintableText type,
                        String name, PrintableText init
    ) {
        this.annotations = annotations;
        this.javaDoc = javaDoc;
        this.access = access;
        this.isStatic = isStatic;
        this.name = name;
        this.type = type;
        this.init = init;
    }

    public JavaField doc(Object value) {
        PrintableText pt = PrintableText.from(value);
        return copyWith("javaDoc", value);
    }


    public JavaField init(Object value) {
        return copyWith("init", PrintableText.from(value));
    }

    public JavaField type(Object value){
        return copyWith("type",PrintableText.from(value));
    }

    public JavaField asStatic() {
        return copyWith("isStatic", true);
    }

    public JavaField annotate(Object annotation) {
        return copyWith("annotations",this.annotations.plus(PrintableText.from(annotation)));
    }


    public static JavaField of(Object type, String fieldName) {
        return of(type,fieldName,empty);
    }
    public static JavaField of(Object type, String fieldName, Object init) {
        return new JavaField(
                PList.empty(),
                PrintableText.empty,
                "private",
                false,
                PrintableText.from(type),
                fieldName,
                PrintableText.from(init)
        );
    }

    public JavaFunction asAbstract() {
        return copyWith("isAbstract",true);
    }

    @Override
    public void print(PrintTextWriter out) {
        out.print(javaDoc);
        annotations.forEach(a -> {
            out.println(a);
        });
        out.print(access);
        out.print(isStatic ? " static" : "");
        out.print(" " + type.printToString());
        out.print(" " + name);
        if(init != empty){
            out.print("\t=\t");
            out.print(init);
        }
        out.println(";");
    }

}
