package com.persistentbit.core.experiments.pred.draw;


import com.persistentbit.core.function.Memoizer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 23/03/2017
 */
public class GraphDrawContext implements DrawContext {
    private Graphics2D graph;
    private FontDef currentFontDef;

    private Function<FontDef, Font> font;
    private Function<FontDef, FontMetrics> fontMetrics;
    private Color fgColor;
    private Color bgColor;

    public GraphDrawContext(Graphics2D graph) {
        init(graph);
    }

    private void init(Graphics2D graph) {
        this.graph = graph;
        this.graph = graph;
        this.font = Memoizer.of(fdef -> {
            /*Font f = new Font(fdef.getName(),
                    (fdef.isBold() ? Font.BOLD : Font.PLAIN)
                            | (fdef.isItalic() ? Font.ITALIC : 0)
                    ,
                    fdef.getSize()
            );*/

			try {
				Font f = new Font(fdef.getName(),
					(fdef.isBold() ? Font.BOLD : Font.PLAIN)
						| (fdef.isItalic() ? Font.ITALIC : 0)
					,
					fdef.getSize()
				);
				Map<TextAttribute, Object> attributes = new HashMap<>();
				attributes.put(TextAttribute.SIZE, fdef.getSize());
				attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
				return f.deriveFont(attributes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}



        });
        this.fontMetrics = Memoizer.of( fdef ->
                graph.getFontMetrics(font.apply(fdef))
        );
		this.currentFontDef = new FontDef("PragmataPro",18,false,false,false);
    }

    @Override
    public FontDef getCurrentFont() {
        return currentFontDef;
    }

    @Override
    public Color getFgColor() {
        return fgColor;
    }

    @Override
    public Color getBgColor() {
        return bgColor;
    }

    @Override
    public int textWidth(FontDef font, String text) {
        return fontMetrics.apply(font).stringWidth(text);
    }

    @Override
    public int textHeight(FontDef font, String text) {
        return fontMetrics.apply(font).getHeight();
    }

    private Font getFont(FontDef fontDef) {
        return font.apply(fontDef);
    }

    @Override
    public int baseLine(FontDef font) {
        return fontMetrics.apply(font).getAscent();
    }

    @Override
    public Layout drawText(DPoint pos, FontDef font, Color color, String text) {
        graph.setFont(this.font.apply(font));
        graph.setColor(color);
        //graph.drawString(text,pos.x,pos.y);

		FontRenderContext frc    = graph.getFontRenderContext();
		TextLayout        layout = new TextLayout(text, this.font.apply(font), frc);
		layout.draw(graph, (float)pos.x,(float)pos.y);


        return new Layout(new Dim(textWidth(font,text),textHeight(font,text)),baseLine(font));
    }

    @Override
    public void reset() {
        throw new RuntimeException("GraphDrawContext.reset TODO: Not yet implemented");
    }
}
