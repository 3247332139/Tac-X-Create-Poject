package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class LocationInput implements ILocationArgument {
   private final LocationPart x;
   private final LocationPart y;
   private final LocationPart z;

   public LocationInput(LocationPart pX, LocationPart pY, LocationPart pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public Vector3d getPosition(CommandSource pSource) {
      Vector3d vector3d = pSource.getPosition();
      return new Vector3d(this.x.get(vector3d.x), this.y.get(vector3d.y), this.z.get(vector3d.z));
   }

   public Vector2f getRotation(CommandSource pSource) {
      Vector2f vector2f = pSource.getRotation();
      return new Vector2f((float)this.x.get((double)vector2f.x), (float)this.y.get((double)vector2f.y));
   }

   public boolean isXRelative() {
      return this.x.isRelative();
   }

   public boolean isYRelative() {
      return this.y.isRelative();
   }

   public boolean isZRelative() {
      return this.z.isRelative();
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof LocationInput)) {
         return false;
      } else {
         LocationInput locationinput = (LocationInput)p_equals_1_;
         if (!this.x.equals(locationinput.x)) {
            return false;
         } else {
            return !this.y.equals(locationinput.y) ? false : this.z.equals(locationinput.z);
         }
      }
   }

   public static LocationInput parseInt(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();
      LocationPart locationpart = LocationPart.parseInt(pReader);
      if (pReader.canRead() && pReader.peek() == ' ') {
         pReader.skip();
         LocationPart locationpart1 = LocationPart.parseInt(pReader);
         if (pReader.canRead() && pReader.peek() == ' ') {
            pReader.skip();
            LocationPart locationpart2 = LocationPart.parseInt(pReader);
            return new LocationInput(locationpart, locationpart1, locationpart2);
         } else {
            pReader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
         }
      } else {
         pReader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
      }
   }

   public static LocationInput parseDouble(StringReader pReader, boolean pCenterCorrect) throws CommandSyntaxException {
      int i = pReader.getCursor();
      LocationPart locationpart = LocationPart.parseDouble(pReader, pCenterCorrect);
      if (pReader.canRead() && pReader.peek() == ' ') {
         pReader.skip();
         LocationPart locationpart1 = LocationPart.parseDouble(pReader, false);
         if (pReader.canRead() && pReader.peek() == ' ') {
            pReader.skip();
            LocationPart locationpart2 = LocationPart.parseDouble(pReader, pCenterCorrect);
            return new LocationInput(locationpart, locationpart1, locationpart2);
         } else {
            pReader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
         }
      } else {
         pReader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
      }
   }

   /**
    * A location with a delta of 0 for all values (equivalent to <code>~ ~ ~</code> or <code>~0 ~0 ~0</code>)
    */
   public static LocationInput current() {
      return new LocationInput(new LocationPart(true, 0.0D), new LocationPart(true, 0.0D), new LocationPart(true, 0.0D));
   }

   public int hashCode() {
      int i = this.x.hashCode();
      i = 31 * i + this.y.hashCode();
      return 31 * i + this.z.hashCode();
   }
}