package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

/**
 *
 * @author gemini1991
 */
public class AccountListItem extends ListCell<Account> {

    @FXML Label lblName;
    @FXML Label lblAddress;
    @FXML Label lblStatus;
    
    public AccountListItem() {
        loadFXML();
    }
    
    private void loadFXML() {
        
    }
    
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);
        if (empty || account == null) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("listitem.fxml"));
                loader.setController(this);
                VBox root = (VBox)loader.load();
                
                lblName.setText(String.format("%s %s",
                        account.getAccountUserName()[0],
                        account.getAccountUserName()[1]));
                lblAddress.setText(account.getAddress().toString());
                lblStatus.setText(account.getStatus().toString());
                
                setGraphic(root);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    
    
}
