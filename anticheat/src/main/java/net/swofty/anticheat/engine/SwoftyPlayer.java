package net.swofty.anticheat.engine;

import lombok.Getter;
import net.swofty.anticheat.event.SwoftyEventHandler;
import net.swofty.anticheat.event.events.PlayerPositionUpdateEvent;
import net.swofty.anticheat.math.Pos;
import net.swofty.anticheat.math.Vel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SwoftyPlayer {
    public static Map<UUID, SwoftyPlayer> players = new HashMap<>();

    private final UUID uuid;
    private List<PlayerTickInformation> lastTicks;
    private PlayerTickInformation currentTick;

    public SwoftyPlayer(UUID uuid) {
        this.uuid = uuid;

        players.put(uuid, this);
    }

    public void moveTickOn() {
        Pos currentPos = currentTick.getPos();
        Pos lastPos = !lastTicks.isEmpty() ? lastTicks.getLast().getPos() : currentPos;
        Vel calculatedVelocity = currentPos.sub(lastPos).asVel();

        currentTick = new PlayerTickInformation(currentTick.getPos(), calculatedVelocity, currentTick.isOnGround());

        SwoftyEventHandler.callEvent(new PlayerPositionUpdateEvent(this,
                lastTicks.getLast(),
                currentTick
        ));

        lastTicks.add(currentTick);
        // Remove any ticks that are older than 20 ticks
        if (lastTicks.size() > 20) lastTicks.removeFirst();

        // Resort contexts
        for (int i = 0; i < lastTicks.size(); i++) {
            lastTicks.get(i).updateContext(i == lastTicks.size() - 1 ? null : lastTicks.get(i + 1), i == 0 ? null : lastTicks.get(i - 1));
        }

        currentTick = new PlayerTickInformation(currentTick.getPos(), currentTick.getVel(), currentTick.isOnGround());
    }

    public void processMovement(@NotNull Pos packetPosition, boolean onGround) {
        if (currentTick == null) return;
        PlayerTickInformation newTick = new PlayerTickInformation(
                packetPosition,
                currentTick.getVel(),
                onGround
        );
    }
}
