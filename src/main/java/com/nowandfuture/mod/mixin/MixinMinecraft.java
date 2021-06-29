package com.nowandfuture.mod.mixin;

import com.nowandfuture.mod.utilities.Log;
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
            method = "destroy",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;close()V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void run(CallbackInfo callbackInfo) {
        Log.info("Shutting... CEF!");
        MCEF.PROXY.onShutdown();
    }
}
