package com.nowandfuture.mod.mixin;

import net.minecraft.client.Minecraft;
import net.montoyo.mcef.MCEF;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(
            method = "run",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void run(CallbackInfo callbackInfo) {
        MCEF.PROXY.onShutdown();
    }
}
