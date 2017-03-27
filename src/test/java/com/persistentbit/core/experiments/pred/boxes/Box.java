package com.persistentbit.core.experiments.pred.boxes;

import com.persistentbit.core.experiments.pred.ViewCursor;
import com.persistentbit.core.experiments.pred.draw.DPoint;
import com.persistentbit.core.experiments.pred.draw.DrawContext;
import com.persistentbit.core.experiments.pred.draw.Layout;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public interface Box{
    Layout layout(DrawContext context, int width);

    boolean needLayout();

    Layout draw(DPoint offset, DrawContext context, int width);
	Optional<ViewCursor> createCursor(DrawContext context, int width, DPoint point);
}