package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import javafx.scene.control.TableCell;

/**
 *
 * @author gemini1991
 */
public class AccountNameTableCell extends TableCell<Account, String> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty); 
        if (!empty) {
            Account acct = getTableRow().getItem();
            if (acct != null) {
                setText(String.format("%s %s", acct.getFirstName(), acct.getLastName()));
            }
        } else {
            setText("");
        }
    }
    
}
