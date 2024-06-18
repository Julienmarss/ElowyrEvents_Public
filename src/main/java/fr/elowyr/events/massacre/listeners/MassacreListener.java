package fr.elowyr.events.massacre.listeners;

import com.massivecraft.factions.FPlayers;
import fr.elowyr.events.massacre.Massacre;
import fr.elowyr.events.massacre.MassacreManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MassacreListener implements Listener {

    private final Massacre massacre;

    public MassacreListener(Massacre massacre) {
        this.massacre = massacre;
    }

    @EventHandler
    public void onBreak(EntityDeathEvent event) {
        if (!this.massacre.isStarted()) return;
        Player player = event.getEntity().getKiller();
        if (player == null || event.getEntity() instanceof Player) return;
        for (EntityType entityType : MassacreManager.getInstance().getPoints().keySet()) {
            if (event.getEntity().getType().equals(entityType)) {
                massacre.incrementPoints(FPlayers.getInstance().getByPlayer(player).getFaction(), MassacreManager.getInstance().getPoints().get(entityType));
            }
        }
    }
}
