package com.gemseeker.sms.fxml.print;

import javafx.print.Paper;

/**
 *
 * @author gemini1991
 */
public class PrinterPaper {
    
    private String name;
    private Paper paper;
    
    public PrinterPaper() {
        
    }
    
    public PrinterPaper(Paper paper) {
        this.name = paper.getName();
        this.paper = paper;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Paper getPaper() {
        return paper;
    }
    
    public void setPaper(Paper paper) {
        this.paper = paper;
        this.name = paper.getName();
    }
    
    @Override
    public String toString() {
        return name;
    }
}
