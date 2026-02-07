package com.prooject.WeaponSignaturePreservation;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class SignaturePreservationConfig {
   public static final BuilderCodec<SignaturePreservationConfig> CODEC;
   private boolean enabled = true;
   private boolean debug = false;
   private long restoreDelayMs = 100L;

   public boolean isEnabled() { return this.enabled; }
   public void setEnabled(boolean enabled) { this.enabled = enabled; }
   public boolean isDebug() { return this.debug; }
   public void setDebug(boolean debug) { this.debug = debug; }
   public long getRestoreDelayMs() { return this.restoreDelayMs; }
   public void setRestoreDelayMs(long restoreDelayMs) { this.restoreDelayMs = restoreDelayMs; }

   @Override
   @Nonnull
   public String toString() {
      return "SignaturePreservationConfig{enabled=" + this.enabled + ", debug=" + this.debug + ", restoreDelayMs=" + this.restoreDelayMs + "}";
   }

   static {
      var builder = BuilderCodec.builder(SignaturePreservationConfig.class, SignaturePreservationConfig::new);
      
      builder.append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), 
         (config, value) -> ((SignaturePreservationConfig)config).enabled = (boolean)value, 
         (config) -> ((SignaturePreservationConfig)config).enabled).add();
         
      builder.append(new KeyedCodec<>("Debug", Codec.BOOLEAN), 
         (config, value) -> ((SignaturePreservationConfig)config).debug = (boolean)value, 
         (config) -> ((SignaturePreservationConfig)config).debug).add();
         
      builder.append(new KeyedCodec<>("RestoreDelayMs", Codec.LONG), 
         (config, value) -> ((SignaturePreservationConfig)config).restoreDelayMs = (long)value, 
         (config) -> ((SignaturePreservationConfig)config).restoreDelayMs).add();

      CODEC = builder.build();
   }
}