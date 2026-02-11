package com.prooject.RaiseTheArrow.ArrowMain.Recoverarrows;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

public final class ArrowMagnetSystem extends EntityTickingSystem<EntityStore> {
   private static final String ALLOWED_ARROW_ITEM = "Weapon_Arrow_Crude";
   private static final double VELOCITY_EPSILON = 0.001D;
   private static final int CLEANUP_INTERVAL_TICKS = 200;
   private static final HytaleLogger LOG = HytaleLogger.get("ArrowPickup");
   private final Map<Ref<EntityStore>, Vector3d> lastPositions = new HashMap();
   private int tickCounter = 0;

   public Query<EntityStore> getQuery() {
      return Projectile.getComponentType();
   }

   public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commands) {
      ++this.tickCounter;
      if (this.tickCounter >= 200) {
         this.tickCounter = 0;
         this.cleanupStaleReferences(store);
      }

      Ref<EntityStore> ref = chunk.getReferenceTo(index);
      TransformComponent tx = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
      Velocity vel = (Velocity)store.getComponent(ref, Velocity.getComponentType());
      ModelComponent model = (ModelComponent)store.getComponent(ref, ModelComponent.getComponentType());
      if (tx != null && vel != null && model != null && model.getModel() != null) {
         String modelAssetId = model.getModel().getModelAssetId();
         if (modelAssetId != null && modelAssetId.contains("Arrow_")) {
            Vector3d pos = tx.getPosition();
            Vector3d last = (Vector3d)this.lastPositions.get(ref);
            Vector3f velocity = vel.getVelocity().toVector3f();
            if (last == null || !last.equals(pos) || velocity != null && (double)velocity.length() > 0.001D) {
               this.lastPositions.put(ref, pos.clone());
            } else {
               int idx = modelAssetId.lastIndexOf("Arrow_");
               if (idx == -1) {
                  this.lastPositions.remove(ref);
               } else {
                  String arrowSuffix = modelAssetId.substring(idx);
                  String itemId = "Weapon_" + arrowSuffix;
                  if (!"Weapon_Arrow_Crude".equals(itemId)) {
                     LOG.at(Level.FINE).log("Skipping non-supported arrow type: %s", itemId);
                     this.lastPositions.remove(ref);
                  } else {
                     ItemStack arrow = new ItemStack("Weapon_Arrow_Crude", 1);
                     Holder<EntityStore> holder = ItemComponent.generateItemDrop(commands, arrow, pos, Vector3f.ZERO, 0.0F, 0.0F, 0.0F);
                     if (holder == null) {
                        LOG.at(Level.WARNING).log("Failed to spawn arrow pickup for %s", "Weapon_Arrow_Crude");
                        this.lastPositions.remove(ref);
                     } else {
                        ItemComponent itemComponent = (ItemComponent)holder.getComponent(ItemComponent.getComponentType());
                        if (itemComponent != null) {
                           itemComponent.setPickupDelay(1.0F);
                        }

                        commands.addEntity(holder, AddReason.SPAWN);
                        commands.removeEntity(ref, RemoveReason.REMOVE);
                        this.lastPositions.remove(ref);
                        LOG.at(Level.FINE).log("Recovered arrow at %s", pos);
                     }
                  }
               }
            }
         } else {
            this.lastPositions.remove(ref);
         }
      } else {
         this.lastPositions.remove(ref);
      }
   }

   private void cleanupStaleReferences(Store<EntityStore> store) {
      Iterator iterator = this.lastPositions.keySet().iterator();

      while(iterator.hasNext()) {
         Ref<EntityStore> ref = (Ref)iterator.next();
         if (!ref.isValid()) {
            iterator.remove();
            LOG.at(Level.FINER).log("Cleaned up invalid arrow reference");
         } else {
            try {
               TransformComponent tx = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
               if (tx == null) {
                  iterator.remove();
                  LOG.at(Level.FINER).log("Cleaned up stale arrow reference");
               }
            } catch (IllegalStateException var5) {
               iterator.remove();
               LOG.at(Level.FINER).log("Cleaned up arrow reference (entity removed)");
            }
         }
      }

   }
}
