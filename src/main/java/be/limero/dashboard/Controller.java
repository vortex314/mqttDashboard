package be.limero.dashboard;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.ToggleSwitch;
import org.eclipse.paho.client.mqttv3.*;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.font.Script;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    public AnchorPane anchorPane;
    public FlowPane flowPane;
    @FXML
    public Boolean on = true;
    @FXML
    public Mqtt mqtt = new Mqtt();


    private final ObjectProperty<String> selectedClient = new SimpleObjectProperty<>("start String");

    public final String getSelectedClient() {
        return this.selectedClient.get();
    }

    public final void setSelectedClient(String value) {
        this.selectedClient.set(value);
    }

    public final ObjectProperty<String> selectedClientProperty() {
        return this.selectedClient;
    }


    private static final Logger log
            = LoggerFactory.getLogger(Controller.class);

    HashMap<String, Observer<MqttMessage>> topicObserver = new HashMap<String, Observer<MqttMessage>>();


    void onButton(Button button) {
        Object userData = button.getUserData();
    }

    void scanChildren(Pane parent) {
        ObservableList<Node> children = parent.getChildren();
        for (Node node : children) {
            if (node instanceof MqttProperty) {
                MqttProperty mqttProperty = (MqttProperty) node;
                mqttProperty.setMqtt(mqtt);
                mqtt.register(mqttProperty);
                log.info(" " + node.getClass() + " : " + mqttProperty.getSrc());
                if (node instanceof Initializable) ((Initializable) node).initialize(null, null);
            } else if (node instanceof Pane) {
                scanChildren((Pane) node);
            }
        }
    }

    public void onMqtt(String topic, Object object, String action) {
        log.info(" attach " + topic + " to " + object.toString() + " with " + action);
        if (object instanceof Label && action == "setText") {
            mqtt.register(topic, (String tpc, Object obj) -> {
                ((Label) object).setText(obj.toString());
            });
        }
    }

    public void print(Object object) {
        if (object instanceof ScriptObjectMirror) {
            ScriptObjectMirror som = (ScriptObjectMirror) object;
            som.call(this);
        }
        log.info(object.toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Controller started " + url + "  " + resourceBundle);
//        scanChildren(anchorPane);
/*        Window stage =  anchorPane.getScene().getWindow();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                Platform.setImplicitExit(false);
                mqtt.disconnect();
                mqtt.stop();
            }
        });*/
    //    mqtt.connect();
        mqtt.register("src/ESP82-10027/system/alive", (topic, object) -> {
            log.info(topic + " = " + object);
            Platform.runLater(() -> {
                selectedClient.set(object.toString());
            });
        });
        Long time = System.currentTimeMillis();
    }

    public void publish(String topic, String object) {
        log.info(" Controller publish " + topic + " = " + object);
        mqtt.mqttPublish(topic, object);
    }


    public void subscribe(String topic, Control control, Object func) {
        log.info(" subscribe " + topic + " to " + control.toString()+" method "+func.toString());
        mqtt.register(topic, (t, value) -> {
            log.info(t + " = " + value);
            Platform.runLater(() -> {
                // call func with args of object and value func(control,value)
                ScriptObjectMirror so=(ScriptObjectMirror)func;
                so.call(null,control,value);
            });
        });
    }

    public void subscribeExpired(String topic, Control control, Object func) {
        log.info(" subscribeExpired " + topic + " to " + control.toString()+" method "+func.toString());
        mqtt.register(topic, (t, value) -> {
            log.info(t + " = " + value);
            Platform.runLater(() -> {
                // call func with args of object and value func(control,value)
                ScriptObjectMirror so=(ScriptObjectMirror)func;
                so.call(null,control,value);
            });
        });
    }

    class LineChartData {
         Long startTime = 0L;
    };

    public void subscribeLineChart(String topic, LineChart lineChart,Integer index,Integer samples) {
        if (!( lineChart.getUserData() instanceof  LineChartData) ){
            lineChart.setUserData(new LineChartData());
        }
        LineChartData data=(LineChartData)lineChart.getUserData();
        if ( data.startTime==0L ) {
            data.startTime=System.currentTimeMillis();
            XYChart.Series series = new XYChart.Series<Number,Number>();
            series.setName(topic);
            lineChart.getData().add(series);
            lineChart.setUserData(topic);
        }
        mqtt.register(topic, (t, value) -> {
            log.info(t + " = " + value);
            Platform.runLater(() -> {
                Number v=0;
                if ( value instanceof  Double ) {
                    v=(Double)value;
                } else if ( value instanceof  Integer ) {
                    v=(Integer)value;
                } else {
                    log.warn(" unexpected type for graph : "+value.getClass()+" on topic : "+topic);
                }
                ObservableList<XYChart.Series<Number,Number>> chartList = lineChart.getData();
                XYChart.Series<Number,Number> chart = chartList.get(index);
                if ( chart.getData().size()> samples) {
                    chart.getData().remove(0,1);
                }
                chart.getData().add(new XYChart.Data(System.currentTimeMillis()-data.startTime, v));
            });
        });
    }

    public void onToggle(MouseEvent actionEvent) {
        log.info("toggle " + actionEvent.getSource());
    }

    @FXML
    public EventHandler onActionHandler = new EventHandler() {
        @Override
        public void handle(Event event) {
            log.info("event " + event);
        }
    };

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mqtt.stop();
    }
}
