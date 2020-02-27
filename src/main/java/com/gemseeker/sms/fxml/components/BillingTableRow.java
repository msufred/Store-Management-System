package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.EnumBillingStatus;
import javafx.scene.control.TableRow;

/**
 * Used by Billing TableView to change row background bases on Billing status.
 * 
 * @author gemini1991
 */
public class BillingTableRow extends TableRow<Billing> {
    
    private final String forReviewStatusStyle = "-fx-background-color: #e1e1e1;";
    private final String paidStatusStyle = "-fx-background-color: #87e3be;";
    private final String overDueStatusStyle = "-fx-background-color: #eb7171;";

    @Override
    protected void updateItem(Billing billing, boolean empty) {
        super.updateItem(billing, empty);
        if (billing == null || empty) {
            setStyle("");
        } else {
            if (isSelected()) {
                setStyle("");
            } else {
                EnumBillingStatus status = billing.getStatus();
                switch (status) {
                    case FOR_REVIEW: setStyle(forReviewStatusStyle); break;
                    case PAID: setStyle(paidStatusStyle); break;
                    case OVERDUE: setStyle(overDueStatusStyle); break;
                    default:
                        setStyle("");
                }
            }
        }
    }
    
}
