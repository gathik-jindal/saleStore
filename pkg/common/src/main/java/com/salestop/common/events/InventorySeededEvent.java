package com.salestop.common.events;

import java.time.Instant;

public record InventorySeededEvent(long productId, int stock, Instant seededAt) {}
