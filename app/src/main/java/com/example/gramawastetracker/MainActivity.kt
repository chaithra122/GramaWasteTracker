package com.example.gramawastetracker

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private lateinit var polyline1: Polyline
    private lateinit var polyline2: Polyline
    private lateinit var polyline3: Polyline

    private var marker1: Marker? = null
    private var marker2: Marker? = null
    private var marker3: Marker? = null

    private val handler = Handler(Looper.getMainLooper())

    private var index1 = 0
    private var index2 = 0
    private var index3 = 0

    private val traveledPath1 = mutableListOf<LatLng>()
    private val traveledPath2 = mutableListOf<LatLng>()
    private val traveledPath3 = mutableListOf<LatLng>()

    private val route1 = listOf(

        LatLng(12.9716, 77.5946),
        LatLng(12.9900, 77.6200),
        LatLng(13.0200, 77.6400),
        LatLng(13.0400, 77.6100),
        LatLng(13.0100, 77.5600),
        LatLng(12.9800, 77.5400),
        LatLng(12.9500, 77.5600),
        LatLng(12.9300, 77.6000),
        LatLng(12.9500, 77.6400),
        LatLng(12.9716, 77.5946)

    )

    private val route2 = listOf(

        LatLng(12.9000, 77.5000),
        LatLng(12.9300, 77.4700),
        LatLng(12.9800, 77.4900),
        LatLng(13.0200, 77.5400),
        LatLng(13.0000, 77.5900),
        LatLng(12.9600, 77.6100),
        LatLng(12.9200, 77.5800),
        LatLng(12.9000, 77.5400),
        LatLng(12.9000, 77.5000)

    )

    private val route3 = listOf(

        LatLng(12.9400, 77.7000),
        LatLng(12.9800, 77.7300),
        LatLng(13.0300, 77.7100),
        LatLng(13.0600, 77.6600),
        LatLng(13.0400, 77.6200),
        LatLng(12.9900, 77.6100),
        LatLng(12.9500, 77.6400),
        LatLng(12.9300, 77.6800),
        LatLng(12.9400, 77.7000)

    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(this)

        mapView = MapView(this)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        setContent {

            var showDialog by remember {
                mutableStateOf(false)
            }

            var description by remember {
                mutableStateOf("")
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )

                Button(
                    onClick = {
                        showDialog = true
                    },

                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp)

                ) {

                    Text("Report Garbage")
                }

                if (showDialog) {

                    AlertDialog(

                        onDismissRequest = {
                            showDialog = false
                        },

                        title = {
                            Text("Report Garbage")
                        },

                        text = {

                            OutlinedTextField(

                                value = description,

                                onValueChange = {
                                    description = it
                                },

                                label = {
                                    Text("Enter Description")
                                }
                            )
                        },

                        confirmButton = {

                            Button(

                                onClick = {

                                    val reportRef =
                                        FirebaseDatabase
                                            .getInstance()
                                            .getReference("blackspots")
                                            .push()

                                    reportRef
                                        .child("message")
                                        .setValue(description)

                                    reportRef
                                        .child("lat")
                                        .setValue(marker1?.position?.latitude)

                                    reportRef
                                        .child("lng")
                                        .setValue(marker1?.position?.longitude)

                                    showDialog = false
                                }

                            ) {

                                Text("Submit")
                            }
                        },

                        dismissButton = {

                            Button(

                                onClick = {
                                    showDialog = false
                                }

                            ) {

                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        mapView.getMapAsync { map ->

            googleMap = map

            val defaultLocation =
                LatLng(12.9765, 77.6070)

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    defaultLocation,
                    10f
                )
            )

            polyline1 = googleMap.addPolyline(
                PolylineOptions()
                    .color(Color.BLUE)
                    .width(8f)
            )

            polyline2 = googleMap.addPolyline(
                PolylineOptions()
                    .color(Color.RED)
                    .width(8f)
            )

            polyline3 = googleMap.addPolyline(
                PolylineOptions()
                    .color(Color.GREEN)
                    .width(8f)
            )

            marker1 =
                googleMap.addMarker(
                    MarkerOptions()
                        .position(route1[0])
                        .title("Garbage Vehicle 1")
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.tractor
                                    ),
                                    120,
                                    120,
                                    false
                                )
                            )
                        )
                )

            marker2 =
                googleMap.addMarker(
                    MarkerOptions()
                        .position(route2[0])
                        .title("Garbage Vehicle 2")
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.tractor
                                    ),
                                    120,
                                    120,
                                    false
                                )
                            )
                        )
                )

            marker3 =
                googleMap.addMarker(
                    MarkerOptions()
                        .position(route3[0])
                        .title("Garbage Vehicle 3")
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.tractor
                                    ),
                                    120,
                                    120,
                                    false
                                )
                            )
                        )
                )

            startSimulation1()
            startSimulation2()
            startSimulation3()
        }
    }

    private fun startSimulation1() {

        handler.postDelayed(

            object : Runnable {

                override fun run() {

                    val point = route1[index1]

                    val startPosition =
                        marker1!!.position

                    val animator =
                        ValueAnimator.ofFloat(0f, 1f)

                    animator.duration = 700

                    animator.interpolator =
                        LinearInterpolator()

                    animator.addUpdateListener { animation ->

                        val fraction =
                            animation.animatedFraction

                        val newLat =
                            startPosition.latitude +
                                    (
                                            point.latitude -
                                                    startPosition.latitude
                                            ) * fraction.toDouble()

                        val newLng =
                            startPosition.longitude +
                                    (
                                            point.longitude -
                                                    startPosition.longitude
                                            ) * fraction.toDouble()

                        val newPosition =
                            LatLng(newLat, newLng)

                        marker1!!.position =
                            newPosition

                        traveledPath1.add(newPosition)
                        polyline1.points = traveledPath1

                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLng(newPosition)
                        )
                    }

                    animator.start()

                    index1++

                    if (index1 >= route1.size) {
                        index1 = 0
                    }

                    handler.postDelayed(
                        this,
                        1500
                    )
                }

            },

            1500
        )
    }

    private fun startSimulation2() {

        handler.postDelayed(

            object : Runnable {

                override fun run() {

                    val point = route2[index2]

                    val startPosition =
                        marker2!!.position

                    val animator =
                        ValueAnimator.ofFloat(0f, 1f)

                    animator.duration = 700

                    animator.interpolator =
                        LinearInterpolator()

                    animator.addUpdateListener { animation ->

                        val fraction =
                            animation.animatedFraction

                        val newLat =
                            startPosition.latitude +
                                    (
                                            point.latitude -
                                                    startPosition.latitude
                                            ) * fraction.toDouble()

                        val newLng =
                            startPosition.longitude +
                                    (
                                            point.longitude -
                                                    startPosition.longitude
                                            ) * fraction.toDouble()

                        val newPosition =
                            LatLng(newLat, newLng)

                        marker2!!.position =
                            newPosition

                        traveledPath2.add(newPosition)
                        polyline2.points = traveledPath2
                    }

                    animator.start()

                    index2++

                    if (index2 >= route2.size) {
                        index2 = 0
                    }

                    handler.postDelayed(
                        this,
                        1500
                    )
                }

            },

            1500
        )
    }

    private fun startSimulation3() {

        handler.postDelayed(

            object : Runnable {

                override fun run() {

                    val point = route3[index3]

                    val startPosition =
                        marker3!!.position

                    val animator =
                        ValueAnimator.ofFloat(0f, 1f)

                    animator.duration = 700

                    animator.interpolator =
                        LinearInterpolator()

                    animator.addUpdateListener { animation ->

                        val fraction =
                            animation.animatedFraction

                        val newLat =
                            startPosition.latitude +
                                    (
                                            point.latitude -
                                                    startPosition.latitude
                                            ) * fraction.toDouble()

                        val newLng =
                            startPosition.longitude +
                                    (
                                            point.longitude -
                                                    startPosition.longitude
                                            ) * fraction.toDouble()

                        val newPosition =
                            LatLng(newLat, newLng)

                        marker3!!.position =
                            newPosition

                        traveledPath3.add(newPosition)
                        polyline3.points = traveledPath3
                    }

                    animator.start()

                    index3++

                    if (index3 >= route3.size) {
                        index3 = 0
                    }

                    handler.postDelayed(
                        this,
                        1500
                    )
                }

            },

            1500
        )
    }

    override fun onResume() {

        super.onResume()

        mapView.onResume()
    }

    override fun onPause() {

        mapView.onPause()

        super.onPause()
    }

    override fun onDestroy() {

        mapView.onDestroy()

        super.onDestroy()
    }
}