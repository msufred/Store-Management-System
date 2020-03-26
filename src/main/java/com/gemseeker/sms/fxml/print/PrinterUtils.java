package com.gemseeker.sms.fxml.print;

import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.print.Paper;

/**
 *
 * @author gemini1991
 */
public final class PrinterUtils {

    public static ObservableList<PrinterPaper> getSupportedPapers(Set<Paper> papers) {
        ObservableList<PrinterPaper> supportedPapers = FXCollections.observableArrayList();
        papers.forEach(paper -> supportedPapers.add(new PrinterPaper(paper)));
        return supportedPapers;
    }

}
