package com.persistentbit.core.printing.printing;

import com.persistentbit.core.collections.PList;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
public class PrintableList implements PrintableText{
    private final Function<Integer,String> bulletCreator;
    private final PList<PrintableText>    items;

    private PrintableList(Function<Integer,String> bulletCreator,PList<PrintableText> items) {
        this.bulletCreator = bulletCreator;
        this.items = items;
    }
    public PrintableList(){
        this(i -> "* ", PList.empty());
    }


    public PrintableList addItem(PrintableText item){
        return new PrintableList(bulletCreator,items.plus(item));
    }

    @Override
    public void accept(PrintTextWriter printWriter) {
        printWriter.print(PrintableText.inset("\t",true,out -> {
            items.zipWithIndex().forEach(zip -> {
                int index = zip._1;
                PrintableText item = zip._2;
                out.println(PrintableText.inset("  ",false, itemOut -> {
                    itemOut.print(bulletCreator.apply(index));
                    itemOut.println(item);
                    itemOut.flush();
                }));
            });
        }));
    }
}
