package com.example.quimfersa.appdelfoc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Locale;




public class MainActivity extends AppCompatActivity {
    //Constants
    //Gumersindo
    private static final int NEW = 4;
    private static final int ADD = 1;
    private static final int TAG = 2;
    private static final int CMD = 3;
    private static final int TXT = 0;
    private static final int CNL = 5;

    private static final int ERR = 0;
    private static final int YNR = 1;
    private static final int RES = 2;


    private TextView comando; //es el textview que ha de servir per enviar text a la raspi.
    private String resultat;  //variable que reb el resultat de veu. es la que usarà el sendtext per a enviar.
    private Boolean parla = false;  //diu si parlarà o no parlarà
    private String IP = "192.168.0.148";
    private int PORT = 5500;
    private TextView response;
    private static Socket s;
    private TextToSpeech textToSpeech;
    private static  PrintWriter pw;
    private String resposta_server = "BIBA VOX";
    private String si_no;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayip(IP);
        displayport(PORT);
        comando = (TextView) findViewById(R.id.command);
        response = (TextView) findViewById(R.id.response);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        //vaina necessaria perk la app parli
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                    talk();

            }
        });
    }

    public void talk(){
        String toTalk = response.getText().toString();
        String version = Build.VERSION.RELEASE;
        textToSpeech.speak(toTalk.subSequence(0,toTalk.length()), TextToSpeech.QUEUE_FLUSH, null, "");
    }
    public void setIPPORT (View view) {
        response.setText("Valors de IP i PORT actualitzats correctament.");
        EditText ipdec = (EditText) findViewById(R.id.ip);
        EditText portdec = (EditText) findViewById(R.id.port);
        IP = ipdec.getText().toString();
        PORT = Integer.parseInt(portdec.getText().toString());


    }

    public void displayip(String ip){
        EditText find_ip = (EditText) findViewById(R.id.ip);
        find_ip.setText(String.valueOf(ip));

    }

    public void displayport(int port){
        EditText find_port = (EditText) findViewById(R.id.port);
        find_port.setText(String.valueOf(port));

    }

    public void xerra (View view){  //si xerra o no xerra
        parla = !parla;
        Button parlo = (Button) findViewById(R.id.xerra);
        if (parla){
            parlo.setBackgroundColor(Color.parseColor("#61d800"));
            Toast.makeText(this, "Ara parla", Toast.LENGTH_SHORT).show();
        }
        else{
            parlo.setBackgroundColor(Color.parseColor("#d90e0e"));

            Toast.makeText(this, "No parla", Toast.LENGTH_SHORT).show();
        }
    }

    public void comando (View view){ //envia el comando escrit
        resultat = comando.getText().toString();
        resultat = TXT + resultat;
        sendText();

    }

    public void getSpeechInput(View view) {
        setIPPORT(view);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "No li agrada l'input", Toast.LENGTH_LONG).show();
        }

    }

    public void sendText() {


        try {
            s = new Socket(IP, PORT); //connect to server at port port
            s.setSoTimeout(30000);
            s.setKeepAlive(true);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(resultat);

            pw.flush();  //send the missatge
            Toast.makeText(this, "Missatge enviat" , Toast.LENGTH_LONG).show();
            InputStream is = s.getInputStream();

            InputStreamReader isr =  new InputStreamReader(is);

            BufferedReader in = new BufferedReader(isr);

            String rebut = "";
            String res;
            while((res = in.readLine()) != null){
                rebut += res + "\n";
            }
            if (rebut.charAt(0) == '1'){
                resposta_server = rebut.substring(1);
                pregunta();
                resultat = si_no;
                sendText();

            } else if (rebut.charAt(0) == '2'){
                resposta_server = rebut.substring(1);
                response.setText(resposta_server);
            }


            if(parla == true)this.talk();
            s.close();


        } catch (IOException e) {
            Log.e("myTask", "S: Error", e);
        }
    }



public void pregunta (){
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setTitle(resposta_server);
    alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getApplicationContext(),"Has dit que sí", Toast.LENGTH_SHORT).show();
            si_no = "Y";

        }
    });

    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getApplicationContext(),"Has dit que no", Toast.LENGTH_SHORT).show();
            si_no = "N";
        }
    });

    alertDialog.show();
}



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    resultat = result.get(0);
                    resultat = TXT + resultat;

                    sendText();
                }

                break;
        }
    }
}
