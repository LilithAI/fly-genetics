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
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public class ConnectomeFlyMod implements ModInitializer {
   public static final String MOD_ID = "connectomefly";
   public static final EntityType<ConnectomeFlyEntity> CONNECTOME_FLY = (EntityType<ConnectomeFlyEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("connectomefly", "connectome_fly"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ConnectomeFlyEntity::new)
         .dimensions(EntityDimensions.fixed(0.6F, 0.45F))
         .trackRangeBlocks(64)
         .build()
   );
   public static final EntityType<FlyEggEntity> FLY_EGG = (EntityType<FlyEggEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("connectomefly", "fly_egg"),
      FabricEntityTypeBuilder.create(SpawnGroup.MISC, FlyEggEntity::new).dimensions(EntityDimensions.fixed(0.3F, 0.25F)).trackRangeBlocks(48).build()
   );
   public static final Item CONNECTOME_FLY_SPAWN_EGG = (Item)Registry.register(
      Registries.ITEM, new Identifier("connectomefly", "connectome_fly_spawn_egg"), new SpawnEggItem(CONNECTOME_FLY, 14263361, 11740702, new Settings())
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
      ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((ModifyEntries)entries -> entries.add(CONNECTOME_FLY_SPAWN_EGG));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> FlyLabCommand.register(dispatcher));
      UseBlockCallback.EVENT
         .register(
            (UseBlockCallback)(player, world, hand, hit) -> {
               if (!world.isClient) {
                  BlockState st = world.getBlockState(hit.getBlockPos());
                  if (st.isOf(Blocks.NOTE_BLOCK) || st.getBlock() instanceof BellBlock) {
                     FlyMind.noteSound(hit.getBlockPos(), world.getTime() + 80L);
                  }

                  if (st.isOf(Blocks.OAK_BUTTON)) {
                     ServerWorld sw = (ServerWorld)world;
                     ArenaBuilder.Arena box = ArenaBuilder.spawnButtonAt(sw, hit.getBlockPos());
                     if (box != null) {
                        int gone = FlyLabCommand.populate(sw, box.id(), ArenaBuilder.spawnSpot(box));
                        player.sendMessage(
                           Text.literal(
                                 "box "
                                    + ArenaBuilder.SENSES.get(box.id())[0]
                                    + ": she is inside"
                                    + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)" : "")
                              )
                              .formatted(Formatting.AQUA),
                           false
                        );
                        player.sendMessage(Text.literal(ArenaBuilder.instructions(box.id())).formatted(Formatting.GRAY), false);
                     }
                  }

                  if (st.isOf(Blocks.STONE_BUTTON)) {
                     ArenaBuilder.Arena arena = ArenaBuilder.arenaAt((ServerWorld)world, hit.getBlockPos());
                     if (arena != null && (arena.id().equals("escape") || arena.id().equals("looming"))) {
                        Iterator i$ = ((ServerWorld)world)
                           .getEntitiesByType(CONNECTOME_FLY, new Box(hit.getBlockPos()).expand(12.0), e -> e.isAlive() && !e.isMale())
                           .iterator();
                        if (i$.hasNext()) {
                           ConnectomeFlyEntity f = (ConnectomeFlyEntity)i$.next();
                           LoomStimulus.spawn((ServerWorld)world, f);
                        }
                     }
                  }
               }

               return ActionResult.PASS;
            }
         );
      AttackBlockCallback.EVENT.register((AttackBlockCallback)(player, world, hand, pos, dir) -> {
         if (!world.isClient && world.getBlockState(pos).isOf(Blocks.NOTE_BLOCK)) {
            FlyMind.noteSound(pos, world.getTime() + 80L);
         }

         return ActionResult.PASS;
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
