package com.persistentbit.core.experiments.pred.draw;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class FontDef extends BaseValueClass{
    private final String name;
    private final int   size;
    private final boolean bold;
    private final boolean underline;
    private final boolean italic;

    public FontDef(String name, int size, boolean bold, boolean underline, boolean italic) {
        this.name = name;
        this.size = size;
        this.bold = bold;
        this.underline = underline;
        this.italic = italic;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isItalic() {
        return italic;
    }
}