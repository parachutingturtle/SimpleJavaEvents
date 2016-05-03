/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
