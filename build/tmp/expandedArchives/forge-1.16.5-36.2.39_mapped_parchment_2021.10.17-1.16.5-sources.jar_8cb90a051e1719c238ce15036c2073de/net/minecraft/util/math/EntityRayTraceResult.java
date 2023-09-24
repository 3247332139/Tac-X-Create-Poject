package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class EntityRayTraceResult extends RayTraceResult {
   private final Entity entity;

   public EntityRayTraceResult(Entity pEntity) {
      this(pEntity, pEntity.position());
   }

   public EntityRayTraceResult(Entity pEntity, Vector3d pLocation) {
      super(pLocation);
      this.entity = pEntity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public RayTraceResult.Type getType() {
      return RayTraceResult.Type.ENTITY;
   }
}