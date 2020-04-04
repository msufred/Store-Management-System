package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class HistoryIcon extends SeekIcon {
    
    public HistoryIcon() {
        super();
    }
    
    public HistoryIcon(double size) {
        super(size);
    }
    
    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12.5,2C9,2,5.9,3.9,4.3,6.8L2,4.5V11h6.5L5.7,8.2C7,5.7,9."
                + "5,4,12.5,4c4.1,0,7.5,3.4,7.5,7.5c0,4.1-3.4,7.5-7.5,7.5c-3.3,0-6"
                + "-2.1-7.1-5H3.3c1.1,4,4.8,7,9.2,7c5.3,0,9.5-4.3,9.5-9.5S17.7,2,"
                + "12.5,2z M11,7v5.1l4.7,2.8l0.8-1.3l-4-2.4V7H11z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-history-icon";
    }

}
