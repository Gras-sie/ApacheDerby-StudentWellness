package com.wellness.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * A very lightweight publish-subscribe event bus for decoupled communication
 * between controllers, services and views. Thread-safe using concurrent
 * collections so it can be invoked from background threads (e.g. SwingWorker)
 * as well as the Swing EDT.
 *
 * Usage:
 *   EventBus.register(EventType.DATA_CHANGED, evt -> myPanel.refresh());
 *   EventBus.post(EventType.DATA_CHANGED);
 */
public final class EventBus {

    /** Enumeration of generic events used by the application. Extend as needed. */
    public enum EventType {
        APPOINTMENT_UPDATED,
        COUNSELOR_UPDATED,
        FEEDBACK_UPDATED,
        CONNECTION_STATUS_CHANGED
    }

    private static final Map<EventType, Set<Consumer<EventType>>> LISTENERS = new ConcurrentHashMap<>();

    private EventBus() { /* utility */ }

    /**
     * Registers a listener for a given event type.
     * @param type    the event to listen to
     * @param handler the callback executed when the event is fired
     */
    public static void register(EventType type, Consumer<EventType> handler) {
        LISTENERS.computeIfAbsent(type, k -> new CopyOnWriteArraySet<>()).add(handler);
    }

    /**
     * Deregisters a previously registered listener.
     * @param type    the event type to unregister from
     * @param handler the callback to remove
     */
    public static void unregister(EventType type, Consumer<EventType> handler) {
        Set<Consumer<EventType>> set = LISTENERS.get(type);
        if (set != null) {
            set.remove(handler);
        }
    }

    /**
     * Fires an event to all registered listeners. Works off-EDT too - each
     * handler is executed on the same thread that calls {@link #post(EventType)}.
     * @param type the event type to post
     */
    public static void post(EventType type) {
        Set<Consumer<EventType>> set = LISTENERS.get(type);
        if (set != null) {
            for (Consumer<EventType> handler : set) {
                try {
                    handler.accept(type);
                } catch (Exception ex) {
                    ex.printStackTrace(); // simple error sink - consider logging
                }
            }
        }
    }
}
