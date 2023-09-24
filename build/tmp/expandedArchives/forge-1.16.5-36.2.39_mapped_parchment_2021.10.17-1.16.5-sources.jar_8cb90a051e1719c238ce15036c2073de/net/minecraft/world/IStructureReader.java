package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

public interface IStructureReader {
   @Nullable
   StructureStart<?> getStartForFeature(Structure<?> pStructure);

   void setStartForFeature(Structure<?> pStructure, StructureStart<?> pStart);

   LongSet getReferencesForFeature(Structure<?> pStructure);

   void addReferenceForFeature(Structure<?> pStructure, long pChunkValue);

   Map<Structure<?>, LongSet> getAllReferences();

   void setAllReferences(Map<Structure<?>, LongSet> pStructureReferences);
}