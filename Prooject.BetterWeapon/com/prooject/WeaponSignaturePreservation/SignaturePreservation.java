package com.prooject.WeaponSignaturePreservation;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import java.io.PrintStream;
import javax.annotation.Nonnull;

public class SignaturePreservation extends JavaPlugin {
   private final Config<SignaturePreservationConfig> config;

   public SignaturePreservation(@Nonnull JavaPluginInit init) {
      super(init);
      this.config = this.withConfig("SignaturePreservationConfig", SignaturePreservationConfig.CODEC);
   }

   protected void setup() {
      super.setup();
      this.config.save();
      SignaturePreservationConfig cfg = (SignaturePreservationConfig)this.config.get();
      SignatureEnergyPreservationSystem system = new SignatureEnergyPreservationSystem(cfg);
      this.getEntityStoreRegistry().registerSystem(system);
      System.out.println("[SP] ========================================");
      System.out.println("[SP] Signature Preservation mod loaded!");
      PrintStream var10000 = System.out;
      boolean var10001 = cfg.isEnabled();
      var10000.println("[SP] Config: enabled=" + var10001 + ", debug=" + cfg.isDebug());
      System.out.println("[SP] ========================================");
   }
}
