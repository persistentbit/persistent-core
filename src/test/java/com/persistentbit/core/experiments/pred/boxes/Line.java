package com.persistentbit.core.experiments.pred.boxes;

import com.persistentbit.core.experiments.pred.ViewCursor;
import com.persistentbit.core.experiments.pred.draw.*;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class Line extends AbstractBox{
    private String text;
    private boolean needLayout = true;
    private int[] xpos;
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
	private class LineViewCursor extends BaseValueClass implements ViewCursor {
    	private int textPos;
    	private Line	box;

		public LineViewCursor(int textPos, Line box) {
			this.textPos = textPos;
			this.box = box;
		}

		@Override
		public String toString() {
			return "LineViewCursor(" + text.substring(0,textPos)+"_" + text.substring(textPos) + ")";
		}
	}

	@Override
	public Optional<ViewCursor> createCursor(DrawContext context, int width, DPoint point) {
		FontDef font = context.getCurrentFont();
		int h = context.textHeight(font,text);
    	if(point.y<0 || point.y > h){
    		return Optional.empty();
		}
		if(point.x<=0){
    		return Optional.of(new LineViewCursor(0,this));
		}
		int w = context.textWidth(font,text);
		if(point.x >= w){
			return Optional.of(new LineViewCursor(text.length(),this));
		}
		int prev = 0;
		for(int t=1; t<=text.length();t++){
			w = context.textWidth(font,text.substring(0,t));

			if(prev <= point.x && point.x <= w){
				int dif = (w-prev)/2;
				int pos = t;
				if(w-point.x <= dif){
					pos = pos-1;
				}
				return Optional.of(new LineViewCursor(pos,this));
			}
		}
		return Optional.empty();
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