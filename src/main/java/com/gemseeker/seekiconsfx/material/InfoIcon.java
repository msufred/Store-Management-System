package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class InfoIcon extends SeekIcon {
    
    public InfoIcon() {
        super();
    }
    
    public InfoIcon(double size) {
        super(size);
    }
    
    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12,2C6.5,2,2,6.5,2,12s4.5,10,10,10c5.5,0,10-4.5,10-10S1"
                + "7.5,2,12,2z M13,17h-2v-6h2V17z M13,9h-2V7h2V9z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-info-icon";
    }

}
