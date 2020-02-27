package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Account;
import com.gemseeker.sms.data.EnumAccountStatus;
import static com.gemseeker.sms.data.EnumAccountStatus.ACTIVE;
import static com.gemseeker.sms.data.EnumAccountStatus.DISCONNECTED;
import static com.gemseeker.sms.data.EnumAccountStatus.TERMINATED;
import javafx.scene.control.TableRow;

/**
 *
 * @author gemini1991
 */
public class AccountTableRow extends TableRow<Account> {
    
    private final static String STYLE_DISCONNECTED = "-fx-background-color: #e1e1e1;";
    private final static String STYLE_ACTIVE = "-fx-background-color: #87e3be;";
    private final static String STYLE_TERMINATED = "-fx-background-color: #eb7171;";

    @Override
    protected void updateItem(Account acct, boolean empty) {
        super.updateItem(acct, empty); 
        if (empty || acct == null) {
            setStyle("");
        } else {
            if (isSelected()) {
                setStyle("");
            } else {
                EnumAccountStatus status = acct.getStatus();
                setText(status.toString());
                switch(status) {
                    case DISCONNECTED: setStyle(STYLE_DISCONNECTED); break;
                    case ACTIVE: setStyle(STYLE_ACTIVE); break;
                    case TERMINATED: setStyle(STYLE_TERMINATED); break;
                    default: setStyle("");
                }
            }
        }
    }
    
    

}
