package fr.elowyr.events;

import fr.elowyr.events.commands.EventsReloadCommand;
import fr.elowyr.events.ctf.CTF;
import fr.elowyr.events.domination.Domination;
import fr.elowyr.events.farm.Farm;
import fr.elowyr.events.geantkoth.GeantKoth;
import fr.elowyr.events.koth.KOTH;
import fr.elowyr.events.massacre.Massacre;
import fr.elowyr.events.totems.StatsCommand;
import fr.elowyr.events.totems.classic.Totem;
import fr.elowyr.events.totems.geant.TotemGeant;
import fr.elowyr.events.utils.DiscordUtils;
import fr.elowyr.events.utils.api.CommandFramework;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ElowyrEvents extends JavaPlugin {
    private static ElowyrEvents instance;

    private Events events;
    private KitsManager kitsManager;
    private CommandFramework commandFramework;

    public static ElowyrEvents getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        events = new Events();
        this.kitsManager = new KitsManager();
        commandFramework = new CommandFramework(this);
        commandFramework.registerCommands(new EventsCommand());
        commandFramework.registerCommands(new EventsReloadCommand());
        commandFramework.registerCommands(new StatsCommand());
        this.registerEvents();
        registerPlaceHolder();
    }

    private void registerEvents() {
        events.registerEvent(new Totem().toSecure(true));
        events.registerEvent(new TotemGeant().toSecure(true));
        events.registerEvent(new KOTH().toSecure(true));
        events.registerEvent(new CTF());
        events.registerEvent(new Farm());
        events.registerEvent(new Massacre());
        events.registerEvent(new GeantKoth().toSecure(true));
        events.registerEvent(new Domination().toSecure(true));
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {

            @Override
            public String getIdentifier() {
                return "elowyrevents";
            }

            @Override
            public String getAuthor() {
                return "AnZok";
            }

            @Override
            public String getVersion() {
                return "1.0";
            }

            @Override
            public String onPlaceholderRequest(Player player, String params) {
                if (params.contains("isEnable")) {
                    String[] args = params.split(":");
                    if (getEvents().getEvent(args[1]).isStarted()) {
                        return "true";
                    } else {
                        return "false";
                    }
                }
                return super.onPlaceholderRequest(player, params);
            }
        }.register();
    }

    public void sendResult(AbstractEvent event, List<String> winners) {
        DiscordUtils embed = new DiscordUtils("https://discord.com/api/webhooks/1220664343218356234/IYYiYMk48e0Dg5vF9Kj8yqPb9hPYrH4H2r0gTquD1RXxGoP2iQNCqgNDZNQFJgKN3FGb");
        embed.setAvatarUrl("https://cdn.discordapp.com/attachments/1196203764105883669/1220664513557434398/icon.png?ex=660fc36a&is=65fd4e6a&hm=a53172e24227bb1cf96ed252cef056c3507962acab5ea01a84ff7d25ef040e18&");
        embed.setUsername("Elowyr - Events");

        DiscordUtils.EmbedObject embedObject = new DiscordUtils.EmbedObject();
        embedObject.setTitle("<a:crossed_swords:959520798664704070>  **Résultats Event " + WordUtils.capitalize(event.getName()) + "** <a:crossed_swords:959520798664704070>");
        embedObject.setColor(Color.YELLOW);
        embedObject.setFooter("play.elowyr.fr", "https://cdn.discordapp.com/attachments/1087087052094455819/1195510388079460483/icon.png?ex=65b440d0&is=65a1cbd0&hm=fb9807312be149432e3752f109301b1f490a6c406fc9174571229b80362fb901&");
        StringBuilder top = new StringBuilder();
        for (int i = 0; i < winners.size(); i++) {
            top.append(String.format(this.getTopEmoji(i) + "**• %s** \\n", winners.get(i)));
        }
        embedObject.addField(String.format("**Classement du %s:**", WordUtils.capitalize(event.getName())), top.length() > 0 ? top.toString() : "Aucun", false);
        embed.addEmbed(embedObject);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                embed.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getTopEmoji(int i) {
        if (i == 0) {
            return "<a:first_place:959480104105431162>";
        }
        if (i == 1) {
            return "<a:second_place:959480104105431162>";
        }
        return "<a:third_place:959480104105431162>";
    }

    public Events getEvents() {
        return events;
    }

    public KitsManager getKitsManager() {
        return kitsManager;
    }

    public CommandFramework getCommandFramework() {
        return commandFramework;
    }
}
