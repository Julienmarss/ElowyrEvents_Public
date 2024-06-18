package fr.elowyr.events.ctf;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.ctf.flags.FlagState;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class CTF extends AbstractEvent {

    private CTFManager ctfManager;

    public CTF() {
        super("CTF");
    }

    @Override
    public String[] getLines() {
        return new String[]{"§fEvent: §6§lCaptureTheFlag", " §fCapturez les §e4 §fdrapeaux.", " §fGagnez §e5 §fpoints au classement."};
    }

    @Override
    public void setup() {
        ctfManager = new CTFManager(this).setup();
    }

    @Override
    public void onStart() {
        sendMessage("§6§lCTF §7§l• §fvient d'apparaitre. §fAllez chercher les drapeaux dans " +
                "chaque coins avant §el'antiback ap §fpour les ramener au §b/warp ctf.");
        ctfManager.setFactionId(null);
        Arrays.stream(ctfManager.getFlags()).forEach(flag -> ctfManager.spawnBanner(flag));
        ctfManager.startTask();
        //ScoreboardManager.getInstance().setCurrentBoard(new CTFBoard(ctfManager));
    }

    @Override
    public void onStop() {
        sendMessage("§6§lCTF §7§l• §fLe ctf a été arrêté.");
        Arrays.stream(ctfManager.getFlags()).forEach(flag -> flag.getLocation().getBlock().setType(Material.AIR));
        reset();
    }

    @Override
    public void onFinish() {
        List<String> winners = Lists.newArrayList();
        String faction = Factions.getInstance().getFactionById(ctfManager.getFactionId()).getTag();
        winners.add(faction);
        ElowyrEvents.getInstance().sendResult(this, winners);
        sendMessage("§6§lCTF §7§l• §fLa faction §e" + faction + " §fremporte le ctf.");
        getRewards().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%",
                ctfManager.getPlayerName())));
        reset();
    }

    @Override
    public boolean isFinish() {
        return Arrays.stream(ctfManager.getFlags()).allMatch(flag -> flag.isFlagState(FlagState.PLACED));
    }

    @Override
    public void onSecurize() {

    }

    @Override
    public void onUnsecurize() {

    }

    private void reset() {
        ctfManager.getTask().cancel();
        //ScoreboardManager.getInstance().setCurrentBoard(new GlobalBoard());
    }

}
