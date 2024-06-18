package fr.elowyr.events.massacre.data;

import com.massivecraft.factions.Faction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MassacreData {

    private Faction faction;
    private int points;

    public void incrementPoints(int value) {
        this.points += value;
    }
}
