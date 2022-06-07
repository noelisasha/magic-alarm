package com.example.magicalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

//Communication
import android.content.Intent;

// Sensor Management
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// Logging
import android.util.Log;

// UI Components
import android.graphics.Color;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ImageView;

import java.text.DecimalFormat;

public class Activity2_Sensors extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView      lightness;
    private ImageView     lightBulbImg;
    DecimalFormat float2 = new DecimalFormat("###.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        // Defino los TXT para representar los datos de los sensores
        lightness   = (TextView) findViewById(R.id.lightness);
        lightBulbImg = (ImageView) findViewById(R.id.lightBulbimageView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Acceder al servicio de sensores

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    protected void initializeSensors() { // Iniciar el acceso a los sensores
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),           SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),         SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void stopSensorListening() { // Finalizar escucha de sensores
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { // Escuchar cambio de sensibilidad de los sensores
    }


    @Override
    public void onSensorChanged(SensorEvent event) { // Escuchar cambio en los sensores

        String txt = "";

        // Cada sensor puede lanzar un thread que pasa por aca ==> Accesos Simultaneos
        // Accesos Simultaneos ==> Sincronizar por seguridad
        synchronized (this) {

            Log.d("sensor", event.sensor.getName());

            switch(event.sensor.getType()) {

                case Sensor.TYPE_LIGHT :
                    txt += "--- Análisis del Sensor de Luminosidad ---\n";
                    txt += "\n";
                    txt += "Función: Al tapar el sensor de luz del celular, se iluminará la lamparita de la imagen que esta abajo.\n";
                    txt += "\n";
                    txt += "Medición del Sensor de luz:\n";
                    txt += "\n";
                    txt += event.values[0];

                    lightness.setText(txt);

                    if(event.values[0] <= 500) {
                        System.out.println("No se detecta luz ==> PRENDER LUZ"); /** DEBUG !! **/
                        lightBulbImg.setColorFilter(Color.parseColor("#000000")); // Si no detectó luz ==> pintar borde de lampara simulando encender luz
                    } else {
                        System.out.println("Se detectó luz ==> APAGAR LUZ"); /** DEBUG !! **/
                        lightBulbImg.setColorFilter(Color.parseColor("#FFFFEB3B")); // Si detectó luz ==> pintar borde de lampara simulando apagar luz
                    }

                    break;

                default:
                    Log.d("UI Sensor ==>","Por defecto --> ningun sensor");
                    break;

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeSensors();
    }

    @Override
    protected void onRestart() {
        initializeSensors();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        stopSensorListening();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopSensorListening();
        super.onDestroy();
    }

}
