package com.persistentbit.core.experiments.grid;


import com.persistentbit.core.experiments.grid.draw.DPoint;
import com.persistentbit.core.experiments.grid.draw.TextDrawContext;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 20/03/2017
 */
public class Rijbewijs {

    public static void main(String... args) throws Exception {
        TextDrawContext dc = new TextDrawContext();
        LineList page= new LineList(
            new Line("Line 1"),
            new UnorderedList(
                    new Line("item1"),
                    new UnorderedList(
                            new Line("sub item 1"),
                            new Line("sub item 2")
                    ),
                    new Line("item2")
            )
        );
        page.draw(new DPoint(),dc,80);
        dc.tg.print(System.out);
    }
}
