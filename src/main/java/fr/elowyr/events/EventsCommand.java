package fr.elowyr.events;

import fr.elowyr.events.geantkoth.GeantKoth;
import fr.elowyr.events.koth.KOTH;
import fr.elowyr.events.koth.KOTHManager;
import fr.elowyr.events.tasks.PreStartTask;
import fr.elowyr.events.totems.classic.Totem;
import fr.elowyr.events.totems.classic.TotemManager;
import fr.elowyr.events.totems.geant.TotemGeant;
import fr.elowyr.events.totems.geant.TotemGeantManager;
import fr.elowyr.events.utils.api.Command;
import fr.elowyr.events.utils.api.CommandArgs;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class EventsCommand {

    private final ElowyrEvents elowyrEvents = ElowyrEvents.getInstance();
    private PreStartTask preStartTask;

    @Command(name = "elowyrevents", permission = "elowyrevents.admin")
    public void onCommand(CommandArgs commandArgs) {
        CommandSender sender = commandArgs.getSender();
        String[] args = commandArgs.getArgs();

        if (args.length < 2) {
            sender.sendMessage("§6§lElowyr §7§l• §c/elowyrevents <action> <event>.");
            return;
        }

        AbstractEvent event = elowyrEvents.getEvents().getEvent(args[1]);
        if (event == null) {
            String events = elowyrEvents.getEvents()
                    .getEvents()
                    .stream()
                    .map(AbstractEvent::getName)
                    .collect(Collectors.joining(", "));

            sender.sendMessage("§6§lElowyr §7§l• §cCet event n'existe pas !");
            sender.sendMessage("§cLes event disponibles sont : " + events);
            return;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (!event.isStarted()) {
                if (event instanceof KOTH) {
                    if (args.length < 3) {
                        sender.sendMessage("§6§lElowyr §7§l• §c/elowyrevents start koth <name>");
                        return;
                    }
                    final String b = args[2];
                    final KOTHManager manager = ((KOTH) event).kothManager;
                    if (!manager.cuboid.containsKey(b)) {
                        sender.sendMessage("§6§lElowyr §7§l• §cCe KOTH n'existe pas.");
                        sender.sendMessage("§cListe: §7" + manager.cuboid.keySet());
                        return;
                    }
                    manager.current = manager.cuboid.get(b);
                } else if (event instanceof GeantKoth) {
                    if (args.length < 3) {
                        sender.sendMessage("§6§lElowyr §7§l• §c/elowyrevents start geankoth <name>.");
                        return;
                    }
                    final String name = args[2];
                    final GeantKoth koth = ((GeantKoth) event);
                    if (!koth.isZonePresent(name)) {
                        sender.sendMessage("§6§lElowyr §7§l• §cCe KOTH n'existe pas.");
                        sender.sendMessage("§cListe: §7" + String.join(", ", koth.getZones()));
                        return;
                    }
                    koth.setZone(name);
                } else if (event instanceof Totem) {
                    if (args.length < 3) {
                        sender.sendMessage("§6§lElowyr §7§l• §c/elowyrevents start totem <name>.");
                        return;
                    }
                    final String b = args[2];
                    final TotemManager manager = ((Totem) event).getTotemManager();
                    if (!manager.map.containsKey(b)) {
                        sender.sendMessage("§6§lElowyr §7§l• §cCe Totem n'existe pas.");
                        sender.sendMessage("§cListe: §7" + manager.map.keySet());
                        return;
                    }
                    manager.location = manager.map.get(b);
                } else if (event instanceof TotemGeant) {
                    if (args.length < 3) {
                        sender.sendMessage("§6§lElowyr §7§l• §c/elowyrevents start totemgeant <name>.");
                        return;
                    }
                    final String b = args[2];
                    final TotemGeantManager manager = ((TotemGeant) event).getTotemManager();
                    if (!manager.map.containsKey(b)) {
                        sender.sendMessage("§6§lElowyr §7§l• §cCe Totem Géant n'existe pas.");
                        sender.sendMessage("§cListe: §7" + manager.map.keySet());
                        return;
                    }
                    manager.location = manager.map.get(b);
                }
                preStartTask = new PreStartTask(event);
                preStartTask.runTaskTimer(elowyrEvents, 0, 20);
                sender.sendMessage("§6§lElowyr §7§l• §fVous avez lancé l'event §6" + event.getName() + "§f.");
            } else {
                sender.sendMessage("§6§lElowyr §7§l• §cL'event n'a pas pu se démarrer !");
            }
        }

        if (args[0].
                equalsIgnoreCase("stop")) {
            if (elowyrEvents.getEvents().stopEvent(event)) {
                sender.sendMessage("§eVous avez arrêté l'event §8» §6" + event.getName());
            } else if (preStartTask != null && preStartTask.getEvent().getName().equals(event.getName())) {
                preStartTask.cancel();
                sender.sendMessage("§eVous avez arrêté l'event §8» §6" + event.getName());
            } else {
                sender.sendMessage("§cCet event n'est pas démarré !");
            }
        }
    }
}