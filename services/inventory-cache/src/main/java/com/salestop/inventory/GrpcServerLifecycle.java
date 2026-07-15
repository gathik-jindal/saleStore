package com.salestop.inventory;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
class GrpcServerLifecycle implements SmartLifecycle {
  private final InventoryGrpcService service;
  private final int port;
  private Server server;
  private volatile boolean running;

  GrpcServerLifecycle(
      InventoryGrpcService service, @Value("${inventory.grpc-port:9095}") int port) {
    this.service = service;
    this.port = port;
  }

  public void start() {
    try {
      server = NettyServerBuilder.forPort(port).addService(service).build().start();
      running = true;
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to start inventory gRPC server", ex);
    }
  }

  public void stop() {
    if (server != null) server.shutdown();
    running = false;
  }

  public boolean isRunning() {
    return running;
  }
}
