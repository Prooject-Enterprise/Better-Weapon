package com.prooject.bettertrainingdummy.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;
import com.prooject.bettertrainingdummy.DummyComponent;

public class FixLegacyDummies extends HolderSystem<EntityStore> {
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      NPCEntity component = (NPCEntity)holder.getComponent(NPCEntity.getComponentType());
      if (component.getRoleName().equals("Dummy")) {
         holder.tryRemoveComponent(Interactable.getComponentType());
         holder.ensureComponent(DummyComponent.getComponentType());
      }

   }

   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }

   public Query<EntityStore> getQuery() {
      return NPCEntity.getComponentType();
   }
}
