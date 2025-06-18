package com.simplecity.amp_library.ui.screens.drawer;

import java.util.ArrayList;
import java.util.List;

public class MiniPlayerLockManager {

    public interface MiniPlayerLock {
        String getTag();
    }

    private List<MiniPlayerLock> miniPlayerLocks = new ArrayList<>();
    
    public MiniPlayerLockManager() {
        // Constructor is intentionally empty because no initialization is required at this time.
    }
    
    public boolean canShowMiniPlayer() {
        return miniPlayerLocks.isEmpty();
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