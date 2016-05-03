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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Handler class for {@link Event Event}s with event arguments of type T.
 * <p>
 * It is recommended to extend this class with an event-specific handler as a nested class within the class that serves as the "observer".
 * Handle events by overriding the {@link EventHandler#handleEvent(java.lang.Object, java.lang.Object) } method.
 * </p>
 * 
 * @param <T> The type of the event arguments used for the event to handle.
 * @see Event
 */
public abstract class EventHandler<T> {
    private final List<EventDischarge<T>> _queue = Collections.synchronizedList(new LinkedList<EventDischarge<T>>());
    private final static EventDispatcher _dispatcher = EventDispatcher.instance();
    private boolean _isRegistered = false;
    private EventDischarge<T> _evt = null;

    /**
     * Stops the event handler thread that is used for asynchronous event forwarding.
     */
    public static void stopHandlerThread() {
        _dispatcher.stop();
    }

    /**
     * Adds a fired event to the queue of events to dispatch asynchronously. The event will be handled on the event dispatcher thread as soon as possible.
     * @param sender The object firing the event
     * @param args The arguments for the event
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
     * Called from the event dispatcher thread, forwards all unhandled events via their {@link EventHandler#handleEvent(java.lang.Object, java.lang.Object)  handleEvent} methods.
     */
    protected void forwardEvents() {
        while (!_queue.isEmpty()) {
            _evt = _queue.get(0);
            _queue.remove(0);
            //Even though it is possible that the collection will be modified on another thread between these get and remove calls,
            //such modification can only be an addition, therefore the first element will not be accessed.
            //This is thread safe because we only operate on the first element here, and removing from the collection only happens on this thread.
            //Furthermore, the _queue is a Collections.synchronizedList, which guarantees the atomic execution of individual collection operations,
            //everything other than iteration, and we don't perform iteration here.
            handleEvent(_evt.getSender(), _evt.getParameter());
            _evt = null;
        }
    }

    /**
     * Override this method to handle events with event arguments of type T.
     * @param sender The object firing the event
     * @param e The arguments for the event
     */
    protected abstract void handleEvent(Object sender, T e);
}
