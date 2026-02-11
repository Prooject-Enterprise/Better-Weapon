package com.prooject.RaiseTheArrow.ArrowMain;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.prooject.RaiseTheArrow.ArrowMain.Recoverarrows.ArrowMagnetSystem;
import javax.annotation.Nonnull;

public final class ArrowPickupPlugin extends JavaPlugin {
   public ArrowPickupPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      EntityStore.REGISTRY.registerSystem(new ArrowMagnetSystem());
   }
}
