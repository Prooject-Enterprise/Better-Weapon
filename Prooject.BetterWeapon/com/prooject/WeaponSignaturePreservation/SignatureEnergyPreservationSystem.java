package com.prooject.WeaponSignaturePreservation;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SignatureEnergyPreservationSystem extends EntityTickingSystem<EntityStore> {
   public static final String META_KEY_SIGNATURE_ENERGY = "SP_SavedSignatureEnergy";
   private final SignaturePreservationConfig config;
   private final Map<UUID, Byte> lastActiveSlot = new ConcurrentHashMap();
   private final Map<UUID, Float> previousTickEnergy = new ConcurrentHashMap();
   private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

   public SignatureEnergyPreservationSystem(@Nonnull SignaturePreservationConfig config) {
      this.config = config;
   }

   private void debug(@Nonnull String message) {
      if (this.config.isDebug()) {
         System.out.println("[SIGPREV:DEBUG] " + message);
      }

   }

   @Nullable
   public Query<EntityStore> getQuery() {
      return Player.getComponentType();
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (this.config.isEnabled()) {
         Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
         if (entityRef.isValid()) {
            Player player = (Player)archetypeChunk.getComponent(index, Player.getComponentType());
            if (player != null) {
               Inventory inventory = player.getInventory();
               if (inventory != null) {
                  UUIDComponent uuidComponent = (UUIDComponent)store.getComponent(entityRef, UUIDComponent.getComponentType());
                  if (uuidComponent != null) {
                     UUID playerUuid = uuidComponent.getUuid();
                     byte currentSlot = inventory.getActiveHotbarSlot();
                     float currentEnergy = this.getSignatureEnergy(entityRef, store);
                     Byte lastSlotObj = (Byte)this.lastActiveSlot.get(playerUuid);
                     if (lastSlotObj == null) {
                        this.lastActiveSlot.put(playerUuid, currentSlot);
                        this.previousTickEnergy.put(playerUuid, currentEnergy);
                        this.debug(String.format("Player first tick - initial slot: %d, energy: %.1f", currentSlot, currentEnergy));
                     } else {
                        byte previousSlot = lastSlotObj;
                        if (currentSlot == previousSlot) {
                           this.previousTickEnergy.put(playerUuid, currentEnergy);
                        } else {
                           Float energyBeforeReset = (Float)this.previousTickEnergy.get(playerUuid);
                           float savedEnergy = energyBeforeReset != null ? energyBeforeReset : 0.0F;
                           this.lastActiveSlot.put(playerUuid, currentSlot);
                           this.previousTickEnergy.put(playerUuid, currentEnergy);
                           this.debug(String.format("Hotbar slot change detected: %d -> %d (energy before reset: %.1f)", previousSlot, currentSlot, savedEnergy));
                           this.handleSlotChange(entityRef, store, inventory, previousSlot, currentSlot, savedEnergy);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void handleSlotChange(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store, @Nonnull Inventory inventory, byte previousSlot, byte currentSlot, float energyBeforeReset) {
      ItemContainer hotbar = inventory.getHotbar();
      ItemStack oldItem = hotbar.getItemStack((short)previousSlot);
      ItemStack newItem = hotbar.getItemStack((short)currentSlot);
      this.debug(String.format("Hotbar swap: slot %d -> %d | Energy before reset: %.1f", previousSlot, currentSlot, energyBeforeReset));
      this.debug(String.format("Old item: %s | New item: %s", oldItem != null ? oldItem.getItem().getId() : "null", newItem != null ? newItem.getItem().getId() : "null"));
      boolean oldIsWeapon = oldItem != null && !oldItem.isEmpty() && this.isWeapon(oldItem);
      this.debug(String.format("Old item is weapon: %b", oldIsWeapon));
      if (oldIsWeapon) {
         if (energyBeforeReset > 0.0F) {
            ItemStack updatedOldItem = this.saveSignatureEnergy(oldItem, energyBeforeReset);
            hotbar.setItemStackForSlot((short)previousSlot, updatedOldItem);
            this.debug(String.format("SAVED SignatureEnergy %.1f to old weapon in slot %d", energyBeforeReset, previousSlot));
         } else {
            this.debug("Skipping save - energy before reset is 0");
         }
      }

      boolean newIsWeapon = newItem != null && !newItem.isEmpty() && this.isWeapon(newItem);
      this.debug(String.format("New item is weapon: %b", newIsWeapon));
      if (newIsWeapon) {
         Float savedEnergy = this.getSavedSignatureEnergy(newItem);
         this.debug(String.format("Saved energy in new weapon metadata: %s", savedEnergy));
         if (savedEnergy != null && savedEnergy > 0.0F) {
            ItemStack updatedNewItem = this.clearSavedSignatureEnergy(newItem);
            hotbar.setItemStackForSlot((short)currentSlot, updatedNewItem);
            this.debug("Cleared saved energy from new weapon metadata");
            World world = ((EntityStore)store.getExternalData()).getWorld();
            float energyToRestore = savedEnergy;
            long delayMs = this.config.getRestoreDelayMs();
            this.debug(String.format("Scheduling restore of %.1f energy in %dms", energyToRestore, delayMs));
            this.scheduler.schedule(() -> {
               this.debug(String.format("Scheduler fired - about to restore %.1f", energyToRestore));
               world.execute(() -> {
                  this.debug(String.format("world.execute() running - restoring %.1f", energyToRestore));
                  this.setSignatureEnergy(entityRef, store, energyToRestore);
                  this.debug(String.format("RESTORED SignatureEnergy %.1f for weapon in slot %d (after %dms delay)", energyToRestore, currentSlot, delayMs));
               });
            }, delayMs, TimeUnit.MILLISECONDS);
         } else {
            this.debug("No saved energy to restore (null or 0)");
         }
      } else {
         this.debug("New item is not a weapon - no restore needed");
      }

   }

   private boolean isWeapon(@Nullable ItemStack item) {
      if (item != null && !item.isEmpty()) {
         return item.getItem().getWeapon() != null;
      } else {
         return false;
      }
   }

   public void cleanupPlayer(@Nonnull UUID playerUuid) {
      this.lastActiveSlot.remove(playerUuid);
      this.previousTickEnergy.remove(playerUuid);
   }

   private float getSignatureEnergy(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
      int signatureEnergyIndex = EntityStatType.getAssetMap().getIndex("SignatureEnergy");
      if (signatureEnergyIndex == Integer.MIN_VALUE) {
         return 0.0F;
      } else {
         EntityStatMap statMap = (EntityStatMap)store.getComponent(entityRef, EntityStatMap.getComponentType());
         if (statMap == null) {
            return 0.0F;
         } else {
            EntityStatValue statValue = statMap.get(signatureEnergyIndex);
            return statValue != null ? statValue.get() : 0.0F;
         }
      }
   }

   private void setSignatureEnergy(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store, float value) {
      int signatureEnergyIndex = EntityStatType.getAssetMap().getIndex("SignatureEnergy");
      if (signatureEnergyIndex == Integer.MIN_VALUE) {
         this.debug("setSignatureEnergy FAILED: SignatureEnergy stat not found!");
      } else if (!entityRef.isValid()) {
         this.debug("setSignatureEnergy FAILED: entityRef is no longer valid!");
      } else {
         EntityStatMap statMap = (EntityStatMap)store.getComponent(entityRef, EntityStatMap.getComponentType());
         if (statMap == null) {
            this.debug("setSignatureEnergy FAILED: statMap is null!");
         } else {
            statMap.setStatValue(signatureEnergyIndex, value);
            if (this.config.isDebug()) {
               EntityStatValue verify = statMap.get(signatureEnergyIndex);
               float verifyValue = verify != null ? verify.get() : -1.0F;
               this.debug(String.format("setSignatureEnergy: set %.1f, verify read back: %.1f", value, verifyValue));
            }

         }
      }
   }

   @Nonnull
   private ItemStack saveSignatureEnergy(@Nonnull ItemStack item, float energy) {
      return item.withMetadata("SP_SavedSignatureEnergy", Codec.FLOAT, energy);
   }

   @Nullable
   private Float getSavedSignatureEnergy(@Nonnull ItemStack item) {
      return (Float)item.getFromMetadataOrNull("SP_SavedSignatureEnergy", Codec.FLOAT);
   }

   @Nonnull
   private ItemStack clearSavedSignatureEnergy(@Nonnull ItemStack item) {
      return item.withMetadata("SP_SavedSignatureEnergy", Codec.FLOAT, 0.0F);
   }
}
