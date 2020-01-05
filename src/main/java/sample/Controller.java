package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Label fromLabel;
    public Button bookButton;
    public LineChart<Number, Number> linechart;
    public DatePicker fromDate;
    public DatePicker toDate;
    public ProgressBar progress;
    public Slider slider;

    LocalDate fromTime(Long time) {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(date, formatter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Long time = System.currentTimeMillis();
        fromDate.setValue(fromTime(time - 100000000000L));
        toDate.setValue(fromTime(time + 100000000000L));
    }

    public void sayHello(ActionEvent actionEvent) {
        System.out.println("clicked");
        fromLabel.setText(" FROM ");
        bookButton.setDisable(true);

        linechart.getXAxis().setLabel("No of employees");
        linechart.getYAxis().setLabel("Revenue per employee");
        XYChart.Series<Number, Number> dataSeries1 = new XYChart.Series<Number, Number>();
        dataSeries1.setName("2014");

        dataSeries1.getData().add(new XYChart.Data<Number, Number>(1, 567));
        dataSeries1.getData().add(new XYChart.Data(5, 612));
        dataSeries1.getData().add(new XYChart.Data(10, 800));
        dataSeries1.getData().add(new XYChart.Data(20, 780));
        dataSeries1.getData().add(new XYChart.Data(40, 810));
        dataSeries1.getData().add(new XYChart.Data(80, 850));

        linechart.getData().add(dataSeries1);
        progress.setProgress(0.5);
        slider.setValue(50);
    }

    public void colorChosen(ActionEvent actionEvent) {
        System.out.println(actionEvent.getEventType().toString());
    }

    public void textChanged(InputMethodEvent inputMethodEvent) {
        System.out.println(inputMethodEvent.getCommitted());
    }

    public static final LocalDate NOW_LOCAL_DATE() {
        String date = new SimpleDateFormat("dd-MM-  yyyy").format(Calendar.getInstance().getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate;
    }


}
