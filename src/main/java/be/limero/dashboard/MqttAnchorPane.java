package be.limero.dashboard;

import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;

public class MqttAnchorPane extends AnchorPane {
    @Setter
    @Getter
    String connectionUrl;

    public MqttAnchorPane(){ super();}
}
