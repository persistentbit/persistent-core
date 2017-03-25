package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.experiments.grid.draw.*;
import com.persistentbit.core.utils.ToDo;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class Line extends AbstractDComponent{
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
	public ViewCursor createCursor(int x, int y) {
		throw new ToDo();
	}

	@Override
    public boolean needLayout() {
        return needLayout;
    }

    @Override
    public Layout draw(DPoint offset, DrawContext context, int width) {

        return context.drawText(offset.addY(context.baseLine(context.getCurrentFont())),context.getCurrentFont(),context.getFgColor(),text);
    }
}