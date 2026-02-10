package lat.sal.library.library_item_service.dto;

import lat.sal.library.library_item_service.dto.Item;

public record OverdueBorrowRecord(Item item, int overdueDays) { }
