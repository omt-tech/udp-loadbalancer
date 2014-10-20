package com.mtdevelopment.loadbalancer;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpLoadBalancerServer {


    public static void main(String[] args) throws Exception {

        boolean debug = Boolean.parseBoolean(System.getProperty("debug", "false"));
        if(debug) {
            System.out.println("Debug enabled");
            System.out.println("Arguments:" + java.util.Arrays.toString(args));

        }

        int port = 0;
        String[] servers;
        try{
            port = Integer.parseInt(args[0]);
            if(debug) {
                System.out.println("Port parsed is: " + port);
            }
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException e) {
            System.err.println("Missing or invalid port. Usage: java -jar balancer.jar port server1 server2 server3");
            System.exit(0);
        }

        if(args.length < 2) {
            System.err.println("Missing server list to load balance against. Usage: java -jar balancer.jar port server1 server2 server3");
            System.exit(0);
        }

        servers = new String[args.length-1];
        for(int i=1; i< args.length; i++) {
            servers[i-1] = args[i];
        }

        System.out.println("Started load balancer on port: " + port + " and with servers: " + java.util.Arrays.toString(servers));

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UdpLoadBalancerHandler(servers, debug));

            b.bind(port).sync().channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

}