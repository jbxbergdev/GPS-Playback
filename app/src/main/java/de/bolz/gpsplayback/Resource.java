package de.bolz.gpsplayback;

/**
 * Inspired from https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample
 * @param <Data>
 * @param <Error>
 */
public class Resource<Data, Error> {

    public final Status status;
    public final Data data;
    public final Error error;

    public Resource(Status status, Data data, Error error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <Data, Error> Resource<Data,Error> success(Data data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <Data, Error> Resource<Data, Error> error(Data data, Error error) {
        return new Resource<>(Status.ERROR, data, error);
    }

    public static <Data, Error> Resource<Data, Error> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public enum Status {
        LOADING, SUCCESS, ERROR
    }
}
