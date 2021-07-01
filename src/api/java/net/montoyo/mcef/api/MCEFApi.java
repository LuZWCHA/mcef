package net.montoyo.mcef.api;

import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;

public class MCEFApi {

    /**
     * Call this to get the API instance.
     *
     * @return the MCEF API or null if something failed.
     */
    @Deprecated
    public static API getAPI() {
        try {
            Class cls = Class.forName("net.montoyo.mcef.MCEF");
            Field field = cls.getField("PROXY");
            return (API) field.get(null);
        } catch(Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    
    /**
     * Checks if MCEF was loaded by forge.
     * @return true if it is loaded. false otherwise.
     */
    public static boolean isMCEFLoaded() {
        return ModList.get().isLoaded("mcef");
    }

}
