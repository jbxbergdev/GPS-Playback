package de.bolz.gpsplayback;

import android.app.Application;

import de.bolz.gpsplayback.di.AppComponent;
import de.bolz.gpsplayback.di.AppModule;
import de.bolz.gpsplayback.di.DaggerAppComponent;

public class App extends Application {

    private static App instance;

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        appComponent.inject(this);
    }

    public static App getAppInstance() {
        return instance;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
