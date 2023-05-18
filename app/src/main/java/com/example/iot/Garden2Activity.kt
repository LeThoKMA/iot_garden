package com.example.iot

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.*
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import com.amazonaws.services.iot.AWSIotClient
import com.example.iot.databinding.ActivityGardenBinding
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class Garden2Activity : AppCompatActivity() {

    var oldHum = 0
    var oldTemp = 0
    var oldLamp = 0
    var oldPump = 0
    var oldFlux = 0
    lateinit var timePicker: TimePickerDialog
    lateinit var tv_light: TextView
    lateinit var worker: MyWorker
    lateinit var detail: GardentDetail
    var loadingDialog: AlertDialog? = null

    val simpleDateFormatt = SimpleDateFormat("HH:mm")
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH-mm")
    lateinit var binding: ActivityGardenBinding
    var count = 0
    var min = ""
    var hour = ""
    private val topic = "\$aws/things/ASasASa/shadow/name/cubicon/update/accepted"
    private val msgden1_on = "cubimess"
    val topic2 = "\$aws/things/ASasASa/shadow/name/cubicon/get/accepted"

    val LOG_TAG = MainActivity::class.java.canonicalName

    val CUSTOMER_SPECIFIC_ENDPOINT = "a33sxh086vie00-ats.iot.ap-northeast-1.amazonaws.com"

    val COGNITO_POOL_ID = "ap-northeast-1:c08e6d7a-2dab-4792-a724-e7ec215ec910"

    val MY_REGION: Regions = Regions.AP_NORTHEAST_1

    var listTemp: ArrayList<Point> = arrayListOf()
    var listHump: ArrayList<Point> = arrayListOf()

    var listFlux: ArrayList<Point> = arrayListOf()
    var mIotAndroidClient: AWSIotClient? = null
    var mqttManager: AWSIotMqttManager? = null
    var clientId: String? = null
    lateinit var preference: MyPreference
    var credentialsProvider: CognitoCachingCredentialsProvider? = null
    var login: HashMap<String, String> = hashMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garden)
        loadingDialog = setupProgressDialog()
        //   timePicker= TimePickerDialog()
        preference = MyPreference().getInstance(this)!!
        binding = DataBindingUtil.setContentView(this, R.layout.activity_garden)
        detail = GardentDetail()
        binding.tvToolbar.text = "Garden 2"

        binding.tvSettime1.text = preference.getTextTime("g2_text1")
        binding.edtTemp1.setText(preference.getTextTemp("g2_temp1"))
        binding.edtHumi1.setText(preference.getTextHumi("g2_humi1"))

        binding.tvSettime2.text = preference.getTextTime("g2_text2")
        binding.edtTemp2.setText(preference.getTextTemp("g2_temp2"))
        binding.edtHumi2.setText(preference.getTextHumi("g2_humi2"))

        binding.tvSettime3.text = preference.getTextTime("g2_text3")
        binding.edtTemp3.setText(preference.getTextTemp("g2_temp3"))
        binding.edtHumi3.setText(preference.getTextHumi("g2_humi3"))

        binding.imageView.visibility = View.VISIBLE
        binding.imvPumpOn.visibility = View.GONE

        binding.imvLampOn.visibility = View.INVISIBLE
        binding.imvLampOff.visibility = View.VISIBLE

        test()
        initListener()
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun initListener() {
        binding.tvLight.setOnClickListener {
            publish("lamp_on", topic)
//            binding.imvLampOn.visibility=View.VISIBLE
//            binding.imvLampOff.visibility=View.INVISIBLE
            Toast.makeText(this, "Đang bật ...", Toast.LENGTH_LONG).show()
        }
        binding.tvLightOff.setOnClickListener {
            publish("lamp_off", topic)
//            binding.imvLampOn.visibility=View.INVISIBLE
//            binding.imvLampOff.visibility=View.VISIBLE
            Toast.makeText(this, "Đang tắt...", Toast.LENGTH_LONG).show()
        }
        binding.tvPump.setOnClickListener {
            publish("pump2_on", topic)
//            binding.imageView.visibility=View.GONE
//            binding.imvPumpOn.visibility=View.VISIBLE
            Toast.makeText(this, "Đang bật...", Toast.LENGTH_LONG).show()
        }
        binding.tvPumpOff.setOnClickListener {
            publish("pump2_off", topic)
//            binding.imageView.visibility=View.VISIBLE
//            binding.imvPumpOn.visibility=View.GONE
            Toast.makeText(this, "Đang tắt...", Toast.LENGTH_LONG).show()
        }
        binding.tvSettime1.setOnClickListener {
            openTimerPickerDialog(binding.tvSettime1)
        }
        binding.tvSettime2.setOnClickListener {
            openTimerPickerDialog(binding.tvSettime2)
        }
        binding.tvSettime3.setOnClickListener {
            openTimerPickerDialog(binding.tvSettime3)
        }
        binding.btnok1.setOnClickListener {
            if (binding.edtTemp1.text.toString().isNotBlank() && binding.edtTemp1.text.toString()
                    .toInt() > 0 && binding.edtTemp1.text.toString()
                    .toInt() < 70 && binding.edtHumi1.text.toString().isNotBlank()
            ) {
                if (binding.tvSettime1.text.isNotBlank()) {
                    var pumpRequest = PumpRequest(
                        binding.edtHumi1.text.toString().toInt(),
                        "v2",
                        binding.edtTemp1.text.toString().toInt(),
                    )
                    mqttManager?.disconnect()
                    var data = Data.Builder()
                    Log.e("TAG", Gson().toJson(mqttManager.toString()))

                    data.put("topic", topic)
                    data.put("msg", Gson().toJson(pumpRequest).toString())

                    var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                    var month = Calendar.getInstance().get(Calendar.MONTH).plus(1).toString()
                    var day = Calendar.getInstance().get(Calendar.DATE).toString()
                    val time = year + "-" + month + "-" + day + " " + hour + "-" + min

                    var time_gone = simpleDateFormat.parse(time)?.time
                    simpleDateFormat.parse(time)?.let { it1 -> Log.e("TAG", it1.toString()) }
                    Log.e("TAG", simpleDateFormat.parse(time)?.time.toString())
                    Log.e("TAG", Calendar.getInstance().time.time.toString())
                    var myWorker: WorkRequest =
                        time_gone?.let { it1 ->
                            OneTimeWorkRequest.Builder(MyWorker::class.java)
                                .setInputData(data.build()).setInitialDelay(
                                    it1.minus(Calendar.getInstance().time.time),
                                    TimeUnit.MILLISECONDS,
                                ).build()
                        }!!

                    WorkManager.getInstance(this).enqueue(myWorker)
                    preference.saveTimeGarden("g2_t1", time_gone)
                    preference.saveTempGarden("g2_temp1", binding.edtTemp1.text.toString())
                    preference.saveTempGarden("g2_humi1", binding.edtHumi1.text.toString())
                    preference.setTextTime("g2_text1", binding.tvSettime1.text.toString())
                    binding.tvSettime1.isEnabled = false
                    test()
                    Toast.makeText(this, "Đã lên lịch", Toast.LENGTH_LONG).show()
                    binding.btnok1.isEnabled = false
                }
            }
        }
        binding.icBack.setOnClickListener {
            finish()
        }
        binding.checkbox.setOnClickListener {
            if (binding.checkbox.isChecked) {
                binding.csManu.visibility = View.GONE
                binding.linear.visibility = View.VISIBLE
                publish("lamp_auto", topic)
            } else {
                binding.csManu.visibility = View.VISIBLE
                binding.linear.visibility = View.GONE
            }
        }
        binding.btnok2.setOnClickListener {
            if (binding.edtTemp2.text.toString().isNotBlank() && binding.edtTemp2.text.toString()
                    .toInt() > 0 && binding.edtTemp2.text.toString()
                    .toInt() < 70 && binding.edtHumi2.text.toString().isNotBlank()
            ) {
                if (binding.tvSettime2.text.isNotBlank()) {
                    var pumpRequest = PumpRequest(
                        binding.edtHumi2.text.toString().toInt(),
                        "v2",
                        binding.edtTemp2.text.toString().toInt(),
                    )
                    mqttManager?.disconnect()
                    var data = Data.Builder()
                    Log.e("TAG", Gson().toJson(mqttManager.toString()))

                    data.put("topic", topic)
                    data.put("msg", Gson().toJson(pumpRequest).toString())

                    var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                    var month = Calendar.getInstance().get(Calendar.MONTH).plus(1).toString()
                    var day = Calendar.getInstance().get(Calendar.DATE).toString()
                    val time = year + "-" + month + "-" + day + " " + hour + "-" + min

                    var time_gone = simpleDateFormat.parse(time)?.time
                    simpleDateFormat.parse(time)?.let { it1 -> Log.e("TAG", it1.toString()) }
                    Log.e("TAG", simpleDateFormat.parse(time)?.time.toString())
                    Log.e("TAG", Calendar.getInstance().time.time.toString())
                    var myWorker: WorkRequest =
                        time_gone?.let { it1 ->
                            OneTimeWorkRequest.Builder(MyWorker::class.java)
                                .setInputData(data.build()).setInitialDelay(
                                    it1.minus(Calendar.getInstance().time.time),
                                    TimeUnit.MILLISECONDS,
                                ).build()
                        }!!

                    WorkManager.getInstance(this).enqueue(myWorker)
                    preference.saveTimeGarden("g2_t2", time_gone)
                    preference.saveTempGarden("g2_temp2", binding.edtTemp2.text.toString())
                    preference.saveTempGarden("g2_humi2", binding.edtHumi2.text.toString())

                    preference.setTextTime("g2_text2", binding.tvSettime2.text.toString())

                    binding.tvSettime2.isEnabled = false
                    test()
                    Toast.makeText(this, "Đã lên lịch", Toast.LENGTH_LONG).show()
                    binding.btnok2.isEnabled = false
                }
            }
        }

        binding.btnok3.setOnClickListener {
            if (binding.edtTemp3.text.toString().isNotBlank() && binding.edtTemp3.text.toString()
                    .toInt() > 0 && binding.edtTemp3.text.toString()
                    .toInt() < 70 && binding.edtHumi3.text.toString().isNotBlank()
            ) {
                if (binding.tvSettime3.text.isNotBlank()) {
                    var pumpRequest = PumpRequest(
                        binding.edtHumi3.text.toString().toInt(),
                        "v2",
                        binding.edtTemp3.text.toString().toInt(),
                    )
                    mqttManager?.disconnect()
                    var data = Data.Builder()
                    Log.e("TAG", Gson().toJson(mqttManager.toString()))

                    data.put("topic", topic)
                    data.put("msg", Gson().toJson(pumpRequest).toString())

                    var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                    var month = Calendar.getInstance().get(Calendar.MONTH).plus(1).toString()
                    var day = Calendar.getInstance().get(Calendar.DATE).toString()
                    val time = year + "-" + month + "-" + day + " " + hour + "-" + min

                    var time_gone = simpleDateFormat.parse(time)?.time
                    simpleDateFormat.parse(time)?.let { it1 -> Log.e("TAG", it1.toString()) }
                    Log.e("TAG", simpleDateFormat.parse(time)?.time.toString())
                    Log.e("TAG", Calendar.getInstance().time.time.toString())
                    var myWorker: WorkRequest =
                        time_gone?.let { it1 ->
                            OneTimeWorkRequest.Builder(MyWorker::class.java)
                                .setInputData(data.build()).setInitialDelay(
                                    it1.minus(Calendar.getInstance().time.time),
                                    TimeUnit.MILLISECONDS,
                                ).build()
                        }!!

                    WorkManager.getInstance(this).enqueue(myWorker)
                    preference.saveTimeGarden("g2_t3", time_gone)
                    preference.saveTempGarden("g2_temp3", binding.edtTemp3.text.toString())
                    preference.saveTempGarden("g2_humi3", binding.edtHumi3.text.toString())
                    preference.setTextTime("g2_text3", binding.tvSettime3.text.toString())

                    binding.tvSettime3.isEnabled = false
                    test()
                    Toast.makeText(this, "Đã lên lịch", Toast.LENGTH_LONG).show()
                    binding.btnok3.isEnabled = false
                }
            }
        }
        binding.tvNext.setOnClickListener {
            var intent = Intent(this, ChartActivity::class.java)
            intent.putExtra("temp", Gson().toJson(listTemp))
            intent.putExtra("hum", Gson().toJson(listHump))
            intent.putExtra("flux", Gson().toJson(listFlux))

            intent.putExtra("publish", "update2")
            startActivity(intent)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun test() {
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
    }

    fun connect() {
        Log.d(LOG_TAG, "clientId = $clientId")
        try {
            mqttManager!!.connect(
                credentialsProvider,
            ) { status, throwable ->
                runOnUiThread {
                    if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting) {
                        Log.e("TAG", "Connecting: ")
                        loadingDialog?.show()
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                        publish("update2", topic)
                        subscribe(topic2)
                        loadingDialog?.dismiss()
                        Log.e("TAG", "Connected: ")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "Connection error.", throwable)
                            loadingDialog?.show()
                        }
                        Log.e("TAG", "Reconnecting: ")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "Connection error.", throwable)
                            loadingDialog?.dismiss()
                            throwable.printStackTrace()
                        }
                        Log.e("TAG", "ConnectionLost: ")
                    } else {
                        Log.e("TAG", "Disconnect: ")
                        loadingDialog?.dismiss()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Connection error.", e)
            // tvStatus.setText("Error! " + e.message)
        }
    }

    fun subscribe(topic: String) {
        Log.d(LOG_TAG, "topic = $topic")
        try {
            mqttManager!!.subscribeToTopic(
                topic,
                AWSIotMqttQos.QOS0,
            ) { topic, data ->
                runOnUiThread {
                    try {
                        val message = String(data)

                        detail = Gson().fromJson(message, GardentDetail::class.java)
                        try {
                            oldHum = detail.hum2.toString().toInt()
                        } catch (e: Exception) {
                        }

                        try {
                            oldLamp = detail.lamp.toString().toInt()
                        } catch (e: Exception) {
                        }
                        try {
                            oldTemp = detail.temp.toString().toInt()
                        } catch (e: Exception) {
                        }
                        try {
                            oldPump = detail.pump2.toString().toInt()
                        } catch (e: Exception) {
                        }
                        try {
                            oldFlux = detail.flux.toString().toInt()
                        } catch (e: Exception) {
                        }

                        if (detail != null) {
                            if (listTemp.size == 100) {
                                listTemp.clear()
                                count = 0
                            }
                            if (listHump.size == 100) {
                                listHump.clear()
                            }
                            if (listFlux.size == 100) {
                                listFlux.clear()
                            }
                            listTemp.add(
                                Point(
                                    count.toFloat(),
                                    (oldTemp.toFloat() ?: 0.0) as Float,
                                ),
                            )

                            listHump.add(
                                Point(
                                    count.toFloat(),
                                    (oldHum.toFloat() ?: 0.0) as Float,
                                ),
                            )
                            listFlux.add(
                                Point(
                                    count.toFloat(),
                                    (oldFlux.toFloat() ?: 0.0) as Float,
                                ),
                            )
                            count += 5
                        }
                        Log.d(LOG_TAG, "Message arrived:")
                        Log.d(LOG_TAG, "   Topic: $topic")
                        Log.d(LOG_TAG, " Message: $message")
                        binding.tvTemp.text = oldTemp.toString()
                        binding.tvHumi.text = oldHum.toString()

                        if (oldLamp == 0) {
                            binding.imvLampOn.visibility = View.INVISIBLE
                            binding.imvLampOff.visibility = View.VISIBLE
                        } else {
                            binding.imvLampOn.visibility = View.VISIBLE
                            binding.imvLampOff.visibility = View.INVISIBLE
                        }

                        if (oldPump == 0) {
                            binding.imageView.visibility = View.VISIBLE
                            binding.imvPumpOn.visibility = View.GONE
                        } else {
                            binding.imageView.visibility = View.GONE
                            binding.imvPumpOn.visibility = View.VISIBLE
                        }

                        Handler(Looper.getMainLooper()).postDelayed(
                            Runnable {
                                publish("update2", this.topic)
                            },
                            5000,
                        )
                    } catch (e: UnsupportedEncodingException) {
                        Log.e(LOG_TAG, "Message encoding error.", e)
                        //    tvStatus.setText("subscribe error")
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(LOG_TAG, "Subscription error.", e)
        }
    }

    fun publish(msg: String?, topic: String?) {
        try {
            mqttManager!!.publishString(msg, topic, AWSIotMqttQos.QOS0)
        } catch (e: java.lang.Exception) {
            Log.e(LOG_TAG, "Publish error.", e)
        }
    }

    private fun openTimerPickerDialog(view: TextView) {
        var timeSetListener = TimePickerDialog.OnTimeSetListener { _, p1, p2 ->

            // check số kí tự <2
            hour = if (p1 < 9) {
                binding.root.context.getString(R.string.day_month_with_0, p1)
            } else {
                p1.toString()
            }
            min = if (p2 < 10) {
                binding.root.context.getString(R.string.day_month_with_0, p2)
            } else {
                p2.toString()
            }

            var year = Calendar.getInstance().get(Calendar.YEAR).toString()
            var month = Calendar.getInstance().get(Calendar.MONTH).plus(1).toString()
            var day = Calendar.getInstance().get(Calendar.DATE).toString()
            val s = year + "-" + month + "-" + day + " " + p1 + "-" + p2

            val localTime = simpleDateFormat.parse(s)
            Log.e("TAG", localTime.toString())

            if (Calendar.getInstance().time.after(localTime)) {
                view.text = simpleDateFormatt.format(Calendar.getInstance().time)
            } else {
                view.text =
                    binding.root.context.getString(R.string.time_simple, hour, min)
            }
            Calendar.getInstance().get(Calendar.HOUR)
            Calendar.getInstance().get(Calendar.MINUTE)
        }
        var timePickerDialog = TimePickerDialog(
            binding.root.context,
            timeSetListener,
            0,
            0,
            true,
        )
        timePickerDialog.show()
    }

    override fun onDestroy() {
        mqttManager?.disconnect()
        super.onDestroy()
    }

    private fun setupProgressDialog(): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setCancelable(false)

        val myLayout = LayoutInflater.from(this)
        val dialogView: View = myLayout.inflate(R.layout.fragment_progress_dialog, null)

        builder.setView(dialogView)

        val dialog: AlertDialog = builder.create()
        val window: Window? = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }
}
