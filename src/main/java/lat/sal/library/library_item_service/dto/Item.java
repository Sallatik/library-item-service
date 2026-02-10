package lat.sal.library.library_item_service.dto;

public record Item(long id, ItemCategory category, String title, boolean borrowed) { }
