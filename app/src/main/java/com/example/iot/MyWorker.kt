package com.example.iot

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import com.amazonaws.services.iot.AWSIotClient
import java.util.*

class MyWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    val CUSTOMER_SPECIFIC_ENDPOINT = "a33sxh086vie00-ats.iot.ap-northeast-1.amazonaws.com"

    val COGNITO_POOL_ID = "ap-northeast-1:c08e6d7a-2dab-4792-a724-e7ec215ec910"

    var mIotAndroidClient: AWSIotClient? = null
    var mqttManager: AWSIotMqttManager? = null
    var clientId: String? = null
    var credentialsProvider: CognitoCachingCredentialsProvider? = null

    val MY_REGION: Regions = Regions.AP_NORTHEAST_1
    var login: HashMap<String, String> = hashMapOf()

    override fun doWork(): Result {
        clientId = UUID.randomUUID().toString()

        // Initialize the AWS Cognito credentials provider

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = CognitoCachingCredentialsProvider(
            applicationContext, // context
            COGNITO_POOL_ID, // Identity Pool ID
            MY_REGION, // Region
        )
        credentialsProvider!!.logins = login

        // MQTT Client

        // MQTT Client
        mqttManager = AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT)
        mqttManager!!.isAutoReconnect = true

        mIotAndroidClient = AWSIotClient(credentialsProvider)
        connect()

        return Result.success()
    }

    private fun publish(mqtt: AWSIotMqttManager, topic: String, msg: String) {
        try {
            mqtt.publishString(msg, topic, AWSIotMqttQos.QOS0)
        } catch (e: java.lang.Exception) {
            Log.e("LOG_TAG", "Publish error.", e)
        }
    }

    fun connect() {
        Log.d("LOG_TAG", "clientId = $clientId")
        try {
            mqttManager!!.connect(
                credentialsProvider,
            ) { status, throwable ->
                runOnUiThread {
                    if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting) {
                        Log.e("TAG", "Connecting: ")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                        var topic = inputData.keyValueMap["topic"]
                        Log.e("TAG", topic.toString())
                        var msg = inputData.keyValueMap["msg"]
                        mqttManager?.let { publish(it, topic.toString(), msg.toString()) }
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting) {
                        if (throwable != null) {
                        }
                        Log.e("TAG", "Reconnecting: ")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                        if (throwable != null) {
                            throwable.printStackTrace()
                        }
                        Log.e("TAG", "ConnectionLost: ")
                    } else {
                        Log.e("TAG", "Disconnect: ")
                    }
                }
            }
        } catch (e: Exception) {
            // tvStatus.setText("Error! " + e.message)
        }
    }
}
