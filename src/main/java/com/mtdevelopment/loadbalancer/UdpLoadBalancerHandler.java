package com.mtdevelopment.loadbalancer;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Random;

public class UdpLoadBalancerHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    private static final Random random = new Random();

    public UdpLoadBalancerHandler(UdpLoadBalancerConfig config, boolean debug) {
        this.debug = debug;
        this.servers = config.servers;
        this.port = config.port;
    }

    private boolean debug;
    private String[] servers;
    private int port;


    String getNextServer() {
        if(debug) {
            String server = servers[random.nextInt(servers.length)];
            System.out.println("Sending to server: " + server);
            return server;
        }
        return servers[random.nextInt(servers.length)];
    }

    InetSocketAddress getServer() {
        return new InetSocketAddress(getNextServer(), port);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) {
        DatagramPacket newPacket = new DatagramPacket(packet.content(), getServer());
        newPacket.retain();
        ctx.write(newPacket);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }

}
