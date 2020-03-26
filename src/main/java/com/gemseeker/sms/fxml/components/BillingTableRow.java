package com.gemseeker.sms.fxml.components;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.data.Billing;
import com.gemseeker.sms.data.EnumBillingStatus;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
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
                boolean isDueDate = false;
                try {
                    Calendar now = Calendar.getInstance();
                    Date date = Utils.DATE_FORMAT_2.parse(billing.getDueDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    
                    if (cal.get(Calendar.DAY_OF_MONTH) <= now.get(Calendar.DAY_OF_MONTH) &&
                            cal.get(Calendar.MONTH) <= now.get(Calendar.MONTH) &&
                            cal.get(Calendar.YEAR) <= now.get(Calendar.YEAR)) {
                        isDueDate = true;
                    }
                } catch (ParseException ex) {
                    System.err.println("Error parsing String to Date");
                }
                
                EnumBillingStatus status = billing.getStatus();
                switch (status) {
                    case FOR_REVIEW: setStyle(forReviewStatusStyle); break;
                    case PAID: setStyle(paidStatusStyle); break;
                    case OVERDUE: setStyle(overDueStatusStyle); break;
                    default:
                        if (!isDueDate) {
                            setStyle("");
                        } else {
                            setStyle(overDueStatusStyle);
                        }
                }
            }
        }
    }
    
}
