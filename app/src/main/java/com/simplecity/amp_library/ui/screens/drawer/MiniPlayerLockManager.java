package com.simplecity.amp_library.ui.screens.drawer;

import java.util.ArrayList;
import java.util.List;

public class MiniPlayerLockManager {

    public interface MiniPlayerLock {
        String getTag();
    }

    private List<MiniPlayerLock> miniPlayerLocks = new ArrayList<>();
    
    private MiniPlayerLockManager() {
    
    }
    
    public boolean canShowMiniPlayer() {
        return miniPlayerLocks.isEmpty();
    }
    
    private static class Holder {
        private static final MiniPlayerLockManager INSTANCE = new MiniPlayerLockManager();
    }
    
    public static MiniPlayerLockManager getInstance() {
        return Holder.INSTANCE;
    }

    public void addMiniPlayerLock(MiniPlayerLock miniPlayerLock) {
        if (!miniPlayerLocks.contains(miniPlayerLock)) {
            miniPlayerLocks.add(miniPlayerLock);
        }
    }

    public void removeMiniPlayerLock(MiniPlayerLock miniPlayerLock) {
        if (miniPlayerLocks.contains(miniPlayerLock)) {
            miniPlayerLocks.remove(miniPlayerLock);
        }
    }
}