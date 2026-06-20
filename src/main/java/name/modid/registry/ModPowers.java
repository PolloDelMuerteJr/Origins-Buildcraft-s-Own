package name.modid.registry;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import name.modid.powers.*;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;


public class ModPowers {

    public static void register() {

        //Giant Size
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_size"),
                GIANT_SIZE
        );
        //Giant Mounted Damage/slow
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_mounted"),
                GIANT_MOUNTED
        );
        //Giant elytra unstable
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_elytra"),
                GIANT_ELYTRA
        );
        //Giant counteract levitation
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_levitation"),
                GIANT_LEVITATION
        );
        //Giant footstep noise
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_footsteps"),
                GIANT_FOOTSTEPS
        );
        //Giant Breaks Blocks
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_breaks_blocks"),
                GIANT_BREAKS_BLOCKS
        );
        //Giant Constant Food drain
        Registry.register(
                ApoliRegistries.POWER_FACTORY,
                new Identifier("origin-buildcrafts-own", "giant_constant_food_drain"),
                GIANT_CONSTANT_FOOD_DRAIN
        );
    }
//--------------------------------------------------------------------------------------------------//
    //Giant Size
    public static final PowerFactory<Power> GIANT_SIZE =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_size"),
                    new SerializableData(),
                    data -> GiantSizePower::new
            );
    //Giant Mounted Damage/slow
    public static final PowerFactory<Power> GIANT_MOUNTED =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_mounted"),
                    new SerializableData(),
                    data -> GiantMountedPower::new
            );
    //Giant elytra unstable
    public static final PowerFactory<Power> GIANT_ELYTRA =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_elytra"),
                    new SerializableData(),
                    data -> GiantElytraUnstablePower::new
            );
    //Giant counteract levitation
    public static final PowerFactory<Power> GIANT_LEVITATION =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_levitation"),
                    new SerializableData(),
                    data -> GiantLevitationPower::new
            );
    //Giant footstep noise
    public static final PowerFactory<Power> GIANT_FOOTSTEPS =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_footsteps"),
                    new SerializableData(),
                    data -> GiantFootstepsPower::new
            );
    //Giant Breaks Blocks
    public static final PowerFactory<Power> GIANT_BREAKS_BLOCKS =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_breaks_blocks"),
                    new SerializableData(),
                    data -> GiantBreaksBlocksPower::new
            );
    //Giant Constant Food Drain
    public static final PowerFactory<Power> GIANT_CONSTANT_FOOD_DRAIN =
            new PowerFactory<>(
                    new Identifier("origin-buildcrafts-own", "giant_constant_food_drain"),
                    new SerializableData(),
                    data -> GiantConstantFoodDrainPower::new
            );
}