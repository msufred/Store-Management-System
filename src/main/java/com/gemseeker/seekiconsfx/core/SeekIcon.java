package com.gemseeker.seekiconsfx.core;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public abstract class SeekIcon extends Region {
    
    protected double prefWidth = 18;
    protected double prefHeight = 18;
    
    public SeekIcon() {
        super();
        initIcon();
    }
    
    public SeekIcon(double width, double height) {
        super();
        this.prefWidth = width;
        this.prefHeight = height;
        initIcon();
    }
    
    public SeekIcon(double size) {
        this.prefWidth = size;
        this.prefHeight = size;
        initIcon();
    }
    
    private void initIcon() {
        setPrefSize(prefWidth, prefHeight);
        setMaxSize(prefWidth, prefHeight);
        getStyleClass().add("material-icon");
        getStyleClass().add(getCssStyle());
        setShape(createIcon());
        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    }
    
    protected abstract SVGPath createIcon();
    protected abstract String getCssStyle();
}
