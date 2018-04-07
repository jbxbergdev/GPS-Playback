package de.bolz.gpsplayback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import de.bolz.gpsplayback.playback.PlaybackService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.<Button>findViewById(R.id.btn_start).setOnClickListener(v -> PlaybackService.start(this));
        this.<Button>findViewById(R.id.btn_stop).setOnClickListener(v -> PlaybackService.stop(this));
    }
}
