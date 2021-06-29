package com.nowandfuture.mod.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MCEFConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("mixins.mcef.json");
    }
}
