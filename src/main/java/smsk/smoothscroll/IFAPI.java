package smsk.smoothscroll;

import net.raphimc.immediatelyfastapi.ApiAccess;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;

public class IFAPI {
    private static ApiAccess api;

    public static void loadAPI() {
        api = ImmediatelyFastApi.getApiImpl();
    }
    public static void disableHUDBatching() {
        api.getBatching().endHudBatching();
    }
    public static void enableHUDBatching() {
        api.getBatching().beginHudBatching();
    }
}
