package com.example.magicalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity_NombreUsuario extends AppCompatActivity {

    private Button submitBtn;
    private EditText nombreTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nombre_usuario);

        submitBtn = (Button) findViewById(R.id.buttonSubmit);
        nombreTxt = (EditText) findViewById(R.id.nombreUsuario);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Se hizo clic en el button <<SUBMIT>>"); /** DEBUG !! **/

                String nombreUsr = new String();
                if(TextUtils.isEmpty(nombreTxt.getText().toString())) {
                    nombreUsr = "Desconocid@";
                }
                else {
                    nombreUsr = nombreTxt.getText().toString();
                }

                Intent i = new Intent();
                i.putExtra("nombreUsr", nombreUsr);
                i.setClass(MainActivity_NombreUsuario.this, MainActivity_MagicAlarm.class);
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