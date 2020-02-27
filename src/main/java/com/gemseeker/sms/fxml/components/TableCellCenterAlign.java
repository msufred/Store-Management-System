package com.gemseeker.sms.fxml.components;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.apache.poi.ss.formula.functions.T;

/**
 *
 * @author gemini1991
 */
public class TableCellCenterAlign<T> extends TableCell<T, String>{

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setAlignment(Pos.CENTER);
        if (!empty) setText(item);
    }
    
}
