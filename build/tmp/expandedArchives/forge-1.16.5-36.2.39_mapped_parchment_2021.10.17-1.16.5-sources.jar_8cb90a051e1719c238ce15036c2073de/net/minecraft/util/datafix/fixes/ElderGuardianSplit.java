package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ElderGuardianSplit extends EntityRenameHelper {
   public ElderGuardianSplit(Schema p_i49668_1_, boolean p_i49668_2_) {
      super("EntityElderGuardianSplitFix", p_i49668_1_, p_i49668_2_);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String pName, Dynamic<?> pTag) {
      return Pair.of(Objects.equals(pName, "Guardian") && pTag.get("Elder").asBoolean(false) ? "ElderGuardian" : pName, pTag);
   }
}