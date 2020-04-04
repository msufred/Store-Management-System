package com.gemseeker.seekiconsfx.material;

import com.gemseeker.seekiconsfx.core.SeekIcon;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author gemini1991
 */
public class SettingsIcon extends SeekIcon {
    
    public SettingsIcon() {
        super();
    }
    
    public SettingsIcon(double size) {
        super(size);
    }
    
    @Override
    protected SVGPath createIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19.4,13c0-0.3,0.1-0.6,0.1-1s0-0.7-0.1-1l2.1-1.7c0.2-0.2,"
                + "0.2-0.4,0.1-0.6l-2-3.5C19.5,5.1,19.3,5,19,5.1l-2.5,1c-0.5-0.4-1"
                + ".1-0.7-1.7-1l-0.4-2.6C14.5,2.2,14.2,2,14,2h-4C9.8,2,9.5,2.2,9.5"
                + ",2.4L9.1,5.1C8.5,5.3,8,5.7,7.4,6.1L5,5.1C4.7,5,4.5,5.1,4.3,5.3l"
                + "-2,3.5C2.2,8.9,2.3,9.2,2.5,9.4L4.6,11c0,0.3-0.1,0.6-0.1,1s0,0.7"
                + ",0.1,1l-2.1,1.7c-0.2,0.2-0.2,0.4-0.1,0.6l2,3.5C4.5,18.9,4.7,19,"
                + "5,18.9l2.5-1c0.5,0.4,1.1,0.7,1.7,1l0.4,2.6c0,0.2,0.2,0.4,0.5,0."
                + "4h4c0.2,0,0.5-0.2,0.5-0.4l0.4-2.6c0.6-0.3,1.2-0.6,1.7-1l2.5,1c0"
                + ".2,0.1,0.5,0,0.6-0.2l2-3.5c0.1-0.2,0.1-0.5-0.1-0.6L19.4,13z M12"
                + ",15.5c-1.9,0-3.5-1.6-3.5-3.5s1.6-3.5,3.5-3.5s3.5,1.6,3.5,3.5S13"
                + ".9,15.5,12,15.5z");
        return path;
    }

    @Override
    protected String getCssStyle() {
        return "material-settings-icon";
    }

}
