        var mqtt=controller.mqtt

        function publish(topic,msg) {
        controller.publish(topic,JSON.stringify(msg));
        }

        function log(o) {
        print("LOG >> "+JSON.stringify(o));
        }

        function setControlText(control,value) {
        control.text=value
        }

        function setControlDisable(control,value) {
        control.disabled=value
        }



        function powerOn(control,bool){
            publish('dst/pi3/gpio21/mode','OUTPUT')
            if ( bool ) {
                publish('dst/pi3/gpio21/value',bool?1:0)
            } else {
               publish('dst/pi3/gpio21/value',bool?1:0)
            }
        }

                    mqtt.url = mqttUrl.text
                    mqtt.connect()
                    controller.subscribe('src/drive/motor/message',motorMessage,setControlText);
                    controller.subscribe('src/drive/motor/isrCounter',isrCounter,setControlText);
                    controller.subscribeExpired('src/ESP82-10027/system/alive',powerSwitch,setControlDisable);
                    controller.subscribeLineChart('src/drive/motor/rpmMeasured',motorRpm,0,1000);
                    controller.subscribeLineChart('src/drive/motor/rpmTarget',motorRpm,1,1000);
                    controller.subscribeLineChart('src/drive/motor/pwm',motorRpm,2,1000);
                    function setTextFill(control,bool) {
                        log(control);
                        log(bool);
                        if ( bool ) {
                            motorLabel.textFill=javafx.scene.paint.Color.GREEN
                            motorLabel.style="-fx-background-color: green;"
                            powerSwitch.disable=true
                            motorStartButton.disable=true
                            motorStopButton.disable=true
                            motorPauseButton.disable=true
                            }
                        else {
                            motorLabel.textFill=javafx.scene.paint.Color.RED
                            powerSwitch.disable=false
                            }
                    }
                    controller.subscribe('src/ESP82-10027/system/alive',powerSwitch,setTextFill);
                    // controller.subscribeLineChart('src/ESP82-10027/system/counter',espCounter,0,1000);