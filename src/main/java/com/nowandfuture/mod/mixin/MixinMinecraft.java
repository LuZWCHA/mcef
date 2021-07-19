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
            method = "shutdownMinecraftApplet",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void run(CallbackInfo callbackInfo) {
        MCEF.PROXY.onShutdown();
    }

}
