package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class SaveIcon extends SeekIcon {

    public SaveIcon() {
        super();
    }
    
    public SaveIcon(double size) {
        super(size);
    }

    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M17,3H5C3.9,3,3,3.9,3,5l0,14c0,1.1,0.9,2,2,2h14c1.1,0,2-0"
                + ".9,2-2V7L17,3z M12,19c-1.7,0-3-1.3-3-3s1.3-3,3-3c1.7,0,3,1.3,3,"
                + "3S13.7,19,12,19z M15,9H5V5h10V9z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-save-icon";
    }

}
