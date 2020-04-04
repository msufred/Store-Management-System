package com.gemseeker.sms.core;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Abstract controller for windowed components.
 * This abstract class already implements methods for showing and
 * closing the window. These are {@link AbstractWindowController#openWindow()}
 * and {@link AbstractWindowController#closeWindow()} respectively.
 * 
 * @author gemini1991
 */
public abstract class AbstractWindowController {

    private final Stage owner;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private boolean sizeToScene;
    
    private final EventHandler<WindowEvent> closeRequestHandler = evt -> {
        onCloseRequest(evt);
    };
    
    public AbstractWindowController() {
        this(null, true);
    }
    
    public AbstractWindowController(Stage owner) {
        this(owner, true);
    }
    
    public AbstractWindowController(Stage owner, boolean sizeToScene) {
        this.owner = owner;
        this.sizeToScene = sizeToScene;
    }
    
    public Parent getRoot() {
        if (root == null) {
            makeRoot();
            assert root != null;
        }
        return root;
    }
    
    public void setRoot(Parent root) {
        this.root = root;
    }
    
    /**
     * Returns the Scene of this window controller.
     * This method is called in {@link AbstractWindowController#getStage() }.
     * 
     * @return Scene object of this window controller.
     */
    public Scene getScene() {
        assert Platform.isFxApplicationThread();
        if (scene == null) {
            scene = new Scene(getRoot());
        }
        return scene;
    }
    
    /**
     * Returns the Stage object of this window controller.
     * This method calls the {@link AbstractWindowController#getScene() }.
     * 
     * @return Stage object of this window controller.
     */
    public Stage getStage() {
        assert Platform.isFxApplicationThread();
        if (stage == null) {
            stage = new Stage();
            stage.initOwner(owner);
            stage.setScene(getScene());
            if (sizeToScene) stage.sizeToScene();
            if (owner != null) stage.getIcons().setAll(owner.getIcons());
            stage.setOnCloseRequest(closeRequestHandler);
        }
        return stage;
    }
    
    /**
     * Creates the FX object composing the window content.
     * This method MUST invoke the {@link AbstractWindowController#setRoot(javafx.scene.Parent) }.
     */
    protected abstract void makeRoot();
    
    /**
     * Handles on close request.
     * 
     * @param windowEvent WindowEvent source
     */
    public abstract void onCloseRequest(WindowEvent windowEvent);
    
    public void openWindow() {
        assert Platform.isFxApplicationThread();
        getStage().show();
    }
    
    public void closeWindow() {
        assert Platform.isFxApplicationThread();
        getStage().close();
    }
}
