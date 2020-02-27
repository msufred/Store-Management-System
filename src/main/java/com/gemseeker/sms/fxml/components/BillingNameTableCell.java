package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.Billing;
import javafx.scene.control.TableCell;

/**
 *
 * @author gemini1991
 */
public class BillingNameTableCell extends TableCell<Billing, Account> {

    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);
        setGraphic(null);
        if (empty) {
            setText("");
        } else {
            if (account != null) {
                setText(String.format("%s %s", account.getFirstName(), account.getLastName()));
            } else {
                setText("< Account Removed >");
            }
        }
    }
    
}
