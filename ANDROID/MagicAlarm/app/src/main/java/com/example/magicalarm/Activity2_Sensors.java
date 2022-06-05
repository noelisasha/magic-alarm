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
    private TextView      accelerometer;
    private TextView      gyroscope;
    private TextView      lightness;
    private TextView      giro;
    private ImageView     lightBulbImg;
    DecimalFormat float2 = new DecimalFormat("###.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        // Defino los botones

        // Defino los TXT para representar los datos de los sensores
        accelerometer  = (TextView) findViewById(R.id.accelerometer);
        gyroscope     = (TextView) findViewById(R.id.gyroscope);
        lightness   = (TextView) findViewById(R.id.lightness);
        giro          = (TextView) findViewById(R.id.giro);
        lightBulbImg = (ImageView) findViewById(R.id.lightBulbimageView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Acceder al servicio de sensores

        //showSensorOutput();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /*private void showSensorOutput() {
        Intent iCommunicate = new Intent();
        iCommunicate.setClass(Activity2_Sensors.this, SensorListActivity.class);

        startActivity(iCommunicate);
    }*/

    protected void initializeSensors() { // Iniciar el acceso a los sensores
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),       SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),           SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),         SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void stopSensorListening() { // Finalizar escucha de sensores

        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
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

                case Sensor.TYPE_ACCELEROMETER :
                    txt += "--- Acelerometro ---\n";
                    txt += "Coord. x = " + float2.format(event.values[0]) + " m/seg2 \n";
                    txt += "Coord. y = " + float2.format(event.values[1]) + " m/seg2 \n";
                    txt += "Coord. z = " + float2.format(event.values[2]) + " m/seg2 \n";
                    if ((event.values[0] > 25) || (event.values[1] > 25) || (event.values[2] > 25)) {
                        txt += "Vibracion Detectada \n";
                    }

                    accelerometer.setText(txt);

                    break;

                case Sensor.TYPE_GYROSCOPE:
                    txt += "--- Giroscopo ---\n";
                    txt += "Coord. x = " + float2.format(event.values[0]) + " deg/s \n";
                    txt += "Coord. y = " + float2.format(event.values[1]) + " deg/s \n";
                    txt += "Coord. z = " + float2.format(event.values[2]) + " deg/s \n";

                    gyroscope.setText(txt);

                    break;

                case Sensor.TYPE_ROTATION_VECTOR :
                    txt += "--- Vector de rotacion ---\n";
                    txt += "Coord. x = " + event.values[0] + "\n";
                    txt += "Coord. y = " + event.values[1] + "\n";
                    txt += "Coord. z = " + event.values[2] + "\n";

                    // Creo objeto para saber como está la pantalla
                    Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int rotation = display.getRotation();

                    // El objeto devuelve 3 estados 0, 1 y 3
                    switch(rotation) {
                        case 0:
                            txt += "Posicion = Vertical \n";
                            break;
                        case 1:
                            txt += "Posicion = Horizontal Izq. \n";
                            break;
                        case 2:
                            txt += "Posicion = Horizontal Der \n";
                            break;
                    }

                    txt += "Display: " + rotation + "\n";
                    giro.setText(txt);

                    break;

                case Sensor.TYPE_LIGHT :
                    txt += "--- Luminosidad ---\n";
                    txt += event.values[0] + " Luz \n";

                    lightness.setText(txt);

                    if(event.values[0] <= 500) {
                        System.out.println("No se detecta luz ==> PRENDER LUZ"); /** DEBUG !! **/
                        lightBulbImg.setColorFilter(Color.parseColor("#000000")); // Si no detectó luz ==> pintar borde de lampara simulando encender luz
                        //lightBulbImg.setBackgroundColor(Color.parseColor("#FFFFEB3B")); // Si no detectó luz ==> pintar fondo de lampara simulando encender luz
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