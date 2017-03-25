package com.persistentbit.core.experiments.grid.draw;

import com.persistentbit.core.experiments.grid.*;
import com.persistentbit.core.utils.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/03/2017
 */
public class JDrawContextPanel<DOC> extends JPanel implements View<DOC>{

    private final ViewManager<DOC> viewManager;
    private DComponent drawComponent;
    private Function<DOC, DComponent> componentBuilder;

    public JDrawContextPanel(ViewManager<DOC> viewManager) {
        this.viewManager = viewManager;
        this.setLayout(null);
        this.setPreferredSize(new Dimension(640,480));

        viewManager.addView(this);
        componentBuilder = doc ->
        	build((OutlineDoc)doc)
		;
		addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				JDrawContextPanel.this.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});
    }

	public void mouseClicked(MouseEvent e){
    	if(drawComponent != null){
    		drawComponent.createCursor(e.getX(),e.getY());
		}
	}

	@Override
	public void paint(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0,0,g.getClipBounds().width,g.getClipBounds().height);
		g.setColor(Color.black);
		GraphDrawContext dc = new GraphDrawContext(g2);
		if(drawComponent!= null){
			drawComponent.layout(dc,getBounds().width);
			drawComponent.draw(DPoint.of(0,0),dc,getBounds().width);
		}
		//dc.drawText(DPoint.of(10,dc.baseLine(dc.getCurrentFont())),dc.getCurrentFont(),dc.getFgColor(),"Hello");

    }

    private DComponent build(OutlineDoc doc){
    	if(doc instanceof OutlineList){
    		return buildList((OutlineList)doc);
		} else if(doc instanceof OutlineText){
    		return buildText((OutlineText) doc);
		}
		throw new ToDo();
	}

	private DComponent buildList(OutlineList doc){
    	return new UnorderedList(doc.getElements().map(d -> build(d)));
	}
	private DComponent buildText(OutlineText text){
		return new Line(text.getText());
	}

    @Override
    public ViewManager<DOC> getManager() {
        return viewManager;
    }

	@Override
	public void docUpdated(DOC doc) {
		drawComponent = componentBuilder.apply(doc);
		repaint(1);
	}
}
