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
 * Represents an event through which an observable class can send messages to all subscribed observers, using a generic parameter of type T.
 * <p>
 * The observers can subscribe to the event using the {@link #subscribe(EventHandler)} method.
 * </p>
 * 
 * <p>
 * Firing the event from the observed class can be done in two ways:
 * </p>
 * <p>
 * Synchronously: in this case the handling of the event will run on the same thread on which the event was fired from: 
 * </p>
 * <p>
 * {@link Event#fireSync(java.lang.Object) }
 * {@link Event#fireSync(java.lang.Object, java.lang.Object)}
 * </p>
 * <p>
 * Asynchronously: in this case the handling of the event will run on the central event dispatcher thread: 
 * </p>
 * <p>
 * {@link Event#fireAsync(java.lang.Object) }
 * {@link Event#fireAsync(java.lang.Object, java.lang.Object) }
 * </p>
 * 
 * @param <T> The type of the event arguments used for the event to handle.
 */
public final class Event<T> {

    private final List<EventHandler<T>> _handlers = Collections.synchronizedList(new ArrayList<EventHandler<T>>());

    /**
     * Creates an event object that can be subscribed to with an {@link EventHandler}.
     */
    public Event() {
    }

    /**
     * Subscribes a new event handler that will be notified of event firings.
     * @param handler The {@link EventHandler} that should be notified of events.
     */
    public synchronized void subscribe(EventHandler<T> handler) {
        if (handler != null && !_handlers.contains(handler)) {
            _handlers.add(handler);
        }
    }

    /**
     * Removes the provided event handler from the list of event handlers, so it will not be notified of future events.
     * @param handler The {@link EventHandler} to remove.
     */
    public synchronized void unsubscribe(EventHandler<T> handler) {
        _handlers.remove(handler);
    }

    /**
     * Fires the event asynchronously, causing all of its subscribed event handlers to execute on the central event dispatcher thread.
     * @param sender The object firing the event
     * @param args The arguments for the event
     */
    public void fireAsync(Object sender, T args) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.addToQueue(sender, args);
            }
        }
    }

    /**
     * Fires the event asynchronously with no arguments, causing all of its subscribed event handlers to execute on the central event dispatcher thread.
     * @param sender The object firing the event
     */
    public void fireAsync(Object sender) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.addToQueue(sender, null);
            }
        }
    }

    /**
     * Fires the event synchrnonously, causing all of its subscribed event handlers to execute on the thread this method was called from.
     * @param sender The object firing the event
     * @param args The arguments for the event
     */
    public void fireSync(Object sender, T args) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.handleEvent(sender, args);
            }
        }
    }

    /**
     * Fires the event synchrnonously with no aruments, causing all of its subscribed event handlers to execute on the thread this method was called from.
     * @param sender The object firing the event
     */
    public void fireSync(Object sender) {
        synchronized (_handlers) {
            for (EventHandler<T> e : _handlers) {
                e.handleEvent(sender, null);
            }
        }
    }
}

