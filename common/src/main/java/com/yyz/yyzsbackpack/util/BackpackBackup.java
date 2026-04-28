package com.yyz.yyzsbackpack.util;

import com.yyz.yyzsbackpack.BackpackPlatform;
import com.yyz.yyzsbackpack.base.BackupRecord;
import com.yyz.yyzsbackpack.item.BackpackItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BackpackBackup {
    /**
     * 备份当前背包内容
     * @param backpackStack 背包物品栈
     * @param inventory 玩家背包容器
     * @param maxBackups 最大备份数
     */
    public static void backupBackpackContents(ItemStack backpackStack, Container inventory, int maxBackups) {
        if (!(backpackStack.getItem() instanceof BackpackItem)) return;

        //先同步当前内容到组件
        BackpackStorage.saveBackpackContents(inventory, backpackStack);

        //获取当前物品列表
        List<ItemStack> currentItems = backpackStack.get(BackpackPlatform.getBackpackItemsComponent());

        if (currentItems == null) currentItems = new ArrayList<>();

        // 检查空背包：所有槽位都为空则跳过备份
        if (isAllEmpty(currentItems)) {
            return;
        }

        // 检查是否与最新备份重复
        List<BackupRecord> backups = backpackStack.get(BackpackPlatform.getBackupRecordsComponent());
        if (backups != null && !backups.isEmpty()) {
            BackupRecord latest = backups.get(0);
            List<ItemStack> latestItems = latest.items();
            if (isSameContents(currentItems, latestItems)) {
                return; // 与最新备份相同，不备份
            }
        }

        List<ItemStack> copiedItems = currentItems.stream()
                .map(ItemStack::copy)
                .toList();

        //获取现有备份列表
        if (backups == null) backups = new LinkedList<>();
        else backups = new LinkedList<>(backups); // 转为可变列表

        //添加新备份
        backups.add(0, new BackupRecord(System.currentTimeMillis(), copiedItems)); // 新备份放前面

        // 超过最大数量则移除最旧的
        while (backups.size() > maxBackups) {
            backups.remove(backups.size() - 1);
        }

        // 6. 写回组件
        backpackStack.set(BackpackPlatform.getBackupRecordsComponent(), backups);
    }

    /**
     * 恢复指定索引的备份（0为最新）
     */
    public static boolean restoreBackup(ItemStack backpackStack, Container inventory, int backupIndex) {
        List<BackupRecord> backups = backpackStack.get(BackpackPlatform.getBackupRecordsComponent());
        if (backups == null || backupIndex < 0 || backupIndex >= backups.size()) return false;

        BackupRecord record = backups.get(backupIndex);
        List<ItemStack> items = record.items();

        // 清空背包原有槽位
        BackpackItem backpackItem = (BackpackItem) backpackStack.getItem();
        int numSlots = backpackItem.getBackpackType().getSize();
        for (int i = 0; i < numSlots; i++) {
            inventory.setItem(36 + i, ItemStack.EMPTY);
        }
        // 恢复物品
        for (int i = 0; i < Math.min(items.size(), numSlots); i++) {
            if (!items.get(i).isEmpty()) {
                inventory.setItem(36 + i, items.get(i).copy());
            }
        }
        // 同步到组件
        BackpackStorage.saveBackpackContents(inventory, backpackStack);
        return true;
    }

    /**
     * 检查物品列表是否全部为空
     */
    private static boolean isAllEmpty(List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较两个物品列表是否内容相同（逐槽比较 ItemStack）
     */
    private static boolean isSameContents(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }
}
