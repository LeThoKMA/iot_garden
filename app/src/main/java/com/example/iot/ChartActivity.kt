package com.example.iot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.regions.Regions
import com.amazonaws.services.iot.AWSIotClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class ChartActivity : AppCompatActivity() {
    lateinit var chart: LineChart

    val topic2 = "\$aws/things/ASasASa/shadow/name/cubicon/get/accepted"
    lateinit var detail: GardentDetail
    var credentialsProvider: CognitoCachingCredentialsProvider? = null
    var login: HashMap<String, String> = hashMapOf()
    var listTemp: ArrayList<Entry> = arrayListOf()
    var listHum: ArrayList<Entry> = arrayListOf()
    var listFlux: ArrayList<Entry> = arrayListOf()
    private val topic = "\$aws/things/ASasASa/shadow/name/cubicon/update/accepted"

    var mIotAndroidClient: AWSIotClient? = null
    var mqttManager: AWSIotMqttManager? = null
    var clientId: String? = null
    var listDataSet: ArrayList<ILineDataSet> = arrayListOf()
    val CUSTOMER_SPECIFIC_ENDPOINT = "a33sxh086vie00-ats.iot.ap-northeast-1.amazonaws.com"

    val COGNITO_POOL_ID = "ap-northeast-1:c08e6d7a-2dab-4792-a724-e7ec215ec910"

    val MY_REGION: Regions = Regions.AP_NORTHEAST_1
    lateinit var lineData: LineData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        chart = findViewById(R.id.line_chart)

        var listTempPoint: ArrayList<Point?> = arrayListOf()
        var listHumPoint: ArrayList<Point?> = arrayListOf()
        var listFluxPoint: ArrayList<Point?> = arrayListOf()

        listTempPoint = Gson().fromJson(
            intent.getStringExtra("temp"),
            object : TypeToken<List<Point?>?>() {}.type,
        )
        listHumPoint = Gson().fromJson(
            intent.getStringExtra("hum"),
            object : TypeToken<List<Point?>?>() {}.type,
        )
        listFluxPoint = Gson().fromJson(
            intent.getStringExtra("flux"),
            object : TypeToken<List<Point?>?>() {}.type,
        )
        for (i in listTempPoint) {
            listTemp.add(Entry((i?.x ?: 0) as Float, (i?.y ?: 0) as Float))
        }
        for (i in listHumPoint) {
            listHum.add(Entry((i?.x ?: 0) as Float, (i?.y ?: 0) as Float))
        }

        for (i in listFluxPoint) {
            listFlux.add(Entry((i?.x ?: 0) as Float, (i?.y ?: 0) as Float))
        }

        var lineTempDataSet: LineDataSet = LineDataSet(listTemp, "Temp")
        lineTempDataSet.color = (ContextCompat.getColor(this, R.color.red))

        var lineHumiDataSet: LineDataSet = LineDataSet(listHum, "Humi")
        lineHumiDataSet.color = (ContextCompat.getColor(this, R.color.purple_700))

        var lineFluxDataSet: LineDataSet = LineDataSet(listFlux, "Flux")
        lineFluxDataSet.color = (ContextCompat.getColor(this, R.color.colorYellowDark))

        listDataSet.add(lineTempDataSet)
        listDataSet.add(lineHumiDataSet)
        listDataSet.add(lineFluxDataSet)

        lineData = LineData(listDataSet)

        chart.xAxis.labelCount = listTemp.size
        chart.xAxis.isEnabled = false
        chart.description.text = ""
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT

        // chart.xAxis.valueFormatter=IndexAxisValueFormatter()
        chart.data = lineData
        chart.invalidate()
    }
}
