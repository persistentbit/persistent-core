package com.persistentbit.core.experiments.pred.boxes;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.experiments.pred.ViewCursor;
import com.persistentbit.core.experiments.pred.draw.DPoint;
import com.persistentbit.core.experiments.pred.draw.Dim;
import com.persistentbit.core.experiments.pred.draw.DrawContext;
import com.persistentbit.core.experiments.pred.draw.Layout;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class UnorderedList extends AbstractBox{
    private final PList<Box> rows;
    public UnorderedList(PList<Box> rows) {
        this.rows = rows;
    }

    public UnorderedList(Box...rows){
        this(PList.val(rows));
    }

    public UnorderedList() {
        this(PList.empty());
    }

    public UnorderedList add(Box line){
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
        for(Box c : rows){
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

    class UListCursor extends BaseValueClass implements ViewCursor{
    	private ViewCursor childCursor;
    	private int itemNumber;

		public UListCursor(ViewCursor childCursor, int itemNumber) {
			this.childCursor = childCursor;
			this.itemNumber = itemNumber;
		}

		@Override
		public String toString() {
			return "UListCursor(line=" + itemNumber + ", " + childCursor + ")";
		}
	}

	@Override
	public Optional<ViewCursor> createCursor(DrawContext context, int width, DPoint point
	) {
		Layout blay = getBulletLayout(context);
		int w = 0;
		int h = 0;
		point = point.addX(-blay.dim.width);
		Integer baseLine = null;
		int index=0;
		for(Box c : rows){
			Layout l = c.layout(context,width-blay.dim.width);
			l = l.combineHeight(blay);
			if(baseLine==null){
				baseLine = l.baseLine;
			}
			w = Math.max(w, l.dim.width);
			if(point.y>= 0 && point.y <= l.dim.height){
				int finIndex = index;
				return c.createCursor(context,width-blay.dim.width,point)
					.map(childCursor -> new UListCursor(childCursor,finIndex));

			}

			point = point.addY(-l.dim.height);
			index += 1;
		}
		return Optional.empty();
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
        for(Box c : rows){
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