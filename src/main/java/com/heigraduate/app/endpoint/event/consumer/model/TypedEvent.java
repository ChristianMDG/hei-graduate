package com.heigraduate.app.endpoint.event.consumer.model;

import com.heigraduate.app.PojaGenerated;
import com.heigraduate.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
