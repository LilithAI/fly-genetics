package io.flyproject.connectomefly.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

/** 26.3 port: everything the fly renderer needs, copied from the entity before drawing. */
public class FlyRenderState extends EntityRenderState {
   public byte bodyMode;
   public boolean male;
   public boolean rigged;
   public FlyAnim anim;
   public int lod;
   public float yaw;
   public Direction surface;
   public float scale = 1.0F;
   public float deathTime;
   public boolean hurt;
   public boolean asleep;
   public Object mobRenderer;
   public Object mobState;
   public String speech = "";
   public boolean showBars;
   public boolean showSpeech;
   public float health;
   public float maxHealth;
   public float hunger;
   // bee body
   public boolean onGround;
   public float walkPos;
   public float walkSpeed;
   public float headYaw;
   public float headPitch;
}
