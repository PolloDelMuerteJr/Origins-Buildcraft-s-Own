package name.modid;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import name.modid.powers.JumperActive;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.UUID;

public class ModEvents {

    public static void register() {

        //Blocking block interaction
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (JumperActive.CHARGING(player)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        //Blocking attacking entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (JumperActive.CHARGING(player)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        //Block breaking blocks
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

            if (JumperActive.CHARGING(player)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        //Blocking item use
        UseItemCallback.EVENT.register((player, world, hand) -> {

            if (JumperActive.CHARGING(player)) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }

            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        //Blocking entity use
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (JumperActive.CHARGING(player)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        //Prevent block breaking
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            if (JumperActive.CHARGING(player)) {
                return false;
            }

            return true;
        });

        //Reset Jumper Instability on respawn.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {

            for (Power power : PowerHolderComponent.KEY.get(newPlayer).getPowers()) {

                if (power instanceof JumperActive jumper) {
                    jumper.resetInstability();
                }

            }
        });
    }
}