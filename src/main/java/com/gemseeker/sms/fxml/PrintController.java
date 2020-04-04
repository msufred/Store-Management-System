package com.gemseeker.sms.fxml;

import com.gemseeker.sms.Utils;
import com.gemseeker.sms.core.AbstractFxmlWindowController;
import com.gemseeker.sms.fxml.components.ProgressBarDialog;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.WindowEvent;

/**
 *
 * @author gemini1991
 */
public class PrintController extends AbstractFxmlWindowController {
    
    @FXML private Button btnPrint;
    @FXML private Spinner<Integer> copiesSpinner;
    @FXML private ChoiceBox<Printer> cbPrinters;
    @FXML private Label lblPrinterStatus;
    @FXML private RadioButton rbPortrait;
    @FXML private RadioButton rbLandscape;
    @FXML private ChoiceBox<PrinterPaper> cbPapers;
    @FXML private Slider zoomSlider;
    @FXML private StackPane contentPane; // paper
    
    // content related components
    @FXML private Slider contentScaleSlider;
    @FXML private Slider contentLeftSlider;
    @FXML private Slider contentTopSlider;
    @FXML private TextField tfScale;
    @FXML private TextField tfLeft;
    @FXML private TextField tfTop;

    // zoom variables
    private SimpleDoubleProperty zoomScaleProperty;
    private Scale zoomScale;
    
    // content scaling variables
    private SimpleDoubleProperty contentScaleProperty;
    private Scale contentScale;
    
    // content positioning variables
    private SimpleDoubleProperty contentTopPositionProperty;
    private SimpleDoubleProperty contentLeftPositionProperty;
    
    private Node nodeToPrint;
    
    private final CompositeDisposable disposables;
    
    public PrintController() {
        super(PrintController.class.getResource("print.fxml"));
        disposables = new CompositeDisposable();
    }
    
    @Override
    public void onFxmlLoaded() {
        // setup copiesSpinner
        copiesSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 100));
        Utils.setAsIntegerTextField(copiesSpinner.getEditor());
        
        // setup orientation RadioButtons
        final ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(rbPortrait, rbLandscape);
        toggleGroup.selectedToggleProperty().addListener((o, t1, t2) -> {
            if (t2 != null) {
                PrinterPaper paper = cbPapers.getValue();
                if (paper != null) checkOrientation(paper);
            }
        });
        
        // setup printer ChoiceBox listener
        cbPrinters.getSelectionModel().selectedItemProperty().addListener((o, p1, p2) -> {
            if (p2 != null) loadSupportedPapers(p2);
        });
        
        // setup papers ChoiceBox listener
        cbPapers.getSelectionModel().selectedItemProperty().addListener((o, p1, p2) -> {
            if (p2 != null) changePaperSize(p2);
        });

        zoomSlider.valueProperty().addListener((o, d1, d2) -> {
            zoomPreview(d2.doubleValue());
        });
        
        btnPrint.setOnAction(evt -> print());
        
        // bind paper's scale to zoomScale
        zoomScaleProperty = new SimpleDoubleProperty(zoomSlider.getValue());
        zoomScale = new Scale();
        zoomScale.xProperty().bind(zoomScaleProperty);
        zoomScale.yProperty().bind(zoomScaleProperty);
        contentPane.getTransforms().add(zoomScale);
        
        // setup the bindings and variables for the content to be printed
        contentScaleProperty = new SimpleDoubleProperty();
        contentScaleProperty.bind(contentScaleSlider.valueProperty());
        
        contentScale = new Scale();
        contentScale.xProperty().bind(contentScaleProperty);
        contentScale.yProperty().bind(contentScaleProperty);
        contentScale.setPivotX(0.0);
        contentScale.setPivotY(0.0);

        contentTopPositionProperty = new SimpleDoubleProperty();
        contentTopPositionProperty.bind(contentTopSlider.valueProperty());
        contentLeftPositionProperty = new SimpleDoubleProperty();
        contentLeftPositionProperty.bind(contentLeftSlider.valueProperty());
        
        contentScaleSlider.valueProperty().addListener((o, v1, v2) -> {
            tfScale.setText(String.valueOf(contentScaleSlider.getValue()));
        });
        
        contentLeftSlider.valueProperty().addListener((o, v1, v2) -> {
            tfLeft.setText(String.valueOf(contentLeftSlider.getValue()));
        });
        
        contentTopSlider.valueProperty().addListener((o, v1, v2) -> {
            tfTop.setText(String.valueOf(contentTopSlider.getValue()));
        });
        
        tfScale.setText(String.valueOf(contentScaleSlider.getValue()));
        tfLeft.setText(String.valueOf(contentLeftSlider.getValue()));
        tfTop.setText(String.valueOf(contentTopSlider.getValue()));
    }

    @Override
    public void onCloseRequest(WindowEvent windowEvent) {
        disposables.dispose();
    }

    private void changePaperSize(PrinterPaper paper) {
        checkOrientation(paper);
        double scale = zoomSlider.getValue();
        zoomPreview(scale);
    }
    
    private void zoomPreview(double zoom) {
        if (zoomScaleProperty != null) zoomScaleProperty.set(zoom);
    }
    
    private void checkOrientation(PrinterPaper printerPaper) {
        if (printerPaper == null) return;
        double w, h;
        Paper paper = printerPaper.getPaper();
        if (rbPortrait.isSelected()) {
            w = paper.getWidth();
            h = paper.getHeight();
        } else {
            w = paper.getHeight();
            h = paper.getWidth();
        }
        contentPane.setPrefSize(w, h);
        contentPane.setMinSize(w, h);
        contentPane.setMaxSize(w, h);
    }
    
    private void fitWidth(boolean fit) {
        if (fit) {
            PrinterPaper printerPaper = cbPapers.getValue();
            if (printerPaper != null) {
                Paper paper = printerPaper.getPaper();
            }
        }
    }
    
    private void print() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        Printer printer = cbPrinters.getValue();
        if (printer != null && printerJob != null) {
            printerJob.setPrinter(printer);
            PrinterPaper srcPaper = cbPapers.getValue();
            Paper paper;
            switch (srcPaper.getName()) {
                case "A4":
                    paper = Paper.A4;
                    break;
                case "Letter":
                    paper = Paper.NA_LETTER;
                    break;
                default:
                    paper = Paper.LEGAL;
                    break;
            }
            
            PageLayout pageLayout = printer.createPageLayout(
                    paper,
                    rbPortrait.isSelected() ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE,
                    0, 0, 0, 0
            );

            double printableWidth = pageLayout.getPrintableWidth();
            double printableHeight = pageLayout.getPrintableHeight();
            
            double nodeWidth = contentPane.getWidth();
            double nodeHeight = contentPane.getHeight();
            
            double scaleX = printableWidth / nodeWidth;
            double scaleY = printableHeight / nodeHeight;
            
            contentPane.getTransforms().clear();
            contentPane.getTransforms().add(new Scale(scaleX, scaleY));
            
            boolean success = printerJob.printPage(pageLayout, contentPane);
            if (success) printerJob.endJob();
            
            contentPane.getTransforms().clear();
            contentPane.getTransforms().add(zoomScale);
        }
    }

    public void openWindow(Node nodeToPrint) {
        openWindow(); // AbstractFxmlWindowController
        contentPane.getChildren().clear();
        addContent(nodeToPrint);
        loadPrintSetup();
    }

    private void addContent(Node nodeToPrint) {
        if (nodeToPrint != null) {
            nodeToPrint.getTransforms().clear();
            nodeToPrint.getTransforms().add(contentScale);
            nodeToPrint.translateXProperty().bind(contentLeftPositionProperty);
            nodeToPrint.translateYProperty().bind(contentTopPositionProperty);
            contentPane.getChildren().add(0, nodeToPrint);
            this.nodeToPrint = nodeToPrint;
        }
    }
    
    private void loadPrintSetup() {
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> {
            ObservableList<Printer> printers = FXCollections.observableArrayList();
            Printer.getAllPrinters().stream().forEach(printer -> printers.add(printer));
            return printers;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(printers -> {
                    ProgressBarDialog.close();
                    cbPrinters.setItems(printers);
                    if (printers.size() > 0) {
                        cbPrinters.getSelectionModel().select(printers.indexOf(Printer.getDefaultPrinter()));
                    }
                }));
    }
    
    private void loadSupportedPapers(Printer printer) {
        if (printer == null) return;
        ProgressBarDialog.show();
        disposables.add(Observable.fromCallable(() -> PrinterUtils.getSupportedPapers(printer.getPrinterAttributes().getSupportedPapers()))
                .subscribeOn(Schedulers.newThread()).observeOn(JavaFxScheduler.platform())
                .subscribe(papers -> {
                    ProgressBarDialog.close();
                    cbPapers.setItems(papers);
                    if (!papers.isEmpty()) {
                        int index;
                        Paper defaultPaper = printer.getPrinterAttributes().getDefaultPaper();
                        for (index =0; index<papers.size(); index++) {
                            if (papers.get(index).getPaper() == defaultPaper) break;
                        }
                        cbPapers.getSelectionModel().select(index);
                    }
                }));
    }
}
