# Akka Architecture

This sample projects aims to provide a pattern for building akka applications.

## Main Goals

* Easy-to-use api 
* Providing consumer and provider traits
* Meaningful docs
* Enable your tools to help you (find usage, implements trait `xy`)

## Concept

The basic idea is providing traits to define what you can do
and what you should do as a _consumer_ and _provider_ (meaning
you implement the API)

```scala

/** define methods to implement and a default PartialFunction for the receive method */
trait YourService { this: Actor => }

/** contains the case classes /objects which define the messages */
object YourSerivce

/** implement this to use YourService */
YourServiceConsumer

```

## Details

By implementing a service trait (e.g `GreetingService`) the developer has guidance what to implement. The
definer of the trait specifies which method should be called on which message.
This could look like this:

```scala
def greetingReceive: Receive = {
  case Greet(lang) => greet(lang) pipeTo sender
  case Greeting(lang, greeting) => setGreeting(lang, greeting)
}
```

the methods `greet(lang: String): Future[String]` and `setGreeting(lang: String, greeting: String)`
have to be implemented by an actor providing this service. To _activate_, the receive method
must be composed

```scala
def receive = greetingReceive orElse otherReceive
```

On the other side a consumer can implement a `GreetingServiceConsumer` trait to know what
can be used. The only thing the consumer should provide is an `ActorRef` to the actual actor
which provides this service. The easiest way would look like this

```scala
class GreetingConsumer(service: akka.actor.ActorRef) extends GreetingServiceConsumer {
  def greetingsService() = service
}
```

# Discussion

I came up with this approach as the learning curve is pretty step for newbies to scala and
akka in particular. My colleques had problemes following the flow of the messages implementing
new features in an existing system.

Pros and cons of this approach

| Pros                                          | Cons                                            |
| --------------------------------------------- |:-----------------------------------------------:|
| Easier for consumer and provider to use       | This approach creates a lot of duplicated code  |
| Typesafty, changes can evolve on both sides   | Cannot use become/unbecome so easy              |
| clear docs                                    |                                                 |
| use tools to identify usages and dependencies |                                                 |


This is neither a battle tested pattern nor a result of longterm experience, so use with care
of help improve it.

