/*
 *
 *
 */
package sje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kezeli a központi eseménykezelő szálat. Lehetőséget biztosít {@link EventHandler} példányok számára az
 * {@link EventHandler#forwardEvents()} metódusuknak a központi eseménykezelő szálról történő meghívására.
 * <p/>
 * Az eseménykezelő példányok a {@link EventHandlingRunnable#instance()} singleton példányon keresztül érhetik el
 * ezt az osztályt, és a {@link EventHandlingRunnable#register(Events.EventHandler)} metódussal regisztrálhatják
 * magukat.<p/>
 * A {@link EventHandlingRunnable#wakeup()} metódus segítségével egy eseménykezelő jelezheti,
 * hogy kezelendő eseménye van, ennek következtében a legközelebbi adandó alkalommal
 * meghívásra fog kerülni a {@link EventHandler#forwardEvents()} metódusa az eseménykezelő szálról.
 * <p/>
 * A központi eseménykezelő szálat a {@link EventHandlingRunnable#stop()} metódussal lehet terminálni, ezt teszi
 * a publikusan elérhető {@link EventHandler#stopHandlerThread()} metódus.
 * <p/>
 * @author Megyesi Attila
 */
class EventDispatcher implements Runnable {

    private Thread _handlerThread = null;
    private boolean _isRunning = false;
    private final List<EventHandler> _handlersToRegister = Collections.synchronizedList(new ArrayList<EventHandler>());
    private final ArrayList<EventHandler> _handlers = new ArrayList<>();
    private static EventDispatcher _instance;

    public boolean isRunning() {
        return _isRunning;
    }

    private EventDispatcher() {
    }

    /** Visszaadja a singleton példányt. */
    public static synchronized EventDispatcher instance() {
        if (_instance == null) {
            _instance = new EventDispatcher();
        }
        return _instance;
    }

    /**
     * Regisztrálja a megadott eseménykezelőt a központi eseménykezelő szálon történő eseménytovábbításhoz.<p/>
     * Regisztrációt követően a {@link EventHandlingRunnable#wakeup()} metódus hatására a megadott
     * eseménykezelő {@link EventHandler#forwardEvents()} metódusa meghívásra fog kerülni a központi eseménykezelő
     * szálról.
     * <p/>
     * @param eh A regisztrálandó eseménykezelő példány.
     */
    public void register(EventHandler eh) {
        _handlersToRegister.add(eh);
        if (!_isRunning) {
            start();
        }
        _handlerThread.interrupt();
    }

    private synchronized void start() {
        if (_isRunning) {
            _handlerThread.interrupt();
            return;
        }
        _isRunning = true;
        _handlerThread = new Thread(this);
        _handlerThread.start();
    }

    /** Leállítja az eseménykezelő szálat. */
    public void stop() {
        if (_handlerThread == null) {
            return;
        }
        System.out.println("Stopping the event handler thread...");
        _isRunning = false;
        _handlers.clear();
        try {
            _handlerThread.join(300);
        } catch (InterruptedException ex) {
        }
        if (_handlerThread.isAlive()) {
            _handlerThread.interrupt();
        }
        try {
            _handlerThread.join(10);
        } catch (InterruptedException ex) {
        }
        _handlerThread = null;
        _handlersToRegister.clear();
        _handlers.clear();
        System.out.println("Event handler thread stopped.");
    }

    /** Folytatásra kényszeríti a központi eseménykezelő szálat. */
    public void wakeup() {
        _handlerThread.interrupt();
    }

    /**
     * A {@link Runnable} interfész által definiált metódus, a szál indításakor kerül meghívásra, kívülről nem kell
     * meghívni. A szál elindul a {@link EventHandlingRunnable#register(Events.EventHandler)} metódus első meghívásakor.
     */
    @Override
    public void run() {
        System.out.println("Event handler thread started.");
        while (_isRunning) {
            for (EventHandler eh : _handlers) {
                eh.forwardEvents();
                if (!_isRunning) {
                    return;
                }
            }
            if (!_handlersToRegister.isEmpty()) {
                synchronized (_handlersToRegister) {
                    //_handlers.addAll(_handlersToRegister);
                    for(EventHandler eh : _handlersToRegister){
                        if(!_handlers.contains(eh)){
                            _handlers.add(eh);
                        }
                    }
                    _handlersToRegister.clear();
                }
            }
            if (!_isRunning) {
                return;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                if (!_isRunning) {
                    return;
                }
            }
        }
    }
}
