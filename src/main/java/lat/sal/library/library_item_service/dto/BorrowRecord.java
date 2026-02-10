package lat.sal.library.library_item_service.dto;

import java.time.Instant;

public record BorrowRecord(Item item, Instant start) { }
