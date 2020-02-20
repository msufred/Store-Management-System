package com.gemseeker.sms;

import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public abstract class Controller implements Initializable {

    protected Pane root = null;
    protected Controller parent;
    protected boolean isLoaded = false;

    public void setContentPane(Pane root) {
        this.root = root;
    }

    public Parent getContentPane() {
        return root;
    }

    public void setParent(Controller parent) {
        this.parent = parent;
    }

    public Controller getParent() {
        return parent;
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    public void isLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    /**
     * Task to execute the first time the controller is loaded. Override this
     * method to execute code such as loading resources for the first time.
     */
    public void onLoadTask() {
        isLoaded(true);
    }
    
    /**
     * Tasks to execute the moment the controller's view is loaded or visible on
     * screen. Override this method to execute code such as refreshing resources,
     * database entries etc.
     */
    public void onResume() {
        
    }
}
