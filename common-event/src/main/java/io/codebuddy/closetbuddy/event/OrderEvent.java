package io.codebuddy.closetbuddy.event;

import java.time.LocalDateTime;

public record OrderEvent<T>(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        T payload
) {
}
