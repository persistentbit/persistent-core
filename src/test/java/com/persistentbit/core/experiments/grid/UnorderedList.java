package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.experiments.grid.draw.DPoint;
import com.persistentbit.core.experiments.grid.draw.Dim;
import com.persistentbit.core.experiments.grid.draw.DrawContext;
import com.persistentbit.core.experiments.grid.draw.Layout;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class UnorderedList extends AbstractDComponent{
    private final PList<DComponent> rows;
    public UnorderedList(PList<DComponent> rows) {
        this.rows = rows;
    }

    public UnorderedList(DComponent...rows){
        this(PList.val(rows));
    }

    public UnorderedList() {
        this(PList.empty());
    }

    public UnorderedList add(DComponent line){
        return new UnorderedList(rows.plus(line));
    }

    private final String bulletText = "  * ";
    private Layout getBulletLayout(DrawContext context){
        return context.textLayout(context.getCurrentFont(),bulletText);
    }

    @Override
    public Layout layout(DrawContext context,int width) {
        Layout blay = getBulletLayout(context);
        int w = 0;
        int h = 0;
        Integer baseLine = null;
        for(DComponent c : rows){
            Layout l = c.layout(context, width-blay.dim.width);
            l = l.combineHeight(blay);
            if(baseLine==null){

                baseLine = l.baseLine;
            }
            w = Math.max(w, l.dim.width);
            h = h + l.dim.height;
        }
        return new Layout(new Dim(w + blay.dim.width,h),baseLine);
    }

    class UListCursor implements ViewCursor{
    	private ViewCursor childCursor;
    	private int itemNumber;

		public UListCursor(ViewCursor childCursor, int itemNumber) {
			this.childCursor = childCursor;
			this.itemNumber = itemNumber;
		}
	}

	@Override
	public ViewCursor createCursor(int x, int y) {
		return null;
	}

	@Override
    public boolean needLayout() {
        return rows.find(c -> c.needLayout()).isPresent();
    }

    @Override
    public Layout draw(DPoint offset, DrawContext context, int width) {
        Layout blay = getBulletLayout(context);
        int w = 0;
        int h = 0;
        offset = offset.addX(blay.dim.width);
        Integer baseLine = null;
        for(DComponent c : rows){
            Layout l = c.layout(context,width-blay.dim.width);
            l = l.combineHeight(blay);
            context.drawText(offset.addX(-blay.dim.width).addY(l.baseLine),context.getCurrentFont(),context.getFgColor(),bulletText);
            c.draw(offset,context,width-blay.dim.width);
            if(baseLine==null){
                baseLine = l.baseLine;
            }
            w = Math.max(w, l.dim.width);
            h = h + l.dim.height;
            offset = offset.addY(l.dim.height);
        }
        return new Layout(new Dim(w+blay.dim.width,h),baseLine);
    }
}