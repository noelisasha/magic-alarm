package com.example.magicalarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.*;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
//import android.support.v7.app.CompatActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
// UI Components
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity_MagicAlarm extends AppCompatActivity {

    private Button submitBtn, sensorBtn;
    private RadioGroup songList;
    private RadioButton selectedSong;
    private TextView bienvenidaUsr;

    //private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mDevice; // Una variable ya que al desear conectar solo con 1 dispositivo dado, no nos interesa tener lista de todos los dispositivos con Bluetooth disponibles.

    private BluetoothAdapter mBluetoothAdapter;

    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    private String nombreUsr;

    //se crea un array de String con los permisos a solicitar en tiempo de ejecucion
    //Esto se debe realizar a partir de Android 6.0, ya que con verdiones anteriores
    //con solo solicitarlos en el Manifest es suficiente
    String[] permissions = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    /*****conexion de bluetooth*************************************************/

    private void ini_bluetooth() {

        //Se crea un adaptador para poder manejar el bluethoot del celular
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        System.out.println("checkiando permisos.... (lineas 87)"); /** DEBUG !! **/
        if (checkPermissions()) {
            System.out.println("permisos checkiados (lineas 87)"); /** DEBUG !! **/
            enableComponent();
        } else {
            System.out.println("permisos fallaron!"); /** DEBUG !! **/
            //ver despues si incluir este if en onresume
        }

    }

    protected void searchDevices() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices == null || pairedDevices.size() == 0)
        {
            System.out.println("No devices found"); /** DEBUG !! **/
        }
        else
        {
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

            list.addAll(pairedDevices);

            System.out.println("list: " + list); /** DEBUG !! **/
            for(BluetoothDevice device :list){
                if (device.getAddress().equals("00:21:09:00:01:B1")) {
                    System.out.println("Se encontró el arduino!"); /** DEBUG !! **/
                    mDevice = device;

                    return;
                }

            }
            System.out.println("No se encontró el arduino! :("); /** DEBUG !! **/
        }
    }

    protected void enableComponent() {
        //se determina si existe bluethoot en el celular
        if (mBluetoothAdapter == null) {
            //si el celular no soporta bluethoot
            //showUnsupported();
        } else {
            //se determina si esta activado el bluethoot
            if (mBluetoothAdapter.isEnabled()) {
                //se informa si esta habilitado
                System.out.println("Bluetooth Enabled (lineas 102)"); /** DEBUG !! **/
                //showEnabled();
                searchDevices();

            } else {
                //se informa si esta deshabilitado
                //showDisabled();
            }
        }

        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Activado /Desactivado)
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Se encuentra un dispositivo bluetooth al realizar una busqueda
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Cuando se comienza una busqueda de bluethoot
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //cuando la busqueda de bluethoot finaliza

        //se define (registra) el handler que captura los broadcast anteriormente mencionados.
        registerReceiver(mReceiver, filter);
    }

    //Metodo que chequea si estan habilitados los permisos
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    enableComponent(); // Now you call here what ever you want :)
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    showToast("ATENCION: La aplicacion no funcionara correctamente debido a la falta de Permisos");
                }
                return;
            }
        }
    }

    //Handler que captura los brodacast que emite el SO al ocurrir los eventos del bluethoot
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();
            System.out.println("este es el action " + action); /** DEBUG !! **/
            //Si cambio de estado el Bluethoot(Activado/desactivado)
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                //Obtengo el parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                //Si esta activado
                if (state == BluetoothAdapter.STATE_ON) {
                    // showToast("Activar");

                    //showEnabled();
                }
            }
            //Si se inicio la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //Creo la lista donde voy a mostrar los dispositivos encontrados
                //mDeviceList = new ArrayList<BluetoothDevice>();

                //muestro el cuadro de dialogo de busqueda
                //mProgressDlg.show();
            }
            //Si finalizo la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //se cierra el cuadro de dialogo de busqueda
                //mProgressDlg.dismiss();

                //se inicia el activity DeviceListActivity pasandole como parametros, por intent,
                //el listado de dispositovos encontrados
                // Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                //newIntent.putParcelableArrayListExtra("device.list", mDeviceList);

                //startActivity(newIntent);
            }
            //si se encontro un dispositivo bluethoot
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Se lo agregan sus datos a una lista de dispositivos encontrados
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("bluetooth encontrado!"); /** DEBUG !! **/


            }
        }
    };

    /**************************************************************************************************/

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Estado de la Activity: <<onCreate>>"); /** DEBUG !! **/




        ini_bluetooth();

        submitBtn = (Button) findViewById(R.id.buttonSubmit);
        sensorBtn = (Button) findViewById(R.id.buttonSensores);
        songList = (RadioGroup) findViewById(R.id.dropdownSongs); // obtener RadioGroup
        bienvenidaUsr = (TextView) findViewById(R.id.textView7);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        nombreUsr = extras.getString("nombreUsr");
        bienvenidaUsr.setText("¡Hola, " + nombreUsr + "!");

        sensorBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Se hizo clic en el button <<SENSOR>>"); /** DEBUG !! **/
                Intent i = new Intent();
                //i.putExtra("nombreUsr", nombreUsr);
                i.setClass(MainActivity_MagicAlarm.this, Activity2_Sensors.class);
                finish();
                startActivity(i);

            }
        });

        selectedSong = (RadioButton)songList.findViewById(songList.getCheckedRadioButtonId()); //Obtener RadioButton seleccionado
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = songList.getCheckedRadioButtonId();
                //we are defining the selectId and then we are fetching the id of the checked radio button using the function getCheckedRadioButton()
                selectedSong = findViewById(selectedId);

                Intent i = new Intent();
                switch(selectedId) { // Verificar qué radio button se activó
                    case R.id.song_HarryPotter:
                        System.out.println("Se hizo clic en la cancion <<song_HarryPotter>>"); /** DEBUG !! **/
                        i.putExtra("chosenSong", "H");
                        break;
                    case R.id.song_2:
                        System.out.println("Se hizo clic en la cancion <<song_2>>"); /** DEBUG !! **/
                        i.putExtra("chosenSong", "T");
                        break;
                    default:
                        showToast("AHHH");
                        System.out.println("ERROR! Se debe seleccionar una cancion. Vuelve a Harry Potter"); /** DEBUG !! **/
                        i.putExtra("chosenSong", "H");
                        break;
                }
                i.putExtra("arduinoDevice", mDevice);
                i.putExtra("nombreUsr", nombreUsr);
                i.setClass(MainActivity_MagicAlarm.this, BT_Com_Activity.class);
                finish();
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        System.out.println("Estado de la Activity: <<onStart>>"); /** DEBUG !! **/
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Estado de la Activity: <<onResume>>"); /** DEBUG !! **/
    }

    @Override
    protected void onStop() {
        System.out.println("Estado de la Activity: <<onStop>>"); /** DEBUG !! **/
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("Estado de la Activity: <<onDestroy>>"); /** DEBUG !! **/
        super.onDestroy();
    }


}