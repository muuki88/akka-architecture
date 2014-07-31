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
trait YourServiceConsumer

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

## Instantiate the consumer or where to get the Actor?

In the give example above, I delegated the lookup for the ActorRef to the developer
that instantiates the `GreetingServiceConsumer` class. This one of the ways to
deal with the actor lookup. This is a list of possibilites I come up with so far

1. Instantiate the actor yourself with `val service = system.actorOf(Props[GreetingServiceActor])`
2. The consumer is an actor itself and can try to look up the service with an `ActorSelection`
3. You have another service creating the specified actor for you

This point is really crucial, because now a new developer needs a a second few on the
system. The first perspective was 

> which actor provides which api  

Your IDE and coding tools help you paint this picture. The second view on the system is now the
actor hierarchy

> where does every actor live that implements this api

Implementing your hierarchy in a microservice manner helps to get just enough of
this hierarchy. I developed a lot with OSGi, which has really a similar approach
to system design. Like OSGi services, actors can be very dynamic. They can be gone
the time you created the actor selection or you have to create them yourself.
OSGi solved this by a declarative xml description of the services and later with
annotations. 

This problem is not meant to be solved by this pattern, but is a crucial part.
So be sure you have a good solution in mind before apply any of this. Your API
will be as unusable as before if you are not able to provide a comprehensible way
to resolve your actorRefs.

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

## Why not use typed actors?

[TypedActor](http://doc.akka.io/docs/akka/snapshot/scala/typed-actors.html)s are there to solve
this problem as well. For simple use cases this should be your first choice IMHO. However if you
want to create services, which depend on other services, routing semantics, clustering, then it
was easier for me to stick with the plain actor semantics. 

The API pattern is like the TypedActor pattern for boundaries between non-actor and actor systems.
Use it for more complex implementations of your service actor.
