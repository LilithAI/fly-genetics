package io.flyproject.connectomefly;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import io.flyproject.connectomefly.entity.FlyEggEntity;
import io.flyproject.connectomefly.entity.FlyMind;
import io.flyproject.connectomefly.lab.ArenaBuilder;
import io.flyproject.connectomefly.lab.FlyLabCommand;
import io.flyproject.connectomefly.lab.LoomStimulus;
import io.flyproject.connectomefly.lab.OptomotorDrum;
import io.flyproject.connectomefly.lab.WindFx;
import io.flyproject.connectomefly.neural.Circuit;
import java.util.Iterator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntries;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.class_124;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_1792;
import net.minecraft.class_1826;
import net.minecraft.class_2246;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3709;
import net.minecraft.class_4048;
import net.minecraft.class_7706;
import net.minecraft.class_7923;
import net.minecraft.class_1792.class_1793;

public class ConnectomeFlyMod implements ModInitializer {
   public static final String MOD_ID = "connectomefly";
   public static final class_1299<ConnectomeFlyEntity> CONNECTOME_FLY = (class_1299<ConnectomeFlyEntity>)class_2378.method_10230(
      class_7923.field_41177,
      new class_2960("connectomefly", "connectome_fly"),
      FabricEntityTypeBuilder.create(class_1311.field_6294, ConnectomeFlyEntity::new)
         .dimensions(class_4048.method_18385(0.6F, 0.45F))
         .trackRangeBlocks(64)
         .build()
   );
   public static final class_1299<FlyEggEntity> FLY_EGG = (class_1299<FlyEggEntity>)class_2378.method_10230(
      class_7923.field_41177,
      new class_2960("connectomefly", "fly_egg"),
      FabricEntityTypeBuilder.create(class_1311.field_17715, FlyEggEntity::new).dimensions(class_4048.method_18385(0.3F, 0.25F)).trackRangeBlocks(48).build()
   );
   public static final class_1792 CONNECTOME_FLY_SPAWN_EGG = (class_1792)class_2378.method_10230(
      class_7923.field_41178, new class_2960("connectomefly", "connectome_fly_spawn_egg"), new class_1826(CONNECTOME_FLY, 14263361, 11740702, new class_1793())
   );
   private static Circuit escapeCircuit;
   private static Circuit steeringCircuit;

   public static synchronized Circuit escapeCircuitData() {
      if (escapeCircuit == null) {
         escapeCircuit = Circuit.loadResource("/data/connectomefly/circuit.json");
      }

      return escapeCircuit;
   }

   public static synchronized Circuit steeringCircuitData() {
      if (steeringCircuit == null) {
         steeringCircuit = Circuit.loadResource("/data/connectomefly/steering_circuit.json");
      }

      return steeringCircuit;
   }

   public void onInitialize() {
      FabricDefaultAttributeRegistry.register(CONNECTOME_FLY, ConnectomeFlyEntity.createFlyAttributes());
      ItemGroupEvents.modifyEntriesEvent(class_7706.field_40205).register((ModifyEntries)entries -> entries.method_45421(CONNECTOME_FLY_SPAWN_EGG));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> FlyLabCommand.register(dispatcher));
      UseBlockCallback.EVENT
         .register(
            (UseBlockCallback)(player, world, hand, hit) -> {
               if (!world.field_9236) {
                  class_2680 st = world.method_8320(hit.method_17777());
                  if (st.method_27852(class_2246.field_10179) || st.method_26204() instanceof class_3709) {
                     FlyMind.noteSound(hit.method_17777(), world.method_8510() + 80L);
                  }

                  if (st.method_27852(class_2246.field_10057)) {
                     class_3218 sw = (class_3218)world;
                     ArenaBuilder.Arena box = ArenaBuilder.spawnButtonAt(sw, hit.method_17777());
                     if (box != null) {
                        int gone = FlyLabCommand.populate(sw, box.id(), ArenaBuilder.spawnSpot(box));
                        player.method_7353(
                           class_2561.method_43470(
                                 "box "
                                    + ArenaBuilder.SENSES.get(box.id())[0]
                                    + ": she is inside"
                                    + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)" : "")
                              )
                              .method_27692(class_124.field_1075),
                           false
                        );
                        player.method_7353(class_2561.method_43470(ArenaBuilder.instructions(box.id())).method_27692(class_124.field_1080), false);
                     }
                  }

                  if (st.method_27852(class_2246.field_10494)) {
                     ArenaBuilder.Arena arena = ArenaBuilder.arenaAt((class_3218)world, hit.method_17777());
                     if (arena != null && (arena.id().equals("escape") || arena.id().equals("looming"))) {
                        Iterator i$ = ((class_3218)world)
                           .method_18023(CONNECTOME_FLY, new class_238(hit.method_17777()).method_1014(12.0), e -> e.method_5805() && !e.isMale())
                           .iterator();
                        if (i$.hasNext()) {
                           ConnectomeFlyEntity f = (ConnectomeFlyEntity)i$.next();
                           LoomStimulus.spawn((class_3218)world, f);
                        }
                     }
                  }
               }

               return class_1269.field_5811;
            }
         );
      AttackBlockCallback.EVENT.register((AttackBlockCallback)(player, world, hand, pos, dir) -> {
         if (!world.field_9236 && world.method_8320(pos).method_27852(class_2246.field_10179)) {
            FlyMind.noteSound(pos, world.method_8510() + 80L);
         }

         return class_1269.field_5811;
      });
   }

   static {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         OptomotorDrum.tick();
         LoomStimulus.tick();
         WindFx.tick(server);
      });
   }
}
