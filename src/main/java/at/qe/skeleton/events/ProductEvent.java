package at.qe.skeleton.events;

import at.qe.skeleton.model.ProductEventType;

public record ProductEvent(Long productId, ProductEventType type) {
}
