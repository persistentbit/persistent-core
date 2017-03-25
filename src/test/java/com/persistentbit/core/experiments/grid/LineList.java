package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.experiments.grid.draw.DPoint;
import com.persistentbit.core.experiments.grid.draw.Dim;
import com.persistentbit.core.experiments.grid.draw.DrawContext;
import com.persistentbit.core.experiments.grid.draw.Layout;
import com.persistentbit.core.utils.ToDo;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class LineList extends AbstractDComponent{
    private final PList<DComponent> rows;

    public LineList(PList<DComponent> rows) {
        this.rows = rows;
    }

    public LineList(DComponent...rows){
        this(PList.val(rows));
    }
    public LineList() {
        this(PList.empty());
    }

    public LineList add(DComponent line){
        return new LineList(rows.plus(line));
    }

	@Override
	public ViewCursor createCursor(int x, int y) {
		throw new ToDo();
	}

	@Override
    public Layout layout(DrawContext context, int width) {
        int w = 0;
        int h = 0;
        Integer baseLine = null;
        for(DComponent c : rows){
            Layout l = c.layout(context, width);
            if(baseLine==null){
                baseLine = l.baseLine;
            }
            w = Math.max(w, l.dim.width);
            h = h + l.dim.height;
        }
        return new Layout(new Dim(w,h),baseLine);
    }

    @Override
    public boolean needLayout() {
        return rows.find(c -> c.needLayout()).isPresent();
    }

    @Override
    public Layout draw(DPoint offset, DrawContext context, int width) {
        Dim  res = new Dim();
        Integer baseLine = null;
        for(DComponent c : rows){
            Layout l = c.draw(offset,context,width);
            if(baseLine == null){
                baseLine = l.baseLine;
            }
            res = res.addHeight(l.dim.height);
            offset = offset.addY(l.dim.height);
        }
        return new Layout(res,baseLine);
    }
}