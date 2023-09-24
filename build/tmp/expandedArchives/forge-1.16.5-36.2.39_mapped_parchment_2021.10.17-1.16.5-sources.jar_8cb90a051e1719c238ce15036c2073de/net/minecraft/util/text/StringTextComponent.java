package net.minecraft.util.text;

/**
 * A Component that represents just a fixed String.
 */
public class StringTextComponent extends TextComponent {
   public static final ITextComponent EMPTY = new StringTextComponent("");
   private final String text;

   public StringTextComponent(String pText) {
      this.text = pText;
   }

   /**
    * Gets the text value of this component. This is used to access the {@link #text} property, and only should be used
    * when dealing specifically with instances of {@link TextComponentString} - for other purposes, use {@link
    * #getUnformattedComponentText()}.
    */
   public String getText() {
      return this.text;
   }

   /**
    * Gets the raw content of this component if possible. For special components (like {@link TranslatableComponent}
    * this usually returns the empty string.
    */
   public String getContents() {
      return this.text;
   }

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   public StringTextComponent plainCopy() {
      return new StringTextComponent(this.text);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof StringTextComponent)) {
         return false;
      } else {
         StringTextComponent stringtextcomponent = (StringTextComponent)p_equals_1_;
         return this.text.equals(stringtextcomponent.getText()) && super.equals(p_equals_1_);
      }
   }

   public String toString() {
      return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}