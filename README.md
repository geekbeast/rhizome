[![Build Status](https://travis-ci.org/kryptnostic/rhizome.svg?branch=develop)](https://travis-ci.org/kryptnostic/rhizome)

rhizome
=======

Java Framework for building clustered RESTful web applications 

###Motivation

Make it easy to build distributed web services in Java with dependency injection, transparent load balancing, distributed task execution, and an abstracted persistence layer by pulling together mature libraries from the Java ecosystem.

###Getting Started

1. Clone the repo
	
		> git clone https://github.com/geekbeast/rhizome.git
		
2. Setup the dev environment

		> ./gradlew eclipse
	
	or 
	
		> ./gradlew idea
		
3. Import into your IDE of choice.

#### Overview

##### Embedded Jetty for HTTP

Rhizome leverages Servlet 3.1 technologies and Spring for fully programatic web application configuration and development.  

##### Jersey and/or Spring MVC for REST + WebSockets

You can use Jersey and/or Spring MVC for building REST endpoints. 

###### STOMP for WebSockets 

Straightforward integration with socks.js and STOMP.

##### Jackson for JSON

Jackson with afterburner and guava support.

##### Metrics for metrics

Coda Hale's awesome metrics library makes it easy to create advanced metrics.

##### Guava

Don't leave home without it.

##### Log4j and SL4J for logging

Using log4j through sl4j facade, hopefully upgrade to logback soon.

##### Joda Time

Better date time

##### RethinkDB / Cassandra / Hyperdex

Cached persistence layer integrated with Hyperdex.

Note:
There is no official rethink Java driver, so for now we're using the driver provided by dkhenry (see build.gradle). It's README isn't up to date. See RethinkDB driver in rhizome.mapstores. Syntax is similar to API docs in Rethink. We use Rethink to store Base64 encoded strings as it's optimized for storing strings. We tried storing binary data and measured a large perf decrease.