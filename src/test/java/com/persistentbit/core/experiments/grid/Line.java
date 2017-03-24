package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.experiments.grid.draw.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class Line extends AbstractComponent{
    private String text;
    private boolean needLayout = true;
    public Line(String text) {
        this.text = text;
    }

    @Override
    public Layout layout(DrawContext context, int width) {
        FontDef font = context.getCurrentFont();
        int w = context.textWidth(font,text);
        int h = context.textHeight(font,text);
        needLayout = false;
        return new Layout(new Dim(w,h),context.baseLine(font));
    }

    @Override
    public boolean needLayout() {
        return needLayout;
    }

    @Override
    public Layout draw(DPoint offset, DrawContext context, int width) {

        return context.drawText(offset,context.getCurrentFont(),context.getFgColor(),text);
    }
}