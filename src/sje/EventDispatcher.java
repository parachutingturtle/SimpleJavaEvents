/* 
 * Copyright (C) 2016 parachutingturtle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the central event dispatcher thread.
 * <p>
 * Lets {@link EventHandler} instances get their {@link EventHandler#forwardEvents()} methods called from the central event dispatcher thread.
 * </p>
 * <p>
 * Event handler instances can access this class through the {@link EventHandlingRunnable#instance()} singleton instance, and can register themselves by calling the 
 * {@link EventHandlingRunnable#register(Events.EventHandler)} method.
 * </p>
 * <p>
 * An event handler can signal that it has unhandled events by calling the {@link EventHandlingRunnable#wakeup()} method, which will cause its {@link EventHandler#forwardEvents()}
 * method to be called from the dispatched thread as soon as possible.
 * </p>
 * <p>
 * The central event dispatcher thread can be stopped using the {@link EventHandlingRunnable#stop()} method,
 * which is done via the publicly accessible {@link EventHandler#stopHandlerThread()}.
 * </p>
 */
class EventDispatcher implements Runnable {

    private Thread _handlerThread = null;
    private boolean _isRunning = false;
    private final List<EventHandler> _handlersToRegister = Collections.synchronizedList(new ArrayList<EventHandler>());
    private final ArrayList<EventHandler> _handlers = new ArrayList<>();
    private static EventDispatcher _instance;

    /**
     * Private constructor for singleton access.
     */
    private EventDispatcher() {
    }

    /**
     * Returns the singleton instance.
     */
    public static synchronized EventDispatcher instance() {
        if (_instance == null) {
            _instance = new EventDispatcher();
        }
        return _instance;
    }

    /**
     * Registers the provided event handler on the central event dispatcher thread so that it can forward asynchronous events.
     * @param eh The event handler instance to register.
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

    /**
     * Stops the event dispatcher thread.
     */
    public void stop() {
        if (_handlerThread == null) {
            return;
        }
        System.out.println("Stopping the event handler thread...");
        _isRunning = false;
        _handlerThread.interrupt();
    }

    /**
     * Forces the event dispatcher thread to continue.
     */
    public void wakeup() {
        _handlerThread.interrupt();
    }

    /**
     * Method defined by the {@link Runnable} interface, called at thread start, no need to call it from outside this class.
     * The thread starts at the first call of the {@link EventHandlingRunnable#register(Events.EventHandler)} method.
     */
    @Override
    public void run() {
        System.out.println("Event handler thread started.");
        while (_isRunning) {
            for (EventHandler eh : _handlers) { //The reason for this archictecture is that no locking is required here; could just queue event firings instead, but that would mean locking and it could cause event firing threads to wait until previous events have been handled.
                eh.forwardEvents();
                if (!_isRunning) {
                    break;
                }
            }
            if (!_handlersToRegister.isEmpty()) {
                synchronized (_handlersToRegister) {
                    for (EventHandler eh : _handlersToRegister) {
                        if (!_handlers.contains(eh)) {
                            _handlers.add(eh);
                        }
                    }
                    _handlersToRegister.clear();
                }
            }
            if (!_isRunning) {
                break;
            }
            try {
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                if (!_isRunning) {
                    break;
                }
            }
        }
        System.out.println("Event handler thread stopped.");
    }
}
