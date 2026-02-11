package com.prooject.RaiseTheArrow.ArrowMain.Recoverarrows;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.HashMap;
import java.util.Map;

public final class ArrowTracker {
   private static final double POSITION_EPSILON = 0.001D;
   private static final double POSITION_EPSILON_SQUARED = 1.0E-6D;
   private final Map<Ref<?>, ArrowTracker.TrackedArrow> tracked = new HashMap();
   private final int requiredStationaryTicks;
   private final int pickupDelayTicks;

   public ArrowTracker(int requiredStationaryTicks, int pickupDelayTicks) {
      this.requiredStationaryTicks = requiredStationaryTicks;
      this.pickupDelayTicks = pickupDelayTicks;
   }

   public void trackIfMissing(Ref<?> arrowRef, Vector3d currentPos) {
      this.tracked.computeIfAbsent(arrowRef, (r) -> {
         return new ArrowTracker.TrackedArrow(r, currentPos);
      });
   }

   public void untrack(Ref<?> arrowRef) {
      this.tracked.remove(arrowRef);
   }

   public ArrowTracker.TrackedArrow get(Ref<?> arrowRef) {
      return (ArrowTracker.TrackedArrow)this.tracked.get(arrowRef);
   }

   public boolean isStopped(ArrowTracker.TrackedArrow a) {
      return a != null && a.stationaryTicks >= this.requiredStationaryTicks;
   }

   public boolean isPickupReady(ArrowTracker.TrackedArrow a, long tick) {
      if (a == null) {
         return false;
      } else if (!this.isStopped(a)) {
         return false;
      } else if (a.stoppedTick < 0L) {
         return false;
      } else {
         return tick - a.stoppedTick >= (long)this.pickupDelayTicks;
      }
   }

   public void update(ArrowTracker.TrackedArrow a, Vector3d currentPos, long tick) {
      if (a != null) {
         if (currentPos != null && a.lastPos != null) {
            double d2 = dist2(currentPos, a.lastPos);
            if (d2 <= 1.0E-6D) {
               ++a.stationaryTicks;
               if (a.stationaryTicks >= this.requiredStationaryTicks && a.stoppedTick < 0L) {
                  a.stoppedTick = tick;
               }
            } else {
               a.stationaryTicks = 0;
               a.stoppedTick = -1L;
            }

            a.lastPos = copy(currentPos);
         } else {
            a.lastPos = copy(currentPos);
            a.stationaryTicks = 0;
            a.stoppedTick = -1L;
         }
      }
   }

   private static double dist2(Vector3d a, Vector3d b) {
      double dx = a.getX() - b.getX();
      double dy = a.getY() - b.getY();
      double dz = a.getZ() - b.getZ();
      return dx * dx + dy * dy + dz * dz;
   }

   private static Vector3d copy(Vector3d p) {
      return p == null ? null : new Vector3d(p.getX(), p.getY(), p.getZ());
   }

   public static final class TrackedArrow {
      public final Ref<?> arrowRef;
      public Vector3d lastPos;
      public int stationaryTicks;
      public long stoppedTick;

      public TrackedArrow(Ref<?> arrowRef, Vector3d initialPos) {
         this.arrowRef = arrowRef;
         this.lastPos = ArrowTracker.copy(initialPos);
         this.stationaryTicks = 0;
         this.stoppedTick = -1L;
      }
   }
}
