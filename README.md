# SimpleJavaEvents
A clean, easy-to-use, strongly-typed implementation of the Observer pattern for Java

Synchronous and asynchronous event firing, unlimited subscribing and unsubscribing, and easy management.
Declare any number of events per class, and pass strongly typed event arguments with your events.
Think C# events, with optional automatic asynchronous dispatching.

### Usage:

##### Declaring an event on the *observable* side:

```
private Event<String> _messageReceived = new Event<String>();
public Event<String> getMessageReceivedEvent() { return _messageReceived; }
```

##### Subscribing to an event from the *observer* side:

`observableObject.getMessageReceivedEvent().subscribe(new MessageHandler());`

Here the `MessageHandler` class doesn't yet exist, but after typing it, a modern IDE will let you create the class (perhaps as a nested class for convenience) with one click, and then fill in the implementation of its abstract method with another click, so it will look something like this:

```
private static class MessageHandler extends EventHandler<String> {
  @Override
    protected void handleEvent(Object sender, String e) {
      //TODO handle event here
    }
}
```
##### Firing an event on the *observable* side:

Synchronous firing (will be handled on the same thread it was fired from):

` _messageReceived.fireSync(this, "Event argument");`

Asynchronous firing (will be handled on the central event dispatcher thread):

` _messageReceived.fireAsync(this, "Event argument");`


