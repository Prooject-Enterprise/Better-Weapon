package com.prooject.bettertrainingdummy.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.EntitySource;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.Source;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import javax.annotation.Nonnull;
import com.prooject.bettertrainingdummy.DummyComponent;

public class DestroyDummy extends DamageEventSystem {
   private static final Duration HIT_RESET_TIME = Duration.ofSeconds(10L);
   private static final int NUMBER_OF_HITS = 3;
   @Nonnull
   private static final Query<EntityStore> QUERY = Archetype.of(new ComponentType[]{DummyComponent.getComponentType(), TransformComponent.getComponentType()});
   @Nonnull
   private static final Set<Dependency<EntityStore>> DEPENDENCIES;

   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Nonnull
   public Set<Dependency<EntityStore>> getDependencies() {
      return DEPENDENCIES;
   }

   public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
      DummyComponent dummyComponent = (DummyComponent)archetypeChunk.getComponent(index, DummyComponent.getComponentType());

      assert dummyComponent != null;

      Instant currentTime = ((TimeResource)commandBuffer.getResource(TimeResource.getResourceType())).getNow();
      if (dummyComponent.getLastHit() != null && currentTime.isAfter(dummyComponent.getLastHit().plus(HIT_RESET_TIME))) {
         dummyComponent.setLastHit((Instant)null);
         dummyComponent.setNumberOfHits(0);
      }

      if (!(damage.getAmount() <= 0.0F)) {
         dummyComponent.setNumberOfHits(dummyComponent.getNumberOfHits() + 1);
         dummyComponent.setLastHit(currentTime);
         boolean shouldDropItem = true;
         Source var10 = damage.getSource();
         if (var10 instanceof EntitySource) {
            EntitySource source = (EntitySource)var10;
            Ref<EntityStore> sourceRef = source.getRef();
            Player player = sourceRef.isValid() ? (Player)commandBuffer.getComponent(sourceRef, Player.getComponentType()) : null;
            if (player != null) {
               shouldDropItem = player.getGameMode() != GameMode.Creative;
               Inventory inventory = player.getInventory();
               ItemStack itemInHand = inventory.getItemInHand();
               if (itemInHand != null) {
                  dummyComponent.setLastHit((Instant)null);
                  dummyComponent.setNumberOfHits(0);
                  return;
               }
            }
         }

         if (dummyComponent.getNumberOfHits() >= 3) {
            if (shouldDropItem && dummyComponent.getSourceItem() != null) {
               TransformComponent transform = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());

               assert transform != null;

               Holder<EntityStore> drop = ItemComponent.generateItemDrop(commandBuffer, new ItemStack(dummyComponent.getSourceItem()), transform.getPosition(), transform.getRotation(), 0.0F, 1.0F, 0.0F);
               if (drop != null) {
                  commandBuffer.addEntity(drop, AddReason.SPAWN);
               }
            }

            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }

   }

   static {
      DEPENDENCIES = Set.of(new SystemGroupDependency(Order.AFTER, DamageModule.get().getGatherDamageGroup()), new SystemGroupDependency(Order.AFTER, DamageModule.get().getFilterDamageGroup()), new SystemGroupDependency(Order.BEFORE, DamageModule.get().getInspectDamageGroup()));
   }
}
