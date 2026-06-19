package com.salestop.identity.grpc;

import java.io.IOException;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.salestop.identity.config.IdentityProperties;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {
    private final IdentityProperties properties;
    private final IdentityGrpcService identityGrpcService;
    private Server server;
    private boolean running;

    public GrpcServerLifecycle(IdentityProperties properties, IdentityGrpcService identityGrpcService) {
        this.properties = properties;
        this.identityGrpcService = identityGrpcService;
    }

    @Override
    public void start() {
        try {
            server = NettyServerBuilder.forPort(properties.grpc().port())
                .addService(identityGrpcService)
                .build()
                .start();
            running = true;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to start identity gRPC server", ex);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
