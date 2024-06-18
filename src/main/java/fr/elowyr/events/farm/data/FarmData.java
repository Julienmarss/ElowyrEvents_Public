package fr.elowyr.events.farm.data;

import com.massivecraft.factions.Faction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FarmData {

    private Faction faction;
    private int points;

    public void incrementPoints(int value) {
        this.points += value;
    }
}
