package com.prooject.bettertrainingdummy.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.EntitySource;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.Source;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;
import com.prooject.bettertrainingdummy.DummyComponent;

public class PreventWeaponDamage extends DamageEventSystem {
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
      Source var7 = damage.getSource();
      if (var7 instanceof EntitySource) {
         EntitySource source = (EntitySource)var7;
         Ref sourceRef = source.getRef();
         Player player = sourceRef.isValid() ? (Player)commandBuffer.getComponent(sourceRef, Player.getComponentType()) : null;
         if (player != null) {
            Inventory inventory = player.getInventory();
            ItemStack itemInHand = inventory.getItemInHand();
            if (itemInHand != null) {
               DamageCause cause = damage.getCause();
               if (cause != null && cause.isDurabilityLoss()) {
                  Item item = itemInHand.getItem();
                  if (item.getWeapon() != null && player.canDecreaseItemStackDurability(sourceRef, commandBuffer)) {
                     player.updateItemStackDurability(sourceRef, itemInHand, inventory.getHotbar(), inventory.getActiveHotbarSlot(), item.getDurabilityLossOnHit(), commandBuffer);
                  }
               }
            }

         }
      }
   }

   static {
      DEPENDENCIES = Set.of(new SystemGroupDependency(Order.AFTER, DamageModule.get().getGatherDamageGroup()), new SystemGroupDependency(Order.AFTER, DamageModule.get().getFilterDamageGroup()), new SystemGroupDependency(Order.AFTER, DamageModule.get().getInspectDamageGroup()));
   }
}
