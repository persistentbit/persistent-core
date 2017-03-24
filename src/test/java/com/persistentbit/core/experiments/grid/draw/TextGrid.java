package com.persistentbit.core.experiments.grid.draw;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.UString;

import java.io.PrintStream;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class TextGrid {
    private PList<String> lines = PList.empty();

    public void draw(int x, int y, String text){
        while(lines.size() <= y){
            lines = lines.plus("");
        }
        String line = lines.get(y);
        line = UString.padRight(line,x+text.length(),' ');
        line = line.substring(0,x) + text + line.substring(x+text.length());
        lines = lines.put(y,line);
    }

    public void reset() {
        lines = PList.empty();
    }

    public void print(PrintStream out){
        lines.forEach(l -> out.println(l));
    }


}
