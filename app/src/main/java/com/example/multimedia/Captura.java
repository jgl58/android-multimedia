package com.example.multimedia;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Captura extends AppCompatActivity implements SurfaceHolder.Callback  {

    private static final int REQUEST_CODE = 1;
    private static int TTS_DATA_CHECK = 1;
    private TextToSpeech tts = null;
    private boolean ttsIsInit = false;
    private RadioButton radioEnglish, radioSpanish;
    private TextView texto;
    private Button leer;
    SurfaceView superficie;
    Button parar, grabar;
    SurfaceHolder m_holder;

    boolean preparado = false;

    // añadimos un objeto privado MediaRecorder
    private MediaRecorder mediaRecorder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captura);
        inicializarInterfaz();
        inicializarControles();
        initTextToSpeech();

        requestPermissions();
        // inicializamos el objeto mediaRecorder
        mediaRecorder = new MediaRecorder();

        // obtenemos el holder de la superficie y añadimos el manejador
        m_holder = superficie.getHolder();
        m_holder.addCallback(this);
        m_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Button boton = (Button)this.findViewById(R.id.boton);
        boton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla");

                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> resultados =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);

            // Utilizar los resultados obtenidos
            TextView texto = (TextView)this.findViewById(R.id.texto);
            texto.setText(resultados.get(0));
        }
        if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                    tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                ttsIsInit = true;
                                Locale loc = new Locale("es","","");
                                if (tts.isLanguageAvailable(loc)
                                        >= TextToSpeech.LANG_AVAILABLE)
                                    tts.setLanguage(loc);
                                tts.setPitch(0.8f);
                                tts.setSpeechRate(1.1f);
                            }
                        }
                    });
                }
            } else {
                Intent installVoice = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installVoice);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    public void requestPermissions(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED &&
                    getApplicationContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                            PackageManager.PERMISSION_GRANTED &&
                    getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED
            ){
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, 29);
            }
        }else{
            Log.d("Debug","Already granted");
        }
    }

    private void inicializarInterfaz() {
        superficie = (SurfaceView)findViewById(R.id.superficie);
        parar = (Button)findViewById(R.id.parar);
        grabar = (Button)findViewById(R.id.grabar);

        grabar.setOnClickListener(new ManejadorBotonGrabar());
        parar.setOnClickListener(new ManejadorBotonParar());
    }

    private class ManejadorBotonParar implements OnClickListener {
        public void onClick(View v) {
            parar.setEnabled(false);
            grabar.setEnabled(true);

            // detener la grabación
            mediaRecorder.stop();
            mediaRecorder.reset();
        }
    };

    private class ManejadorBotonGrabar implements OnClickListener {
        public void onClick(View v) {

            if (preparado) {
                parar.setEnabled(true);
                grabar.setEnabled(false);

                // iniciar la grabación
                configurar(m_holder);
                mediaRecorder.start();

            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mediaRecorder.setPreviewDisplay(m_holder.getSurface());
        preparado = true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void configurar(SurfaceHolder holder) {
        // configurar mediaRecorder

        if (mediaRecorder != null) {
            try {

                // Inicializando el objeto MediaRecorder
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mediaRecorder.setOrientationHint(90);
                }

                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                }

                File file = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "video.mp4");
                }
                mediaRecorder.setOutputFile(file.getPath());

                mediaRecorder.prepare();

            } catch (IllegalArgumentException e) {
                Log.d("MEDIA_PLAYER", e.getMessage());
            } catch (IllegalStateException e) {
                Log.d("MEDIA_PLAYER", e.getMessage());
            } catch (IOException e) {
                Log.d("MEDIA_PLAYER", e.getMessage());
            }
        }
    }
    private void inicializarControles() {
        radioEnglish = (RadioButton)findViewById(R.id.radioEnglish);
        radioSpanish = (RadioButton)findViewById(R.id.radioSpanish);
        texto = (TextView)findViewById(R.id.textoSintesis);
        leer = (Button)findViewById(R.id.butRead);
        radioSpanish.setChecked(true);

        leer.setOnClickListener(butReadListener);
        radioSpanish.setOnClickListener(radioSpanishListener);
        radioEnglish.setOnClickListener(radioEnglishListener);
    }

    private OnClickListener butReadListener = new OnClickListener() {
        public void onClick(View v) {
            speak(texto.getText().toString());
        }
    };

    private OnClickListener radioSpanishListener = new OnClickListener() {

        public void onClick(View v) {
            radioSpanish.setChecked(true);
            radioEnglish.setChecked(false);

            // cambiar el idioma a español
            Locale loc = new Locale("es","","");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                if (tts.isLanguageAvailable(loc)
                        >= TextToSpeech.LANG_AVAILABLE)
                    tts.setLanguage(loc);
            }
        }

    };

    private OnClickListener radioEnglishListener = new OnClickListener() {

        public void onClick(View v) {
            radioSpanish.setChecked(false);
            radioEnglish.setChecked(true);

            // cambiar el idioma a inglés
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                tts.setLanguage(Locale.UK);
            }

        }

    };

    private void initTextToSpeech() {
        // inicializar motor text to speech
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_DATA_CHECK);

    }


    private void speak(String texto) {
        // invocar al método speak del objeto TextToSpeech para leer el texto del EditText
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT && tts!=null) {

            tts.speak(texto, TextToSpeech.QUEUE_ADD, null);
        }

    }

    @Override
    public void onDestroy() {
        //liberar los recursos de Text to Speech
        if (tts != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                tts.stop();
                tts.shutdown();
            }
        }
        super.onDestroy();
    }

    }
