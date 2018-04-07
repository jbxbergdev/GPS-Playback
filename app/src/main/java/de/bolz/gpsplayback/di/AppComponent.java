package de.bolz.gpsplayback.di;

import javax.inject.Singleton;

import dagger.Component;
import de.bolz.gpsplayback.App;
import de.bolz.gpsplayback.playback.PlaybackService;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(App app);

    void inject(PlaybackService playbackService);

}
