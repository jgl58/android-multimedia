package com.example.multimedia;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Locale;

public class Reproduccion extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;

    Button reproducir, pausa, detener, reproducirVideo, detenerVideo;
    VideoView superficie;
    TextView tiempo;
    int duracion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproduccion);

        mediaPlayer = MediaPlayer.create(this, Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3"));

        inicializarBotones();
    }

    private void inicializarBotones() {
        reproducir = findViewById(R.id.reproducir);
        pausa = findViewById(R.id.pausa);
        detener = findViewById(R.id.detener);

        reproducir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //  habilitar los botones Pausa y Detener, deshabilitar el botón Reproducir
                pausa.setEnabled(true);
                detener.setEnabled(true);
                reproducir.setEnabled(false);

                //  iniciar la reproducción del clip de audio
                mediaPlayer.start();

            }
        });

        pausa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // pausar o reanudar la reproducción del audio
                // cambiar el texto del botón a Reanudar o Pausa según corresponda
                if(mediaPlayer.isPlaying()){
                    pausa.setText("Reanudar");
                    mediaPlayer.pause();
                }else{
                    pausa.setText("Pausa");
                    mediaPlayer.start();
                }


            }
        });

        detener.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //  habilitar el botón Reproducir, deshabilitar los botones Pausa y Detener
                //  volver a poner como texto del botón de pausa la cadena Pausa
                reproducir.setEnabled(true);
                pausa.setEnabled(false);
                detener.setEnabled(false);
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                pausa.setText("Pausa");
            }
        });


        //CONTROLES VIDEO
        superficie = (VideoView)findViewById(R.id.superficie);
        tiempo = (TextView)findViewById(R.id.tiempo);
        reproducirVideo = (Button)findViewById(R.id.reproducirVideo);
        detenerVideo = (Button)findViewById(R.id.detenerVideo);
        tiempo.setText("Duración 0");


        superficie.setKeepScreenOn(true);
        superficie.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/" + R.raw.tetris));

        reproducirVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //  Habilitar y deshabilitar botones
                reproducirVideo.setEnabled(false);
                detenerVideo.setEnabled(true);
                //  Inicializar la reproducción de video
                superficie.start();

            }
        });

        // Manejador del evento OnPreparedListener para comenzar
        superficie.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                tiempo.setText("Duración "+secondsToTimeString(mp.getDuration()/1000));
            }
        });
        // la reproducción y poder mostrar la duración del video

        detenerVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Habilitar y deshabilitar botones

                // Detener la ejecución del video
                reproducirVideo.setEnabled(true);
                detenerVideo.setEnabled(false);

                tiempo.setText("Duración 0");
                superficie.pause();
            }
        });
    }

    public static String secondsToTimeString(int totalSeconds) {
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
