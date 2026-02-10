package lat.sal.library.library_item_service;

import lat.sal.library.library_item_service.dto.ItemCategory;

import java.util.Map;

public class LibraryItemServiceConfig {

    public int getMaxItemCountInOrder() {
        return 5;
    }

    public int getMaxBorrowedItemCount() {
        return 7;
    }

    public Map<ItemCategory, Integer> getCategoryToMaxBorrowedItemCount() {
        return Map.of(ItemCategory.NEW, 2);
    }

    public int getMaxDaysBorrowed() {
        return 30;
    }
}
