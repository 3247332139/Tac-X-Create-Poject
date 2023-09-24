package net.minecraft.util;

public class ActionResult<T> {
   private final ActionResultType result;
   private final T object;

   public ActionResult(ActionResultType pResult, T pObject) {
      this.result = pResult;
      this.object = pObject;
   }

   public ActionResultType getResult() {
      return this.result;
   }

   public T getObject() {
      return this.object;
   }

   public static <T> ActionResult<T> success(T pType) {
      return new ActionResult<>(ActionResultType.SUCCESS, pType);
   }

   public static <T> ActionResult<T> consume(T pType) {
      return new ActionResult<>(ActionResultType.CONSUME, pType);
   }

   public static <T> ActionResult<T> pass(T pType) {
      return new ActionResult<>(ActionResultType.PASS, pType);
   }

   public static <T> ActionResult<T> fail(T pType) {
      return new ActionResult<>(ActionResultType.FAIL, pType);
   }

   public static <T> ActionResult<T> sidedSuccess(T pObject, boolean pIsClientSide) {
      return pIsClientSide ? success(pObject) : consume(pObject);
   }
}