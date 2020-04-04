package com.gemseeker.sms.core;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Abstract class for windows that uses FXML file. Subclasses that are called by
 * the application for the first time loads the FXML file then calls the
 * {@link AbstractFxmlWindowController#onFxmlLoaded()} for additional initializations.
 * 
 * EDITOR'S NOTE:
 * I think it is more logical to load the FXML for the controller only when it is
 * about to be used.
 * 
 * @author gemini1991
 */
public abstract class AbstractFxmlWindowController extends AbstractWindowController {

    private final URL fxmlURL;
    
    public AbstractFxmlWindowController(URL fxmlURL) {
        this(fxmlURL, null);
    }
    
    public AbstractFxmlWindowController(URL fxmlURL, Stage owner) {
        super(owner);
        this.fxmlURL = fxmlURL;
    }
    
    public AbstractFxmlWindowController(URL fxmlURL, Stage owner, boolean sizeToScene) {
        super(owner, sizeToScene);
        this.fxmlURL = fxmlURL;
    }
    
    public URL getFxmlURL() {
        return fxmlURL;
    }

    /*
        Implemented
    */
    @Override
    protected void makeRoot() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setLocation(fxmlURL);
        try {
            Parent root = loader.load();
            assert root != null;
            setRoot(root);
            controllerDidLoadFxml();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Failed to load " + fxmlURL.getFile());
        }
    }
  
    /*
     * Asserts and calls onFxmlLoaded() for additional setup/initialization. 
     */
    protected void controllerDidLoadFxml() {
        assert getRoot() != null;
        assert getRoot().getScene() != null;
        onFxmlLoaded();
    }
    
    /**
     * Invoked after the FXML file is loaded by this window controller.
     * Use this method to initialize (other) related components and/or
     * initialize component listeners.
     */
    protected abstract void onFxmlLoaded();
}
