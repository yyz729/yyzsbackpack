package com.yyz.yyzsbackpack.network;

import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PickupPrefs {
    private static final Set<UUID> PREFER = ConcurrentHashMap.newKeySet();

    private PickupPrefs() {}

    public static void set(UUID id, boolean prefer) {
        if (prefer) PREFER.add(id); else PREFER.remove(id);
    }
    public static boolean prefers(Player player) { return PREFER.contains(player.getUUID()); }
    public static void clear(UUID id) { PREFER.remove(id); }
}