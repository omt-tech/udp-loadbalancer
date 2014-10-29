udp-loadbalancer
================

Load balancer for UDP built in netty.
Does stateless random robin balancing across all the provided servers.
This server will also act as a (one way) relay if you supply only one server to balance across.

How to build:

Install maven
Run: `mvn install`

Pick out `balancer.jar` from /target folder.

Run with `java -jar balancer.jar <port> <server1> <server2>`
For example for sip udp trace balancing:

`java -jar balancer.jar 5060 tracelogger01.internal tracelogger02.internal`

