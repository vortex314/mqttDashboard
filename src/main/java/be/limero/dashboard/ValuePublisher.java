package be.limero.dashboard;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static be.limero.dashboard.Util.now;

public class ValuePublisher<T> implements Publisher<T> {
    Vector<Subscriber<? super T>> subscribers = new Vector<>();
    T value;
    Timer timer = new Timer(true);

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        if (subscriber == null) return;
        subscribers.add(subscriber);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                emit(value);
            }
        }, 1000, 1000);
    }

    void emit(T value) {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onNext(value);
        }
    }

    void set(T value) {
        this.value = value;
        emit(this.value);
    }

    T get() {
        return value;
    }
}
