package com.prooject.bettertrainingdummy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer.ItemContainerChangeEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import com.prooject.bettertrainingdummy.systems.DestroyDummy;
import com.prooject.bettertrainingdummy.systems.FixLegacyDummies;
import com.prooject.bettertrainingdummy.systems.PreventWeaponDamage;

public class Main extends JavaPlugin {
   private static Main instance;
   private ComponentType<EntityStore, DummyComponent> dummyComponentType;

   public static Main getInstance() {
      return instance;
   }

   public Main(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public ComponentType<EntityStore, DummyComponent> getDummyComponentType() {
      return this.dummyComponentType;
   }

   protected void setup() {
      instance = this;
      this.dummyComponentType = this.getEntityStoreRegistry().registerComponent(DummyComponent.class, "Dummy", DummyComponent.CODEC);
      this.getEntityStoreRegistry().registerSystem(new FixLegacyDummies());
      this.getEntityStoreRegistry().registerSystem(new DestroyDummy());
      this.getEntityStoreRegistry().registerSystem(new PreventWeaponDamage());
      this.getEventRegistry().registerGlobal(ItemContainerChangeEvent.class, Main::onContainerChange);
      Interaction.CODEC.register("Drex_SpawnNPC", SpawnDummyInteraction.class, SpawnDummyInteraction.CODEC);
   }

   private static void onContainerChange(ItemContainerChangeEvent event) {
      System.out.println("onContainChange");
   }
}
