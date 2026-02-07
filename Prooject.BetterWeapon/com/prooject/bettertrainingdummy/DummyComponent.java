package com.prooject.bettertrainingdummy;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;

public class DummyComponent implements Component<EntityStore> {
   public static final BuilderCodec<DummyComponent> CODEC = BuilderCodec.builder(DummyComponent.class, DummyComponent::new).build();
   private int numberOfHits = 0;
   private Instant lastHit;
   private String sourceItem = "Tinkering_Target_Dummy";

   public static ComponentType<EntityStore, DummyComponent> getComponentType() {
      return Main.getInstance().getDummyComponentType();
   }

   private DummyComponent() {
   }

   public DummyComponent(String sourceItem) {
      this.sourceItem = sourceItem;
   }

   public int getNumberOfHits() {
      return this.numberOfHits;
   }

   public void setNumberOfHits(int numberOfHits) {
      this.numberOfHits = numberOfHits;
   }

   public Instant getLastHit() {
      return this.lastHit;
   }

   public void setLastHit(Instant lastHit) {
      this.lastHit = lastHit;
   }

   public String getSourceItem() {
      return this.sourceItem;
   }

   public Component<EntityStore> clone() {
      return new DummyComponent(this.sourceItem);
   }
}
