package io.flyproject.connectomefly.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.flyproject.connectomefly.ConnectomeFlyMod;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ConnectomeFlyClient implements ClientModInitializer {
   private static boolean introPending;
   private static int introDelay;

   public void onInitializeClient() {
      EntityRendererRegistry.register(ConnectomeFlyMod.CONNECTOME_FLY, ConnectomeFlyRenderer::new);
      EntityRendererRegistry.register(ConnectomeFlyMod.FLY_EGG, FlyEggRenderer::new);
      FlyHud.register();
      FlyHud.loadSize();
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
               dispatcher.register(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal("flyhud")
                              .executes(ctx -> {
                                 FlyHud.enabled = !FlyHud.enabled;
                                 ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout " + (FlyHud.enabled ? "on" : "off")));
                                 return 1;
                              }))
                           .then(ClientCommands.literal("small").executes(ctx -> {
                              FlyHud.setSize("small");
                              FlyHud.enabled = true;
                              ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: small"));
                              return 1;
                           })))
                        .then(ClientCommands.literal("medium").executes(ctx -> {
                           FlyHud.setSize("medium");
                           FlyHud.enabled = true;
                           ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: medium"));
                           return 1;
                        })))
                     .then(ClientCommands.literal("big").executes(ctx -> {
                        FlyHud.setSize("big");
                        FlyHud.enabled = true;
                        ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("live readout: big"));
                        return 1;
                     }))
               );
               dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal("flycredits").executes(ctx -> {
                  Minecraft client = Minecraft.getInstance();
                  client.execute(() -> client.gui.setScreen(new CreditsScreen()));
                  return 1;
               }));
               dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal("flyintro").executes(ctx -> {
                  Minecraft client = Minecraft.getInstance();
                  client.execute(() -> client.gui.setScreen(new IntroScreen()));
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
               if (client.gui.screen() == null) {
                  introPending = false;

                  try {
                     Files.writeString(introMarker(), "shown");
                  } catch (Exception var2) {
                  }

                  client.gui.setScreen(new IntroScreen());
               }
            }
         }
      });
   }

   private static Path introMarker() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_intro_shown");
   }
}
