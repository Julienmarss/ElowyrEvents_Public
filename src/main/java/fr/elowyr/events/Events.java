package fr.elowyr.events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.HashSet;
import java.util.Set;

public class Events {

    private final Set<AbstractEvent> events = new HashSet<>();
    private boolean started;

    public void registerEvent(AbstractEvent event) {
        event.setup();
        events.add(event);
    }

    public void startEvent(AbstractEvent event) {
        if (started) {
            return;
        }
        event.setStarted(started = true);
        registerListeners(event);
        event.onStart();
    }

    public boolean stopEvent(AbstractEvent event) {
        if (!started || !event.isStarted()) {
            return false;
        }
        event.onStop();
        resetEvent(event);
        return true;
    }

    public boolean finishEvent(AbstractEvent event) {
        if (!event.isFinish()) {
            return false;
        }
        resetEvent(event);
        event.onFinish();
        return true;
    }

    private void resetEvent(AbstractEvent event) {
        event.setStarted(started = false);
        event.getListeners().forEach(HandlerList::unregisterAll);
    }

    private void registerListeners(AbstractEvent event) {
        event.getListeners().forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, ElowyrEvents.getInstance()));
    }

    public Set<AbstractEvent> getEvents() {
        return events;
    }

    public AbstractEvent getEvent(String name) {
        return events.stream().filter(event -> event.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
