package com.example.maplabdemo2

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.maplabdemo2.ContentPackage
import com.google.android.gms.maps.CameraUpdateFactory
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.util.UUID
import com.google.gson.Gson
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var client: Mqtt5BlockingClient? = null
    private var latitude: Double = 10.6416
    private var longitude: Double = -61.3995
    private var studentID: String = "816035550"
    private var content: ContentPackage = ContentPackage(studentID, latitude, longitude)
    // setting initial values that should be overitten later if everything works
    private lateinit var message: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker-816032311.sundaebytestt.com")
            .serverPort(1883)
            .build()
            .toBlocking()

        try {
            client!!.connect()
            client!!.subscribeWith().topicFilter("assignment/location").send()
            client!!.toAsync().publishes(MqttGlobalPublishFilter.ALL) { publish ->
                message = String(publish.payloadAsBytes)

                if (message == "") {
                    message =  String((Gson().toJson(content)).toByteArray())
                }
                content = Gson().fromJson(message, ContentPackage::class.java)
                latitude = content.latitude
                longitude = content.longitude
            }

        } catch (e: Exception) {
            Log.e("MQTT", "Failed to connect to server: ${e.message}")
            }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val items = mutableListOf<String>()

        val listView: ListView = findViewById(R.id.listView)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        listView.adapter = adapter

        items.add(content.studentID)

    }


    override fun onMapReady(p0: GoogleMap) {
        mMap = p0;
        val newmmark = LatLng(latitude, longitude) //1
        addMarkerAtLocation(newmmark)
        Log.d("LOAD", "The map loads")
    }

    private fun addMarkerAtLocation(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("First Marker")
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }
}

