package com.prooject.bettertrainingdummy;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnDummyInteraction extends SimpleBlockInteraction {
   public static final BuilderCodec<SpawnDummyInteraction> CODEC;

   private void spawnNPC(World world, Store<EntityStore> store, InteractionContext interactionContext) {
      Ref<EntityStore> ownerRef = interactionContext.getOwningEntity();
      if (ownerRef.isValid()) {
         HeadRotation head = (HeadRotation)store.getComponent(ownerRef, HeadRotation.getComponentType());
         if (head != null) {
            Vector3f rotation = new Vector3f();
            rotation.setYaw((float)((double)head.getRotation().y + 3.141592653589793D));
            int roleIndex = NPCPlugin.get().getIndex("Dummy");
            if (roleIndex >= 0) {
               Vector3d targetLocation = TargetUtil.getTargetLocation(ownerRef, 8.0D, store);
               if (targetLocation != null) {
                  NPCPlugin.get().spawnEntity(store, roleIndex, targetLocation, rotation, (Model)null, (var0, holder, var2) -> {
                     holder.ensureComponent(DummyComponent.getComponentType());
                  }, (TriConsumer)null);
               }
            }
         }
      }
   }

   protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
      commandBuffer.run((store) -> {
         this.spawnNPC(world, store, context);
      });
   }

   protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      commandBuffer.run((store) -> {
         this.spawnNPC(world, store, context);
      });
   }

   static {
      CODEC = BuilderCodec.builder(SpawnDummyInteraction.class, SpawnDummyInteraction::new, SimpleBlockInteraction.CODEC).build();
   }
}
