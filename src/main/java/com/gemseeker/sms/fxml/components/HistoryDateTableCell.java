package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.History;
import java.util.Date;
import javafx.scene.control.TableCell;

/**
 *
 * @author gemini1991
 */
public class HistoryDateTableCell extends TableCell<History, Date> {
    @Override
    protected void updateItem(Date date, boolean empty) {
        super.updateItem(date, empty);
        setGraphic(null);
        if (date == null || empty) {
            setText("");
        } else {
            setText(Utils.TABLE_DATE_FORMAT.format(date));
        }
    }
}
