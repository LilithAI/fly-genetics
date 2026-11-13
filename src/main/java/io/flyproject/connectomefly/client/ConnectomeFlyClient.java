package io.flyproject.connectomefly.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.flyproject.connectomefly.ConnectomeFlyMod;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ConnectomeFlyClient implements ClientModInitializer {
   public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("connectomefly", "connectome_fly"), "main");
   private static boolean introPending;
   private static int introDelay;

   public void onInitializeClient() {
      EntityModelLayerRegistry.registerModelLayer(MODEL_LAYER, ConnectomeFlyModel::getTexturedModelData);
      EntityRendererRegistry.register(ConnectomeFlyMod.CONNECTOME_FLY, ConnectomeFlyRenderer::new);
      EntityRendererRegistry.register(ConnectomeFlyMod.FLY_EGG, FlyEggRenderer::new);
      FlyHud.register();
      FlyHud.loadSize();
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("flyhud")
                              .executes(ctx -> {
                                 FlyHud.enabled = !FlyHud.enabled;
                                 ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout " + (FlyHud.enabled ? "on" : "off")));
                                 return 1;
                              }))
                           .then(ClientCommandManager.literal("small").executes(ctx -> {
                              FlyHud.setSize("small");
                              FlyHud.enabled = true;
                              ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: small"));
                              return 1;
                           })))
                        .then(ClientCommandManager.literal("medium").executes(ctx -> {
                           FlyHud.setSize("medium");
                           FlyHud.enabled = true;
                           ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: medium"));
                           return 1;
                        })))
                     .then(ClientCommandManager.literal("big").executes(ctx -> {
                        FlyHud.setSize("big");
                        FlyHud.enabled = true;
                        ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: big"));
                        return 1;
                     }))
               );
               dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal("flycredits").executes(ctx -> {
                  Minecraft client = Minecraft.getInstance();
                  client.tell(() -> client.setScreen(new CreditsScreen()));
                  return 1;
               }));
               dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal("flyintro").executes(ctx -> {
                  Minecraft client = Minecraft.getInstance();
                  client.tell(() -> client.setScreen(new IntroScreen()));
                  return 1;
               }));
            }
         );
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> {
         if (!Files.exists(introMarker())) {
            introPending = true;
            introDelay = 50;
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (introPending && client.player != null) {
            if (introDelay > 0) {
               introDelay--;
            } else {
               if (client.screen == null) {
                  introPending = false;

                  try {
                     Files.writeString(introMarker(), "shown");
                  } catch (Exception var2) {
                  }

                  client.setScreen(new IntroScreen());
               }
            }
         }
      });
   }

   private static Path introMarker() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_intro_shown");
   }
}
