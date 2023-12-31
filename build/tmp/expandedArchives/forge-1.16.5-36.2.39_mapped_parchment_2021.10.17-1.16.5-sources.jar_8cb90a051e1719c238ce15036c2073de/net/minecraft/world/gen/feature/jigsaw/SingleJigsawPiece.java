package net.minecraft.world.gen.feature.jigsaw;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.ProcessorLists;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class SingleJigsawPiece extends JigsawPiece {
   private static final Codec<Either<ResourceLocation, Template>> TEMPLATE_CODEC = Codec.of(SingleJigsawPiece::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
   public static final Codec<SingleJigsawPiece> CODEC = RecordCodecBuilder.create((p_236841_0_) -> {
      return p_236841_0_.group(templateCodec(), processorsCodec(), projectionCodec()).apply(p_236841_0_, SingleJigsawPiece::new);
   });
   protected final Either<ResourceLocation, Template> template;
   protected final Supplier<StructureProcessorList> processors;

   private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, Template> p_236840_0_, DynamicOps<T> p_236840_1_, T p_236840_2_) {
      Optional<ResourceLocation> optional = p_236840_0_.left();
      return !optional.isPresent() ? DataResult.error("Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(optional.get(), p_236840_1_, p_236840_2_);
   }

   protected static <E extends SingleJigsawPiece> RecordCodecBuilder<E, Supplier<StructureProcessorList>> processorsCodec() {
      return IStructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter((p_236845_0_) -> {
         return p_236845_0_.processors;
      });
   }

   protected static <E extends SingleJigsawPiece> RecordCodecBuilder<E, Either<ResourceLocation, Template>> templateCodec() {
      return TEMPLATE_CODEC.fieldOf("location").forGetter((p_236842_0_) -> {
         return p_236842_0_.template;
      });
   }

   protected SingleJigsawPiece(Either<ResourceLocation, Template> p_i242008_1_, Supplier<StructureProcessorList> p_i242008_2_, JigsawPattern.PlacementBehaviour p_i242008_3_) {
      super(p_i242008_3_);
      this.template = p_i242008_1_;
      this.processors = p_i242008_2_;
   }

   public SingleJigsawPiece(Template pTemplate) {
      this(Either.right(pTemplate), () -> {
         return ProcessorLists.EMPTY;
      }, JigsawPattern.PlacementBehaviour.RIGID);
   }

   private Template getTemplate(TemplateManager pStructureManager) {
      return this.template.map(pStructureManager::getOrCreate, Function.identity());
   }

   public List<Template.BlockInfo> getDataMarkers(TemplateManager pStructureManager, BlockPos pPos, Rotation pRotation, boolean p_214857_4_) {
      Template template = this.getTemplate(pStructureManager);
      List<Template.BlockInfo> list = template.filterBlocks(pPos, (new PlacementSettings()).setRotation(pRotation), Blocks.STRUCTURE_BLOCK, p_214857_4_);
      List<Template.BlockInfo> list1 = Lists.newArrayList();

      for(Template.BlockInfo template$blockinfo : list) {
         if (template$blockinfo.nbt != null) {
            StructureMode structuremode = StructureMode.valueOf(template$blockinfo.nbt.getString("mode"));
            if (structuremode == StructureMode.DATA) {
               list1.add(template$blockinfo);
            }
         }
      }

      return list1;
   }

   public List<Template.BlockInfo> getShuffledJigsawBlocks(TemplateManager pTemplateManager, BlockPos pPos, Rotation pRotation, Random pRandom) {
      Template template = this.getTemplate(pTemplateManager);
      List<Template.BlockInfo> list = template.filterBlocks(pPos, (new PlacementSettings()).setRotation(pRotation), Blocks.JIGSAW, true);
      Collections.shuffle(list, pRandom);
      return list;
   }

   public MutableBoundingBox getBoundingBox(TemplateManager pTemplateManager, BlockPos pPos, Rotation pRotation) {
      Template template = this.getTemplate(pTemplateManager);
      return template.getBoundingBox((new PlacementSettings()).setRotation(pRotation), pPos);
   }

   public boolean place(TemplateManager p_230378_1_, ISeedReader p_230378_2_, StructureManager p_230378_3_, ChunkGenerator p_230378_4_, BlockPos p_230378_5_, BlockPos p_230378_6_, Rotation p_230378_7_, MutableBoundingBox p_230378_8_, Random p_230378_9_, boolean p_230378_10_) {
      Template template = this.getTemplate(p_230378_1_);
      PlacementSettings placementsettings = this.getSettings(p_230378_7_, p_230378_8_, p_230378_10_);
      if (!template.placeInWorld(p_230378_2_, p_230378_5_, p_230378_6_, placementsettings, p_230378_9_, 18)) {
         return false;
      } else {
         for(Template.BlockInfo template$blockinfo : Template.processBlockInfos(p_230378_2_, p_230378_5_, p_230378_6_, placementsettings, this.getDataMarkers(p_230378_1_, p_230378_5_, p_230378_7_, false), template)) {
            this.handleDataMarker(p_230378_2_, template$blockinfo, p_230378_5_, p_230378_7_, p_230378_9_, p_230378_8_);
         }

         return true;
      }
   }

   protected PlacementSettings getSettings(Rotation pRotation, MutableBoundingBox pBoundingBox, boolean p_230379_3_) {
      PlacementSettings placementsettings = new PlacementSettings();
      placementsettings.setBoundingBox(pBoundingBox);
      placementsettings.setRotation(pRotation);
      placementsettings.setKnownShape(true);
      placementsettings.setIgnoreEntities(false);
      placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
      placementsettings.setFinalizeEntities(true);
      if (!p_230379_3_) {
         placementsettings.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
      }

      this.processors.get().list().forEach(placementsettings::addProcessor);
      this.getProjection().getProcessors().forEach(placementsettings::addProcessor);
      return placementsettings;
   }

   public IJigsawDeserializer<?> getType() {
      return IJigsawDeserializer.SINGLE;
   }

   public String toString() {
      return "Single[" + this.template + "]";
   }
}
