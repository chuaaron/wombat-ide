package actions;

import icons.IconManager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import wombat.DocumentManager;

/**
 * Run the active document.
 */
public class Redo extends AbstractAction {
	private static final long serialVersionUID = 448918915341534569L;

	public Redo() {
		super("Redo", IconManager.icon("Redo.png"));
		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		DocumentManager.Redo();
	}
}