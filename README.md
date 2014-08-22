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

##### Embedded Jetty ( and coming soon Tomcat ) for HTTP

Rhizome leverages Servlet 3.1 technologies and Spring for fully programatic web application configuration and development.  

##### Jersey and/or Spring MVC for REST

You can use Jersey and/or Spring MVC for building REST endpoints. 

###### Atmosphere for Websockets / Long Polling

Atmosphere can be hooked up either through Spring MVC or Jersey to provide websocket or cometd endpoints for Ajax push / long polling.

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