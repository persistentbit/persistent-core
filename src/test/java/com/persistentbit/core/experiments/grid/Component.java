package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.experiments.grid.draw.DPoint;
import com.persistentbit.core.experiments.grid.draw.DrawContext;
import com.persistentbit.core.experiments.grid.draw.Layout;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public interface Component {
    Layout layout(DrawContext context, int width);

    boolean needLayout();

    Layout draw(DPoint offset, DrawContext context, int width);
}