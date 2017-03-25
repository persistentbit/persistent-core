package com.persistentbit.core.experiments.grid.draw;

import com.persistentbit.core.experiments.grid.OutlineDoc;
import com.persistentbit.core.experiments.grid.OutlineList;
import com.persistentbit.core.experiments.grid.OutlineText;

import javax.swing.*;
import java.awt.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 24/03/17
 */
public class JTestFrame extends JFrame{
	private final JViewManager<OutlineDoc> viewManager;
	private final JDrawContextPanel<OutlineDoc> view;
	public JTestFrame(OutlineDoc doc, String title) throws HeadlessException {
		super(title);
		viewManager = new JViewManager<>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		view = new JDrawContextPanel<>(viewManager);
		getContentPane().add(view);
		viewManager.setDoc(doc);
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		OutlineList doc = new OutlineList(
			new OutlineText("Dit is een test regel"),
			new OutlineText("Dit is de 2e regel"),
			new OutlineList(
				new OutlineText("Sub item 1"),
				new OutlineText("Sub item 2"),
				new OutlineText("Sub item 3")
			)
		);
		JTestFrame frame = new JTestFrame(doc, "TestFrame");

	}
}
