package fr.elowyr.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractEvent {

    private final String name;
    private final Set<Listener> listeners = new HashSet<>();
    private List<String> rewards;
    private boolean started, toSecure;

    public AbstractEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Listener> getListeners() {
        return listeners;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public void setRewards(List<String> rewards) {
        this.rewards = rewards;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isToSecure() {
        return toSecure;
    }

    public AbstractEvent toSecure(boolean toSecure) {
        this.toSecure = toSecure;
        return this;
    }

    public void sendMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

    public abstract String[] getLines();

    public abstract void setup();

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onFinish();

    public abstract boolean isFinish();

    public abstract void onSecurize();

    public abstract void onUnsecurize();

}
