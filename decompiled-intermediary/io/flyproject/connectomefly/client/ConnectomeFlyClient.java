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
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5601;

public class ConnectomeFlyClient implements ClientModInitializer {
   public static final class_5601 MODEL_LAYER = new class_5601(new class_2960("connectomefly", "connectome_fly"), "main");
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
                                 ((FabricClientCommandSource)ctx.getSource())
                                    .sendFeedback(class_2561.method_43470("live readout " + (FlyHud.enabled ? "on" : "off")));
                                 return 1;
                              }))
                           .then(ClientCommandManager.literal("small").executes(ctx -> {
                              FlyHud.setSize("small");
                              FlyHud.enabled = true;
                              ((FabricClientCommandSource)ctx.getSource()).sendFeedback(class_2561.method_43470("live readout: small"));
                              return 1;
                           })))
                        .then(ClientCommandManager.literal("medium").executes(ctx -> {
                           FlyHud.setSize("medium");
                           FlyHud.enabled = true;
                           ((FabricClientCommandSource)ctx.getSource()).sendFeedback(class_2561.method_43470("live readout: medium"));
                           return 1;
                        })))
                     .then(ClientCommandManager.literal("big").executes(ctx -> {
                        FlyHud.setSize("big");
                        FlyHud.enabled = true;
                        ((FabricClientCommandSource)ctx.getSource()).sendFeedback(class_2561.method_43470("live readout: big"));
                        return 1;
                     }))
               );
               dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal("flycredits").executes(ctx -> {
                  class_310 client = class_310.method_1551();
                  client.method_18858(() -> client.method_1507(new CreditsScreen()));
                  return 1;
               }));
               dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal("flyintro").executes(ctx -> {
                  class_310 client = class_310.method_1551();
                  client.method_18858(() -> client.method_1507(new IntroScreen()));
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
         if (introPending && client.field_1724 != null) {
            if (introDelay > 0) {
               introDelay--;
            } else {
               if (client.field_1755 == null) {
                  introPending = false;

                  try {
                     Files.writeString(introMarker(), "shown");
                  } catch (Exception var2) {
                  }

                  client.method_1507(new IntroScreen());
               }
            }
         }
      });
   }

   private static Path introMarker() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_intro_shown");
   }
}
