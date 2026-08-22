package io.github.idkwhodatis.visibletradershotfix.mixin;

import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.ramixin.visibletraders.LockedTradeData;
import net.ramixin.visibletraders.threading.FutureMerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = LockedTradeData.class, remap = false)
public abstract class LockedTradeDataMixin {
    @Shadow private List<TradeOfferList> lockedOffers;

    @Inject(method = "popTradeSet", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibletradersHotfix$onlyPopResolvedOffers(CallbackInfoReturnable<TradeOfferList> cir) {
        if (lockedOffers == null || lockedOffers.isEmpty()) return;

        TradeOfferList nextSet = lockedOffers.getFirst();
        for (TradeOffer offer : nextSet) {
            if (offer instanceof FutureMerchantOffer futureOffer && !futureOffer.isFulfilled()) {
                // Keep unresolved AIR/BARRIER placeholders out of vanilla Villager offers.
                cir.setReturnValue(null);
                return;
            }
        }

        TradeOfferList popped = lockedOffers.removeFirst();
        TradeOfferList resolved = new TradeOfferList();
        for (TradeOffer offer : popped) {
            if (offer instanceof FutureMerchantOffer futureOffer) {
                TradeOffer realOffer = futureOffer.getFuture();
                if (realOffer != null) resolved.add(realOffer);
            } else {
                resolved.add(offer);
            }
        }
        cir.setReturnValue(resolved);
    }
}
