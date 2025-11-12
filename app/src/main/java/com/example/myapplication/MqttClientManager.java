package com.example.myapplication;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class MqttClientManager {
    private MqttAndroidClient client;

    // Connect and report success/failure via the provided IMqttActionListener
    public void connect(Context ctx, String serverUri, String clientId, IMqttActionListener cb) {
        client = new MqttAndroidClient(ctx.getApplicationContext(), serverUri, clientId);

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        try {
            client.connect(opts, null, cb);
        } catch (MqttException e) {
            if (cb != null) cb.onFailure(null, e);
        }
    }

    // Publish a small payload
    public void publish(String topic, String payload) {
        if (client == null || !client.isConnected()) return;
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(0);
            client.publish(topic, msg);
        } catch (Exception ignored) { }
    }

    // Subscribe; returns true if request accepted, false if it failed immediately
    public boolean subscribe(String topic, IMqttMessageListener listener) {
        if (client == null) return false;
        try {
            client.subscribe(topic, 0, listener);
            return true;
        } catch (MqttException e) {
            return false;
        }
    }

    // Disconnect if connected
    public void disconnect() {
        if (client != null) {
            try { client.disconnect(); } catch (Exception ignored) { }
        }
    }
}
