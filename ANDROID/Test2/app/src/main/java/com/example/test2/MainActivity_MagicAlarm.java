package com.example.test2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
// UI Components
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity_MagicAlarm extends AppCompatActivity {

    private Button submitBtn, sensorBtn;
    private RadioGroup songList;
    private RadioButton selectedSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Estado de la Activity: <<onCreate>>"); /** DEBUG !! **/

        submitBtn = (Button) findViewById(R.id.buttonSubmit);
        sensorBtn = (Button) findViewById(R.id.buttonSensores);
        songList = (RadioGroup) findViewById(R.id.dropdownSongs); // obtener RadioGroup


        sensorBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Se hizo clic en el button <<SENSOR>>"); /** DEBUG !! **/
                Intent i = new Intent();
                i.setClass(MainActivity_MagicAlarm.this, Activity2_Sensors.class);
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

                switch(selectedId) { // Verificar qué radio button se activó
                    case R.id.song_HarryPotter:
                            System.out.println("Se hizo clic en la cancion <<song_HarryPotter>>"); /** DEBUG !! **/
                            // logica envio comando cancion 1
                        break;
                    case R.id.song_2:
                            System.out.println("Se hizo clic en la cancion <<song_2>>"); /** DEBUG !! **/
                            // logica envio comando cancion 2
                        break;
                    case R.id.song_3:
                            System.out.println("Se hizo clic en la cancion <<song_3>>"); /** DEBUG !! **/
                            // logica envio comando cancion 3
                        break;
                }
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