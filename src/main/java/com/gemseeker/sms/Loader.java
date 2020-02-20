package com.gemseeker.sms;

import java.io.IOException;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

/**
 * Loads fxml files and saves each FXMLLoader objects of each fxml in a buffer.
 * The class Loader is a singleton, thus there is only one instance of Loader
 * object within the application.
 *
 * @author Gem Seeker
 *
 */
public class Loader {

    private static Loader instance;
    private final HashMap<String, FXMLLoader> loaders;

    private Loader() {
        System.out.println("Created Loader instance!");
        loaders = new HashMap<>();
    }

    /**
     * Loads the fxml file and sets its controller.
     *
     * @param filePath String path of the fxml file
     * @param controller Controller instance associated with the fxml file
     * @return
     */
    public Pane load(String filePath, Object controller) {
        Pane root = null;
        FXMLLoader loader = loaders.get(filePath);
        if (loader != null) {
            root = loader.getRoot();
        } else {
            loader = new FXMLLoader(getClass().getResource(filePath));
            loader.setController(controller);
            try {
                root = loader.load();
                loaders.put(filePath, loader);
            } catch (IOException e) {
                System.err.println("Failed to load fxml file: " + e);
            }
        }
        if (controller != null && root != null) {
            if (controller instanceof Controller) {
                ((Controller)controller).setContentPane(root);
            }
        }
        return root;
    }

    /**
     * Returns an instance of Loader object.
     *
     * @return Loader instance
     */
    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
        }
        return instance;
    }
}
