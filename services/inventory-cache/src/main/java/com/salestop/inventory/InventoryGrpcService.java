package com.salestop.inventory;

import com.salestop.common.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class InventoryGrpcService
    extends InventoryReservationServiceGrpc.InventoryReservationServiceImplBase {
  private final ReservationService reservations;
  private final String internalKey;

  InventoryGrpcService(
      ReservationService reservations,
      @Value("${salestop.internal-key:local-internal-key}") String key) {
    this.reservations = reservations;
    this.internalKey = key;
  }

  @Override
  public void reserve(
      ReserveInventoryRequest request, StreamObserver<ReserveInventoryResponse> observer) {
    if (!internalKey.equals(request.getInternalKey())) {
      observer.onError(
          Status.PERMISSION_DENIED.withDescription("Invalid internal key").asRuntimeException());
      return;
    }
    try {
      var result =
          reservations.reserve(
              request.getProductId(),
              UUID.fromString(request.getUserId()),
              request.getQuantity(),
              request.getIdempotencyKey(),
              UUID.fromString(request.getProposedOrderId()));
      observer.onNext(
          ReserveInventoryResponse.newBuilder()
              .setStatus(result.status().name())
              .setOrderId(result.orderId() == null ? "" : result.orderId().toString())
              .setRemainingStock(result.remainingStock())
              .build());
      observer.onCompleted();
    } catch (Exception ex) {
      observer.onError(
          Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
    }
  }

  @Override
  public void release(
      ReleaseInventoryRequest request, StreamObserver<ReleaseInventoryResponse> observer) {
    if (!internalKey.equals(request.getInternalKey())) {
      observer.onError(Status.PERMISSION_DENIED.asRuntimeException());
      return;
    }
    boolean released =
        reservations.release(
            request.getProductId(),
            UUID.fromString(request.getUserId()),
            request.getQuantity(),
            request.getIdempotencyKey(),
            UUID.fromString(request.getOrderId()));
    observer.onNext(ReleaseInventoryResponse.newBuilder().setReleased(released).build());
    observer.onCompleted();
  }
}
