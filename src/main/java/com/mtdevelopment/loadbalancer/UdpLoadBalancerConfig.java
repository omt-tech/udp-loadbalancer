package com.mtdevelopment.loadbalancer;

/**
 * Config holder class.
 */
public class UdpLoadBalancerConfig {

    final String label;
    final int port;
    final String[] servers;


    public UdpLoadBalancerConfig(String label, int port, String[] servers) {
        this.label = label;
        this.port = port;
        this.servers = servers;
    }

    public String toString() {
        return "Label: " + label + ", Port: " + port + ", Servers: " + java.util.Arrays.toString(servers);
    }
}
