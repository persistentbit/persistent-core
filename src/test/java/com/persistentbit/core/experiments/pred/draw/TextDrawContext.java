package com.persistentbit.core.experiments.pred.draw;


import java.awt.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class TextDrawContext implements DrawContext{
    private FontDef currentFont;
    private Color fgColor;
    private Color bgColor;
    public final TextGrid tg = new TextGrid();

    public TextDrawContext(){
        reset();
    }
    @Override
    public void reset() {
        currentFont = new FontDef("sansserif",10,false,false,false);
        fgColor = Color.BLACK;
        bgColor = Color.WHITE;
        tg.reset();
    }

    @Override
    public FontDef getCurrentFont() {
        return currentFont;
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
        return text.length();
    }

    @Override
    public int textHeight(FontDef font, String text) {
        return 1;
    }

    @Override
    public int baseLine(FontDef font) {
        return 0;
    }



    @Override
    public Layout drawText(DPoint pos, FontDef font, Color color, String text) {
        tg.draw(pos.x,pos.y,text);
        return new Layout(new Dim(text.length(),1),0);
    }
}
