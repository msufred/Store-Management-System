package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.History;
import java.util.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author gemini1991
 */
public class HistoryDateTableCellFactory implements Callback<TableColumn<History, Date>, TableCell<History, Date>> {

    @Override
    public TableCell<History, Date> call(TableColumn<History, Date> p) {
        return new HistoryDateTableCell();
    }

}
