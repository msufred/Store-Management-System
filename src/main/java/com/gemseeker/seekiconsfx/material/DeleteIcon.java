package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class DeleteIcon extends SeekIcon {
    
    public DeleteIcon() {
        super();
    }
    
    public DeleteIcon(double size) {
        super(size * 0.75, size);
    }
    
    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M6,19c0,1.1,0.9,2,2,2h8c1.1,0,2-0.9,2-2V7H6V19z "
                + "M19,4h-3.5l-1-1h-5l-1,1H5v2h14V4z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-delete-icon";
    }

}
