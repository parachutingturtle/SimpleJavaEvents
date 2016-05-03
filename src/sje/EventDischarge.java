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

/**
 * Contains the information of a single firing of an event.
 * @param <T> The type of the event parameter passed.
 */
public class EventDischarge<T> {

    private final Object _sender;

    /**
     * @return the _sender
     */
    public Object getSender() {
        return _sender;
    }

    private final T _parameter;

    /**
     * @return the _parameter
     */
    public T getParameter() {
        return _parameter;
    }

    public EventDischarge(Object sender, T parameter) {
        _sender = sender;
        _parameter = parameter;
    }

}
