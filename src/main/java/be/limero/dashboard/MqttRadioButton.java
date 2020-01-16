package be.limero.dashboard;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;

import static be.limero.dashboard.Util.now;

public class MqttRadioButton extends RadioButton implements MqttProperty<Boolean> {
    @Setter @Getter String src="src/unknown";
    @Setter @Getter
    String dst="dst/unknown";
    @Setter
    @Getter
    Boolean retained;
    @Setter
    @Getter
    Integer qos;
    @Setter
    @Getter
    Integer disableTimeout = Integer.MAX_VALUE;
    @Setter
    @Getter
    Mqtt mqtt;

    private static final Logger log
            = LoggerFactory.getLogger(MqttRadioButton.class);
    Timer timer = new Timer(true);
    Long lastUpdated = now();

    public MqttRadioButton() {
        super();
    }

    public void ninitialize(URL url, ResourceBundle resourceBundle) {
        log.info(" initialized topic : " + dst + " disableTimeout " + disableTimeout);
        lastUpdated = now();
        mqtt.register(this);
  /*      this.setOnAction((actionEvent) -> {
            mqtt.publish(dst, this.isSelected());
        });*/
    }


    @Override
    public void onNext(Object obj) {
        boolean b=false;
        if ( obj instanceof  Integer ) {
            if ( (Integer)obj !=0  ) b=true;
        } else if ( obj instanceof  Integer ) {
           b= (Boolean)obj;
        }
        final boolean finalb=b;
        Platform.runLater(()->{
            this.setSelected(finalb);
        });
    }

}
