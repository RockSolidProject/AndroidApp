package com.example.pora_projekt.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

object MqttSender {
    private var MQTT_HOST: String = "b2fd99f96df44eac8c0cc5f50362cf30.s1.eu.hivemq.cloud"
    private var MQTT_PORT: Int = 8883
    public var MQTT_USERNAME: String? = null
    public var MQTT_PASSWORD: String? = null

    private var client: Mqtt3AsyncClient? = null
    private val queue: Queue<Pair<String, String>> = LinkedList()
    private var isConnected = false

    fun setCredentials(username: String, password: String) {
        MQTT_USERNAME = username
        MQTT_PASSWORD = password
    }

    fun connect() : Boolean {
        if (MQTT_USERNAME == null || MQTT_PASSWORD == null) return false
        if (client != null && isConnected) return false
        val clientId = UUID.randomUUID().toString()
        client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(clientId)
            .serverHost(MQTT_HOST)
            .serverPort(MQTT_PORT)
            .sslWithDefaultConfig()
            .buildAsync()

        if (client == null) return false
        client!!.connectWith()
            .simpleAuth()
            .username(MQTT_USERNAME!!)
            .password(MQTT_PASSWORD!!.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                isConnected = throwable == null
                if (isConnected) flushQueue()
            }
        return true
    }

    fun publish(topic: String, payload: String) {
        if (isConnected && client != null) {
            client!!.publishWith()
                .topic(topic)
                .payload(payload.toByteArray(Charsets.UTF_8))
                .send()
        } else {
            queue.add(topic to payload)
        }
    }

    private fun flushQueue() {
        while (queue.isNotEmpty() && client != null) {
            val (topic, payload) = queue.poll()
            client!!.publishWith()
                .topic(topic)
                .payload(payload.toByteArray(Charsets.UTF_8))
                .send()
        }
    }

    fun disconnect() {
        client?.disconnect()
        client = null
        isConnected = false
    }

    fun restart() : Boolean {
        disconnect()
        val result = connect()
        isConnected = result
        return result
    }
}