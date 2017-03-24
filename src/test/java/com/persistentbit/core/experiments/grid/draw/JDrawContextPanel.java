package com.persistentbit.core.experiments.grid.draw;

import com.persistentbit.core.experiments.grid.View;
import com.persistentbit.core.experiments.grid.ViewManager;

import javax.swing.*;
import java.awt.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/03/2017
 */
public class JDrawContextPanel<DOC> extends JPanel implements View<DOC>{

    private final ViewManager<DOC> viewManager;

    public JDrawContextPanel(ViewManager<DOC> viewManager) {
        this.viewManager = viewManager;
    }

    @Override
    public void paintAll(Graphics g) {
        throw new RuntimeException("JDrawContextPanel.paintAll TODO: Not yet implemented");
    }

    @Override
    public ViewManager<DOC> getManager() {
        return viewManager
    }
}
