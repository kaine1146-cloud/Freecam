package com.simplefreecam.mixin;

import com.simplefreecam.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // While freecam is on, mouse movement turns the camera instead of your character.
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void auctionhud$freecamLook(double dx, double dy, CallbackInfo ci) {
        if (!Freecam.isActive()) return;
        Entity self = (Entity) (Object) this;
        if (self == MinecraftClient.getInstance().player || self == Freecam.camera()) {
            Freecam.look(dx, dy);
            ci.cancel();
        }
    }
}
