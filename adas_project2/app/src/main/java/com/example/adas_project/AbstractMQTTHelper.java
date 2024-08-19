package com.example.adas_project;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class AbstractMQTTHelper {
    private MqttClient client;
    private boolean isConnected = false;

    public AbstractMQTTHelper(String serverUri, String clientId, String[] topics) {
        try {
            client = new MqttClient(serverUri, clientId,null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    isConnected = true;
                    Log.i("MQTTHelper", "Connection complete");
                    for (String topic : topics) {
                        try {
                            client.subscribe(topic);
                            Log.i("MQTTHelper", "Subsribed for topic " + topic);


                        } catch (MqttException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.w("Tag", "Connection lost");
                    isConnected = false;
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String msg = new String(message.getPayload());
                    onMessage(topic, msg);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            client.connect(options);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public abstract void onMessage(String topic, String message);

    public void sendMessage(String topic, byte[] payload) throws MqttException {
        client.publish(topic, payload, 0, false);
    }

    public boolean getIsConnected() {
        return isConnected;
    }
}

