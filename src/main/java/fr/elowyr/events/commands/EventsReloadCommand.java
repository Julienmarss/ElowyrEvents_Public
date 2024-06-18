package fr.elowyr.events.commands;

import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.koth.KOTH;
import fr.elowyr.events.koth.KOTHManager;
import fr.elowyr.events.totems.geant.TotemGeant;
import fr.elowyr.events.totems.geant.TotemGeantManager;
import fr.elowyr.events.utils.Utils;
import fr.elowyr.events.utils.api.Command;
import fr.elowyr.events.utils.api.CommandArgs;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

public class EventsReloadCommand {

    @Command(name = "elowyrevents.reload", permission = "elowyrevents.reload")
    public void onCommand(CommandArgs args) throws IOException, InvalidConfigurationException {
        if (args.length() == 0) {
            ElowyrEvents.getInstance().reloadConfig();
            args.getSender().sendMessage(Utils.color("&aRELOAD PAR LA PUISSANCE DE ANZOK LE GOAT"));
        } else if (args.length() == 1) {
            AbstractEvent event = ElowyrEvents.getInstance().getEvents().getEvent(args.getArgs(0));
            if (args.getArgs(0).equalsIgnoreCase("koth")) {
                final KOTHManager manager = ((KOTH) event).kothManager;
                manager.getConfig().load("koth.yml");
            } else if (args.getArgs(0).equalsIgnoreCase("totemgeant")) {
                final TotemGeantManager manager = ((TotemGeant) event).totemManager;
                manager.getConfig().load("totemgeant.yml");
            }
            args.getSender().sendMessage(Utils.color("&aRELOAD PAR LA PUISSANCE DE ANZOK LE GOAT"));
        }
    }
}
