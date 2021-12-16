[![Build Status](https://travis-ci.org/geekbeast/rhizome.svg?branch=develop)](https://travis-ci.org/geekbeast/rhizome)

rhizome
=======

Java Framework for building clustered RESTful web applications

###Motivation

Make it easy to build distributed web services in Java with dependency injection, transparent load balancing, distributed task execution, and an abstracted persistence layer by pulling together mature libraries from the Java ecosystem.

### Getting Started

1. Clone the repo

		> git clone git@github.com:geekbeast/rhizome.git

2. Setup the dev environment

		> ./gradlew eclipse

	or

		> ./gradlew idea

3. Import into your IDE of choice.

#### Overview

##### Hazelcast for Clustering

Distributed locks, caches, and other common data structures for building distributed applications.

##### Embedded Jetty for HTTP

Rhizome leverages Servlet 3.1 technologies and Spring for fully programatic web application configuration and development.  

##### Spring MVC for REST + WebSockets

Spring MVC for building REST endpoints.

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


##### Postgres / CitusDB Utilities

Quickly build out mapstores backed by Postgres or CitusDB. Stream large data sets directly from Postgres.
