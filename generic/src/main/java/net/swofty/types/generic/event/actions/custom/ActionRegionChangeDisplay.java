package net.swofty.types.generic.event.actions.custom;

import net.minestom.server.event.Event;
import net.swofty.types.generic.event.EventNodes;
import net.swofty.types.generic.event.SkyBlockEventClass;
import net.swofty.types.generic.user.SkyBlockPlayer;
import net.swofty.types.generic.user.statistics.StatisticDisplayReplacement;
import net.swofty.types.generic.event.SkyBlockEvent;
import net.swofty.types.generic.event.custom.PlayerRegionChangeEvent;

public class ActionRegionChangeDisplay implements SkyBlockEventClass {

    @SkyBlockEvent(node = EventNodes.CUSTOM , requireDataLoaded = true)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        if (event.getTo() != null && event.getFrom() != null
                && !event.getTo().equals(event.getFrom())) {
            player.setDisplayReplacement(StatisticDisplayReplacement
                    .builder()
                    .ticksToLast(20)
                    .display(event.getTo().getColor() + " ⏣ " + event.getTo().getName())
                    .build(), StatisticDisplayReplacement.DisplayType.DEFENSE
            );
        }
    }
}
