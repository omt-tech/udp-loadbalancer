udp-loadbalancer
================

Load balancer for UDP built in netty.
Does stateless random robin balancing across all the provided servers.
This server will also act as a (one way) relay if you supply only one server to balance across.

How to build:

Install maven
Run: `mvn install`

Pick out `balancer.jar` from /target folder.

Run with -Dconfig= pointed at either full path of a .json extension file or a relative path to where you execute the jar.

`java -Dconfig=config.json -jar balancer.jar`

Sample config file:
```
[
	{	
		"label": "syslog",
		"port": 5044,
		"servers": ["backend-server01.internal", "backend-server01.internal","backend-server01.internal"]
	},
	{	
		"label": "syslog2",
		"port": 5045,
		"servers": ["otherone01", "otherone02","otherone03"]
	}
]
```

