package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 *
 * @author gemini1991
 */
public class AccountListCellFactory implements Callback<ListView<Account>, ListCell<Account>>{

    @Override
    public ListCell<Account> call(ListView<Account> p) {
        return new AccountListItem();
    }


}
