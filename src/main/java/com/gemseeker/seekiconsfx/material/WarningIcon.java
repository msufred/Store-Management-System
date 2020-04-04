package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class WarningIcon extends SeekIcon {
    
    public WarningIcon() {
        super();
    }

    public WarningIcon(double size) {
        super(size, size * 0.875);
    }
    
    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M1,21h22L12,2L1,21z M13,18h-2v-2h2V18z M13,14h-2v-4h2V14z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-warning-icon";
    }

}
