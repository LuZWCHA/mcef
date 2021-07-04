package com.nowandfuture.mod.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Minecraft.class)
public interface MixinMinecraftAccessor {

    @Accessor(value = "fpsCounter")
    int getFPS();
}
