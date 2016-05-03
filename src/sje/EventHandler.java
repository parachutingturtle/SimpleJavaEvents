package sje;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * T típusú eseményargumentummal rendelkező {@link Event Esemény}ek kezelésére szolgáló osztály.
 * Érdemes a megfigyelő osztályban belső osztályként leszármaztatni belőle.
 * A {@link EventHandler#handleEvent(java.lang.Object, java.lang.Object)} metódus felüldefiniálásával lehet az
 * eseményeket kezelni.
 * <p/>
 * @see Events.Event
 * @author Megyesi Attila
 */
public abstract class EventHandler<T> {
    private final List<EventDischarge<T>> _queue = Collections.synchronizedList(new LinkedList<EventDischarge<T>>());
    private final static EventDispatcher _dispatcher = EventDispatcher.instance();
    private boolean _isRegistered = false;
    private EventDischarge<T> _evt = null;

    /**
     * Leállítja az eseménykezelő szálat.
     * <p/>
     * @see {@link EventHandlingRunnable#stop() }
     */
    public static void stopHandlerThread() {
        _dispatcher.stop();
    }

    /**
     * Hozzáadja az eseményt a várakozósorba. Az esemény az eseménykezelő szálon lesz kezelve, a legközelebbi
     * adandó alkalommal.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     * @param args Az eseményhez tartozó adatok
     */
    public final void addToQueue(Object sender, T args) {
        if (!_isRegistered) {
            _dispatcher.register(this);
            _isRegistered = true;
        }
        _queue.add(new EventDischarge<>(sender, args));
        _dispatcher.wakeup();
    }

    /**
     * Az eseménykezelő szálról kerül meghívásra, továbbítja az összes felgyülemlett eseményt
     * a {@link EventHandler#handleEvent(java.lang.Object, java.lang.Object) handleEvent} metódus meghívásával.
     */
    protected void forwardEvents() {
        while (!_queue.isEmpty()) {
            _evt = _queue.get(0);
            _queue.remove(0/* _evt */);
            //a get és a remove között ugyan bekövetkezhet egy másik szálon add, de az a nulladik indexet
            //nem módosíthatja.
            //Ez azért szálbiztonságos, mert csak a nulladik elemen dolgozik, és remove csak ezen a szálon van,
            //illetve mert a _queue egy Collections.synchronizedList, amely garantálja az egyedi listaműveletek
            //atomi végrehajtását, tehát csak az iterációét nem, az pedig itt nem történik.
            handleEvent(_evt.getSender(), _evt.getParameter());
            _evt = null;
        }
    }

    /**
     * Felüldefiniálandó a T típusú eseményargumentummal rendelkező események kezelésére.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     * @param e Az eseményargumentum (az esemény bekövetkezéséhez tartozó információt tartalmazó objektum), T típusú.
     */
    protected abstract void handleEvent(Object sender, T e);
}
