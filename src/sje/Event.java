/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Egy olyan eseményt reprezentál, amelyen keresztül a "megfigyelt" osztály üzenhet az eseményre feliratkozott
 * objektumoknak ("megfigyelő" osztályok). Az átadott üzenet T típusú lehet.
 * <p/>
 * A megfigyelők a {@link #subscribe(Events.EventHandler)} metódus segítségével iratkozhatnak fel az eseményre.
 * <p/>
 * Az esemény "tüzelése" a megfigyelt osztályból:
 * <p>Aszinkron módon: ebben az esetben a lekezelés külön szálon fog történni megfigyelőnként:<br/>
 * {@link Events.Event#fire(java.lang.Object)} or<br/>
 * {@link Events.Event#fire(java.lang.Object, java.lang.Object) }.</p>
 * <p/>
 * <p>Szinkron módon: ilyenkor azon a szálon fut majd a lekezelés, ahonnan az esemény el lett indítva:<br/>
 * {@link Events.Event#fireSync(java.lang.Object)} or<br/>
 * {@link Events.Event#fireSync(java.lang.Object, java.lang.Object) }.</p>
 * <p/>
 * @see Events.EventHandler
 * @see Events.EventArgs
 * @author Megyesi Attila
 */
public final class Event<T> {

    private final List<EventHandler<T>> _handlers = Collections.synchronizedList(new ArrayList<EventHandler<T>>());

    /**
     * Létrehoz egy esemény objektumot, amelyre fel lehet iratkozni {@link EventHandler} eseménykezelőkkel.
     */
    public Event() {
    }

    /**
     * Feliratkoztat egy új eseménykezelőt, amely értesítve lesz az esemény bekövetkeztekor.
     * <p/>
     * @param handler Az {@link EventHandler}, amely kezelni fogja az eseményt.
     */
    public synchronized void subscribe(EventHandler<T> handler) {
        if (handler != null && !_handlers.contains(handler)) {
            _handlers.add(handler);
        }
    }

    /**
     * Eltávolítja a megadott eseménykezelőt a kezelők listájából, így az nem lesz értesítve a későbbi eseményhívásokról.
     * <p/>
     * @param handler Az eltávolítandó {@link EventHandler}.
     */
    public synchronized void unsubscribe(EventHandler<T> handler) {
        _handlers.remove(handler);
    }

    /**
     * Eltüzeli az eseményt aszinkron módon, hozzáadva azt az összes feliratkozott {@link Events.EventHandler}
     * feladatlistájához.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     * @param args Az eseményhez tartozó adatok
     */
    public void fireAsync(Object sender, T args) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.addToQueue(sender, args);
            }
        }
    }

    /**
     * Eltüzeli az eseményt aszinkron módon, adatok nélkül, hozzáadva azt az összes feliratkozott
     * {@link Events.EventHandler} feladatlistájához.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     */
    public void fireAsync(Object sender) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.addToQueue(sender, null);
            }
        }
    }

    /**
     * Eltüzeli az eseményt szinkron módon, meghívva az összes feliratkozott {@link Events.EventHandler}
     * kezelő metódusát ezen a szálon.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     * @param args Az eseményhez tartozó adatok
     */
    public void fireSync(Object sender, T args) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.handleEvent(sender, args);
            }
        }
    }

    /**
     * Eltüzeli az eseményt szinkron módon, adatok nélkül, meghívva az összes feliratkozott {@link Events.EventHandler}
     * kezelő metódusát ezen a szálon.
     * <p/>
     * @param sender Az eseményt kiváltó objektum
     * @param args Az eseményhez tartozó adatok
     */
    public void fireSync(Object sender) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.handleEvent(sender, null);
            }
        }
    }
}

