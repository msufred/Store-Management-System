package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author gemini1991
 */
public class BillingNameTableCellFactory implements Callback<TableColumn<Billing, Account>, TableCell<Billing, Account>> {

    @Override
    public TableCell<Billing, Account> call(TableColumn<Billing, Account> p) {
        return new BillingNameTableCell();
    }

}
