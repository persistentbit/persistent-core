package com.persistentbit.core.experiments.pred.draw;


import java.awt.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public interface DrawContext {
    FontDef getCurrentFont();
    Color getFgColor();
    Color   getBgColor();
    int textWidth(FontDef font,String text);
    int textHeight(FontDef font, String text);
    int baseLine(FontDef font);
    default Layout  textLayout(FontDef font, String text){
        return new Layout(new Dim(textWidth(font,text),textHeight(font,text)),baseLine(font));
    }

    Layout drawText(DPoint pos, FontDef font,Color color, String text);

    void reset();
}
