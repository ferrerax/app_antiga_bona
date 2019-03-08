package com.example.quimfersa.appdelfoc;

import android.app.Dialog;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
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
    private static final int YNR = 2;
    private static final int RES = 1;


    private static Socket s;
    private static PrintWriter pw;
    private TextView comando; //es el textview que ha de servir per enviar text a la raspi.
    private String resultat;  //variable que reb el resultat de veu. es la que usarà el sendtext per a enviar.
    private Boolean parla = false;  //diu si parlarà o no parlarà
    private String IP = "192.168.0.148";
    private int PORT = 5500;
    private TextView response;
    private TextToSpeech textToSpeech;
    private String resposta_server;
    private String si_no;
    private String rebut;

    private int id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        switch (intent.getIntExtra("id", 0)) {
            case 0:
                IP = "192.168.0.148";
                setContentView(R.layout.activity_main);
                break;
            case 1:
                IP = "quimfersa.ddns.net";
                setContentView(R.layout.activity_screen2);
                break;

            case 2:
                IP = "Messi es rencarnació de Trotsky";
                setContentView(R.layout.activity_auxiliar);
                break;

        }
//        File file = new File(String.valueOf(id));
//        if(file.exists()) {
//            try {
//
//                FileInputStream fileInputStream = new FileInputStream (String.valueOf(id));
//                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//                comando = (TextView) findViewById(R.id.command);
//                response = (TextView) findViewById(R.id.response);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                String aux;
//                while ( (aux = bufferedReader.readLine()) != null )
//                {
//                    IP = aux;
//                    PORT = Integer.valueOf(bufferedReader.readLine());
//
//                }
//                fileInputStream.close();
//
//                bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//
//            }
//        }

        EditText ip = (EditText) findViewById(R.id.ip);
        ip.setText(IP);
        EditText port = (EditText) findViewById(R.id.port);
        port.setText(String.valueOf(PORT));
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //vaina necessaria perk la app parli
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                talk();

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mes_amb_rodoneta:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.casa:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                id = 0;
                Intent casa = new Intent(this, MainActivity.class);
                casa.putExtra("id", id);
                startActivity(casa);
                return true;
            case R.id.fora1:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                id = 1;
                Intent screen2 = new Intent(this, MainActivity.class);
                screen2.putExtra("id", id);
                startActivity(screen2);
                return true;
            case R.id.fora2:
                id = 2;
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent screen3 = new Intent(this, MainActivity.class);
                screen3.putExtra("id", id);
                startActivity(screen3);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void talk() {
        //String toTalk = response.getText().toString();
        String version = Build.VERSION.RELEASE;
        textToSpeech.speak("hola".subSequence(0, "hola".length()), TextToSpeech.QUEUE_FLUSH, null, "");
    }

    public void setIPPORT(View view) {
        Toast.makeText(this, "Valors de IP i PORT actualitzats correctament.", Toast.LENGTH_LONG).show();
        EditText ipdec = (EditText) findViewById(R.id.ip);
        EditText portdec = (EditText) findViewById(R.id.port);
        IP = ipdec.getText().toString();
        PORT = Integer.parseInt(portdec.getText().toString());
        //save();

    }

    public void save() {
        EditText find_ip = (EditText) findViewById(R.id.ip);
        find_ip.setText(String.valueOf(IP));
        FileOutputStream fos = null;
        try {

            File file = new File(String.valueOf(id));
            if(file.exists()){
                try (FileChannel chinchan = new FileOutputStream(file,true).getChannel()){
                    chinchan.truncate(0);
                }

            }
            else {
                fos = openFileOutput(String.valueOf(id), MODE_PRIVATE);
            }
            fos.write(IP.getBytes());
            fos.write("\n".getBytes());
            fos.write(String.valueOf(PORT).getBytes());
            fos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    public void xerra(View view) {  //si xerra o no xerra
        parla = !parla;
        Button parlo = (Button) findViewById(R.id.xerra);
        if (parla) {
            parlo.setBackgroundColor(Color.parseColor("#61d800"));
            Toast.makeText(this, "Ara parla", Toast.LENGTH_SHORT).show();
        } else {
            parlo.setBackgroundColor(Color.parseColor("#d90e0e"));

            Toast.makeText(this, "No parla", Toast.LENGTH_SHORT).show();
        }
    }

    public void comando(View view) { //envia el comando escrit
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
            s = new Socket("192.168.0.177", 5500); //connect to server at port port
            //s.setSoTimeout(300000000);
            //s.setKeepAlive(true);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(resultat);

            pw.flush();  //send the missatge
            Toast.makeText(this, "Missatge enviat", Toast.LENGTH_LONG).show();
            InputStream is = s.getInputStream();

            InputStreamReader isr = new InputStreamReader(is);

            BufferedReader in = new BufferedReader(isr);

            rebut = "";
            String res;
            while ((res = in.readLine()) != null) {
                rebut += res + "\n";
            }
            s.close();
//            //response.setText(rebut);
//            char[] aux = rebut.toCharArray();
//            if (aux[0] == '2'){
//                resposta_server = rebut.substring(1);
//               popuptxt(resposta_server);
//
//            } else if (aux[0] == '1'){
//                resposta_server = rebut.substring(1);
//                response.setText(resposta_server);
//
//            }
//
//
//            if(parla == true)this.talk();
//            s.close();


        } catch (IOException e) {
            Log.e("myTask", "S: Error", e);
        }
    }


    public void pregunta() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(resposta_server);
        alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Has dit que sí", Toast.LENGTH_SHORT).show();
                si_no = "Y";
                rebut = si_no;
                //sendText();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Has dit que no", Toast.LENGTH_SHORT).show();
                si_no = "N";
                rebut = si_no;
                //sendText();
            }
        });

        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    resultat = result.get(0);
                    resultat = TXT + resultat;

                    sendText();
                    //response.setText(rebut);
                    char[] aux = rebut.toCharArray();
                    if (aux[0] == '2') {
                        resposta_server = rebut.substring(1);
                        response.setText(rebut);
                        pregunta();
                        resultat = si_no;

                    } else if (aux[0] == '1') {
                        resposta_server = rebut.substring(1);
                        response.setText(resposta_server);

                    }


                    if (parla == true) this.talk();
                    //s.close();

                }

                break;
        }
    }
}
