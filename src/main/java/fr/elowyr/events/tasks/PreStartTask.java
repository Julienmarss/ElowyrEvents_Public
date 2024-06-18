package fr.elowyr.events.tasks;

import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PreStartTask extends BukkitRunnable {
    private final AbstractEvent event;
    private final int interval;
    private final String[] lines;

    private int time = 10;
    private int index;

    public PreStartTask(AbstractEvent event) {
        this.event = event;
        this.interval = (60 / event.getLines().length) - 5;
        this.lines = event.getLines();
    }

    @Override
    public void run() {
       /* if (event.isToSecure()) {
            event.onUnsecurize();
            event.toSecure(false);
        }*/
        if (time == 60 || time == 30 || time == 15 || time == 10 || time == 5) {
            event.sendMessage(" §fLe §e" + event.getName() + " §fva débuter dans §b" + time + " §fsecondes.");
        }

        if (lines.length > index && (60 - ((index + 1) * interval)) == time) {
            Bukkit.getOnlinePlayers().forEach(player -> Title.sendTitle(player, 20, 40, 20, "§6§lELOWYR", lines[index]));
            index++;
        }

        if (time == 0) {
            ElowyrEvents.getInstance().getEvents().startEvent(event);
            cancel();
        }
        time--;
    }

    public AbstractEvent getEvent() {
        return event;
    }

}
