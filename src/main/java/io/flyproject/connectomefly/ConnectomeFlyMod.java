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
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ConnectomeFlyMod implements ModInitializer {
   public static final String MOD_ID = "connectomefly";
   private static final ResourceKey<EntityType<?>> FLY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("connectomefly", "connectome_fly"));
   private static final ResourceKey<EntityType<?>> EGG_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("connectomefly", "fly_egg"));
   private static final ResourceKey<Item> SPAWN_EGG_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("connectomefly", "connectome_fly_spawn_egg"));
   public static final EntityType<ConnectomeFlyEntity> CONNECTOME_FLY = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      FLY_KEY,
      EntityType.Builder.<ConnectomeFlyEntity>of(ConnectomeFlyEntity::new, MobCategory.CREATURE).sized(0.6F, 0.45F).clientTrackingRange(4).build(FLY_KEY)
   );
   public static final EntityType<FlyEggEntity> FLY_EGG = Registry.register(
      BuiltInRegistries.ENTITY_TYPE, EGG_KEY, EntityType.Builder.<FlyEggEntity>of(FlyEggEntity::new, MobCategory.MISC).sized(0.3F, 0.25F).clientTrackingRange(3).build(EGG_KEY)
   );
   public static final Item CONNECTOME_FLY_SPAWN_EGG = Registry.register(
      BuiltInRegistries.ITEM, SPAWN_EGG_KEY, new SpawnEggItem(new Properties().spawnEgg(CONNECTOME_FLY).setId(SPAWN_EGG_KEY))
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
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(CONNECTOME_FLY_SPAWN_EGG));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         FlyLabCommand.register(dispatcher);
         io.flyproject.connectomefly.genetics.GeneticsCommand.register(dispatcher);
      });
      UseBlockCallback.EVENT
         .register(
            (UseBlockCallback)(player, world, hand, hit) -> {
               if (!world.isClientSide()) {
                  BlockState st = world.getBlockState(hit.getBlockPos());
                  if (st.is(Blocks.NOTE_BLOCK) || st.getBlock() instanceof BellBlock) {
                     FlyMind.noteSound(hit.getBlockPos(), world.getGameTime() + 80L);
                  }

                  if (st.is(Blocks.OAK_BUTTON)) {
                     ServerLevel sw = (ServerLevel)world;
                     ArenaBuilder.Arena box = ArenaBuilder.spawnButtonAt(sw, hit.getBlockPos());
                     if (box != null) {
                        int gone = FlyLabCommand.populate(sw, box.id(), ArenaBuilder.spawnSpot(box));
                        player.sendSystemMessage(
                           Component.literal(
                                 "box "
                                    + ArenaBuilder.SENSES.get(box.id())[0]
                                    + ": she is inside"
                                    + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)" : "")
                              )
                              .withStyle(ChatFormatting.AQUA)
                        );
                        player.sendSystemMessage(Component.literal(ArenaBuilder.instructions(box.id())).withStyle(ChatFormatting.GRAY));
                     }
                  }

                  if (st.is(Blocks.STONE_BUTTON)) {
                     ArenaBuilder.Arena arena = ArenaBuilder.arenaAt((ServerLevel)world, hit.getBlockPos());
                     if (arena != null && (arena.id().equals("escape") || arena.id().equals("looming"))) {
                        Iterator i$ = ((ServerLevel)world)
                           .getEntities(CONNECTOME_FLY, new AABB(hit.getBlockPos()).inflate(12.0), e -> e.isAlive() && !e.isMale())
                           .iterator();
                        if (i$.hasNext()) {
                           ConnectomeFlyEntity f = (ConnectomeFlyEntity)i$.next();
                           LoomStimulus.spawn((ServerLevel)world, f);
                        }
                     }
                  }
               }

               return InteractionResult.PASS;
            }
         );
      AttackBlockCallback.EVENT.register((AttackBlockCallback)(player, world, hand, pos, dir) -> {
         if (!world.isClientSide() && world.getBlockState(pos).is(Blocks.NOTE_BLOCK)) {
            FlyMind.noteSound(pos, world.getGameTime() + 80L);
         }

         return InteractionResult.PASS;
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
