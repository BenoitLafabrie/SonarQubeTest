package com.simplecity.amp_library.ui.screens.drawer;

import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DrawerLockManager {

    public interface DrawerLock {
        String getTag();
    }

    // Use Initialization-on-demand holder idiom for thread-safe Singleton
    private static class Holder {
        private static final DrawerLockManager INSTANCE = new DrawerLockManager();
    }
    
    private List<DrawerLock> drawerLocks = new ArrayList<>();

    @Nullable
    private DrawerLockController drawerLockController;

    private DrawerLockManager() {
    public static DrawerLockManager getInstance() {
        return Holder.INSTANCE;
    }
        }
        return instance;
    }

    public void setDrawerLockController(@Nullable DrawerLockController drawerLockController) {
        this.drawerLockController = drawerLockController;
    }

    public void addDrawerLock(DrawerLock drawerLock) {
        if (!drawerLocks.contains(drawerLock)) {
            drawerLocks.add(drawerLock);
        }
        if (drawerLockController != null) {
            drawerLockController.lockDrawer();
        }
    }

    public void removeDrawerLock(DrawerLock drawerLock) {
        if (drawerLocks.contains(drawerLock)) {
            drawerLocks.remove(drawerLock);
        }
        if (drawerLocks.isEmpty()) {
            if (drawerLockController != null) {
                drawerLockController.unlockDrawer();
            }
        }
    }
}