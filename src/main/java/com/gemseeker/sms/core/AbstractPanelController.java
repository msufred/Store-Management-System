package com.gemseeker.sms.core;

import javafx.scene.Parent;

/**
 *
 * @author gemini1991
 */
public abstract class AbstractPanelController {

    private Parent panelRoot;
    private final AbstractWindowController windowController; // optional
    
    public AbstractPanelController() {
        this(null);
    }
    
    public AbstractPanelController(AbstractWindowController windowController) {
        this.windowController = windowController;
    }
    
    public AbstractWindowController getWindowController() {
        return windowController;
    }
    
    public Parent getPanel() {
        if (panelRoot == null) {
            makePanel();
        }
        return panelRoot;
    }
    
    public void setPanel(Parent panelRoot) {
        assert panelRoot != null;
        this.panelRoot = panelRoot;
    }
    
    /**
     * Creates the FX components composing the panel content.
     * This method MUST invoke {@link AbstractPanelController#setPanel}.
     */
    protected abstract void makePanel();
    
    /**
     * Convenient method for disposing disposables etc.
     */
    public void dispose() {
        
    }
    
}
