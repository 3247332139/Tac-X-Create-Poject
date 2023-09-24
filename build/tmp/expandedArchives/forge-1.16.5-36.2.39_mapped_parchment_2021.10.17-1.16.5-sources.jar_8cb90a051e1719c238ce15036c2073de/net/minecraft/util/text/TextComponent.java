package net.minecraft.util.text;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.IReorderingProcessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TextComponent implements IFormattableTextComponent {
   /**
    * The later siblings of this component. If this component turns the text bold, that will apply to all the siblings
    * until a later sibling turns the text something else.
    */
   protected final List<ITextComponent> siblings = Lists.newArrayList();
   private IReorderingProcessor visualOrderText = IReorderingProcessor.EMPTY;
   @Nullable
   @OnlyIn(Dist.CLIENT)
   private LanguageMap decomposedWith;
   private Style style = Style.EMPTY;

   /**
    * Add the given component to this component's siblings.
    * 
    * Note: If this component turns the text bold, that will apply to all the siblings until a later sibling turns the
    * text something else.
    */
   public IFormattableTextComponent append(ITextComponent pSibling) {
      this.siblings.add(pSibling);
      return this;
   }

   /**
    * Gets the raw content of this component if possible. For special components (like {@link TranslatableComponent}
    * this usually returns the empty string.
    */
   public String getContents() {
      return "";
   }

   /**
    * Gets the sibling components of this one.
    */
   public List<ITextComponent> getSiblings() {
      return this.siblings;
   }

   /**
    * Sets the style for this component and returns the component itself.
    */
   public IFormattableTextComponent setStyle(Style pStyle) {
      this.style = pStyle;
      return this;
   }

   /**
    * Gets the style of this component.
    */
   public Style getStyle() {
      return this.style;
   }

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   public abstract TextComponent plainCopy();

   /**
    * Creates a copy of this component and also copies the style and siblings. Note that the siblings are copied
    * shallowly, meaning the siblings themselves are not copied.
    */
   public final IFormattableTextComponent copy() {
      TextComponent textcomponent = this.plainCopy();
      textcomponent.siblings.addAll(this.siblings);
      textcomponent.setStyle(this.style);
      return textcomponent;
   }

   @OnlyIn(Dist.CLIENT)
   public IReorderingProcessor getVisualOrderText() {
      LanguageMap languagemap = LanguageMap.getInstance();
      if (this.decomposedWith != languagemap) {
         this.visualOrderText = languagemap.getVisualOrder(this);
         this.decomposedWith = languagemap;
      }

      return this.visualOrderText;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof TextComponent)) {
         return false;
      } else {
         TextComponent textcomponent = (TextComponent)p_equals_1_;
         return this.siblings.equals(textcomponent.siblings) && Objects.equals(this.getStyle(), textcomponent.getStyle());
      }
   }

   public int hashCode() {
      return Objects.hash(this.getStyle(), this.siblings);
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}