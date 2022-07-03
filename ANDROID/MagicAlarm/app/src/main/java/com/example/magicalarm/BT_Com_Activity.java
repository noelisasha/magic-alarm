package com.example.magicalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BT_Com_Activity extends AppCompatActivity {

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private String nombreUsr;
    private String chosenSong;
    private String lightReading;
    private String playMusic;

    private ConnectedThread mConnectedThread;

    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        nombreUsr = extras.getString("nombreUsr");
        chosenSong = extras.getString("chosenSong");
        lightReading = extras.getString("LightReading");
        playMusic = extras.getString("PlayMusic");

        BluetoothDevice mDevice = getIntent().getExtras().getParcelable("arduinoDevice");
        address = mDevice.getAddress();
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        /* Definir el Handler de comunicacion entre el hilo Principal  el secundario.
        El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler*/
        bluetoothIn = Handler_Msg_Hilo_Principal();

    }

    @Override
    /*Cada vez que se detecta el evento OnResume, se establece la comunicacion con el HC05,
    creando un socketBluethoot*/
    public void onResume() {
        super.onResume();

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            //showToast( "La creacción del Socket fallo");
            System.out.println("ERROR! La creacción del Socket fallo."); /** DEBUG !! **/
        }
        // Establish the Bluetooth socket connection.
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }

        /* Una vez establecida la conexion con el Hc05, crear el hilo secundario,
        para recibir los datos de Arduino a traves del bluetooth */
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        if(chosenSong != null){
            mConnectedThread.write(chosenSong);
        } else if(lightReading != null){
            mConnectedThread.write(lightReading);
        } else if (playMusic !=null){
            mConnectedThread.write(playMusic);
        }

        /*Intent i = new Intent();
        //i.putExtra("msgFromArduino", dataInPrint);
        i.setClass(BT_Com_Activity.this, MainActivity_MagicAlarm.class);
        i.putExtra("nombreUsr", nombreUsr);
        finish();
        startActivity(i);*/
    }


    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no ir recibiendo datos
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null ;
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    @SuppressLint("HandlerLeak")
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    @SuppressLint("HandlerLeak") String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);


                        /*
                        Intent i = new Intent();
                        //i.putExtra("msgFromArduino", dataInPrint);
                        i.setClass(BT_Com_Activity.this, MainActivity_MagicAlarm.class);
                        finish();
                        startActivity(i);
                        */
                        //txtPotenciometro.setText(dataInPrint);
                        System.out.println("MSG F+from Arduino: << " + dataInPrint + " >>"); /** DEBUG !! **/

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // Método run del hilo, que va a entrar en una espera activa para recibir los msgs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Hilo secundario se queda en espera de mensajes del HC05
            while (true)
            {
                try
                {
                    // Leer los datos del Bluetooth
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    System.out.println("Se recibió: " + readMessage); /** DEBUG !! **/
                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                    if(readMessage != null && !readMessage.isEmpty()) {
                        Intent i = new Intent();
                        i.setClass(BT_Com_Activity.this, MainActivity_MagicAlarm.class);
                        i.putExtra("mensajeAMostrar", readMessage);
                        i.putExtra("nombreUsr", nombreUsr);
                        finish();
                        startActivity(i);
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                //showToast("La conexion fallo");
                System.out.println("ERROR! La conexion falló."); /** DEBUG !! **/
                finish();

            }
        }
    }

}