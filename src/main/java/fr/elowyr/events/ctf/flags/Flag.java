package fr.elowyr.events.ctf.flags;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Flag {

    private Location location;
    private String playerName;
    private int time;
    private String factionId;
    private ItemStack helmet;
    private FlagState flagState;

    public Flag(Location location) {
        this.location = location;
        flagState = FlagState.AVAILABLE;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public boolean isFlagState(FlagState flagState) {
        return this.flagState.equals(flagState);
    }

    public void setFlagState(FlagState flagState) {
        this.flagState = flagState;
    }

}
