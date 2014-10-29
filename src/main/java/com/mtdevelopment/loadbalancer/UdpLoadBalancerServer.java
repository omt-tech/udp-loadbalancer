package com.mtdevelopment.loadbalancer;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.boon.core.value.LazyValueMap;
import org.boon.core.value.ValueList;
import org.boon.json.JsonException;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UdpLoadBalancerServer {

    static boolean continueRunning = true;


    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                continueRunning = false;
            }
        });

        boolean debug = Boolean.parseBoolean(System.getProperty("debug", "false"));
        if (debug) {
            System.out.println("Debug enabled");
            System.out.println("Arguments:" + java.util.Arrays.toString(args));

        }

        String configFilePath = System.getProperty("config", null);
        if (configFilePath == null) {
            System.err.println("Missing config file. Usage: java -jar balancer.jar -Dconfig=config.json");
            System.exit(-1);
        }
        if (!configFilePath.contains("json")) {
            System.err.println("Config file has to be json. Usage: java -jar balancer.jar -Dconfig=config.json");
            System.exit(-1);
        }

        File configFile = new File(configFilePath);
        if (!configFile.exists() || !configFile.canRead()) {
            System.err.println("Cannot read config file.");
            System.exit(-1);
        }


        //Read in config file
        ObjectMapper mapper = JsonFactory.create();
        ValueList root = null;
        try {
            root = (ValueList) mapper.readValue(configFile, Object.class);
        } catch (ClassCastException e) {
            System.err.println("Cannot read config file, wrong format.");
            System.exit(-1);
        } catch (JsonException e) {
            System.err.println("Malformed JSON, the error is: " + e.getMessage());
            System.exit(-1);
        }

        List<UdpLoadBalancerConfig> configList = new ArrayList<>();
        for (Object config : root) {
            LazyValueMap map = (LazyValueMap) config;
            try {
                String label = (String) map.get("label");
                int port = (int) map.get("port");
                ValueList serverList = (ValueList) map.get("servers");
                List<String> servers = serverList.stream().map(server -> (String) server).collect(Collectors.toList());
                configList.add(new UdpLoadBalancerConfig(label, port, servers.toArray(new String[servers.size()])));
            } catch (NullPointerException | ClassCastException e) {
                System.err.println("Malformed config file. Input should be array of objects with fields: \"label\"(string), \"port\"(int), \"servers\"(list of strings)");
                System.exit(-1);
            }
        }

        //Check for duplicate ports
        boolean duplicatePort = false;
        Map<String, Integer> duplicateCounter = new HashMap<>();
        for (UdpLoadBalancerConfig config : configList) {
            Integer count = duplicateCounter.get(Integer.toString(config.port));
            if (count == null) {
                count = 0;
            } else {
                duplicatePort = true;
                System.err.println("Error, port " + config.port + " duplicated multiple times in the config.");
            }
            count = count + 1;
            duplicateCounter.put(Integer.toString(config.port), count);
        }
        if (duplicatePort) {
            System.exit(-1);
        }


        EventLoopGroup group = new NioEventLoopGroup();
        ChannelGroup channels = new DefaultChannelGroup(group.next());
        try {
            for(UdpLoadBalancerConfig config : configList) {
                System.out.println("Started load balancer on port: " + config.port + " and with servers: " + java.util.Arrays.toString(config.servers));
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .handler(new UdpLoadBalancerHandler(config, debug));
                channels.add(b.bind(config.port).sync().channel());
            }



            while (continueRunning)
            {
                Thread.sleep(1000);
            }
            // Close all channels when you don't need them any longer
            channels.close().await();

        } finally {
            group.shutdownGracefully();
        }
    }

}