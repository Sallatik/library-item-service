package lat.sal.library.library_item_service;

import lat.sal.library.library_item_service.dto.*;
import lat.sal.library.library_item_service.exception.OrderFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LibraryItemService {

    private final LibraryItemServiceConfig config;
    private final LibraryBookingDAO dao;

    @Autowired
    public LibraryItemService(LibraryItemServiceConfig config, LibraryBookingDAO dao) {
        this.config = config;
        this.dao = dao;
    }

    public void borrowItems(long userId, List<Long> itemIds) throws OrderFailedException {
        validateItemCountInOrder(itemIds.size());
        validateNoUnpaidLateFees(userId);

        List<BorrowRecord> borrowRecords = dao.selectCurrentBorrowRecords(userId);
        validateNoOverdueBorrowRecords(borrowRecords);

        List<Item> items = dao.selectItemsByIds(itemIds);
        validateAllItemIdsExist(itemIds, items);
        validateAllItemsAvailable(items);
        validateBorrowedItemLimits(borrowRecords, items);
        insertBorrowOrder(userId, itemIds);
    }

    public void returnItems(long userId, List<Long> itemIds) throws OrderFailedException {
        List<BorrowRecord> borrowRecords = dao.selectCurrentBorrowRecordsByIds(userId, itemIds);
        validateItemsAreBorrowed(itemIds, borrowRecords);
        List<OverdueBorrowRecord> overdueBorrowRecords = getOverdueBorrowRecords(borrowRecords);
        dao.insertLateFees(userId, overdueBorrowRecords);
        insertReturnOrder(userId, itemIds);
    }

    private void insertBorrowOrder(long userId, List<Long> itemIds) {
        long orderId = dao.insertBorrowOrder(userId);
        dao.insertOrderItems(orderId, itemIds);
        dao.insertCurrentlyBorrowed(itemIds, userId, orderId);
    }

    private void insertReturnOrder(long userId, List<Long> itemIds) {
        long orderId = dao.insertReturnOrder(userId);
        dao.insertOrderItems(orderId, itemIds);
        dao.deleteCurrentlyBorrowed(itemIds);
    }
    private List<OverdueBorrowRecord> getOverdueBorrowRecords(List<BorrowRecord> borrowRecords) {
        Instant now = Instant.now();
        int maxDaysBorrowed = config.getMaxDaysBorrowed();

        return borrowRecords.stream()
                .filter(br -> daysBetween(br.start(), now) > maxDaysBorrowed)
                .map(br -> new OverdueBorrowRecord(br.item(), daysBetween(br.start(), now)))
                .toList();
    }

    private void validateAllItemIdsExist(List<Long> itemIds, List<Item> items) throws OrderFailedException {
        List<Long> missingItemIds = getMissingItemIds(itemIds, items);
        if (!missingItemIds.isEmpty()) {
            String message = "item ids do not exist: %s".formatted(missingItemIds);
            throw new OrderFailedException(message);
        }
    }

    private void validateItemsAreBorrowed(List<Long> itemIds, List<BorrowRecord> borrowRecords) throws OrderFailedException {
        List<Item> items = borrowRecords.stream().map(BorrowRecord::item).toList();
        List<Long> missingItemIds = getMissingItemIds(itemIds, items);
        if (!missingItemIds.isEmpty()) {
            String message = "item ids not borrowed: %s".formatted(missingItemIds);
            throw new OrderFailedException(message);
        }
    }

    private List<Long> getMissingItemIds(List<Long> itemIds, List<Item> items) {
        Set<Long> existingItemIds = items.stream()
                .map(Item::id)
                .collect(Collectors.toSet());

        return itemIds.stream()
                .filter(id -> !existingItemIds.contains(id))
                .toList();
    }

    private void validateAllItemsAvailable(List<Item> items) throws OrderFailedException {
        List<String> borrowedItemTitles = items.stream()
                .filter(Item::borrowed)
                .map(Item::title)
                .toList();

        if (!borrowedItemTitles.isEmpty()) {
            String message = "Items not available to borrow: %s".formatted(borrowedItemTitles);
            throw new OrderFailedException(message);
        }
    }

    private void validateItemCountInOrder(int itemCount) throws OrderFailedException {
        if (itemCount <= 0) {
            throw new OrderFailedException("empty order");
        } else if (itemCount > config.getMaxItemCountInOrder()) {
            String message = "can only borrow %s items at a time".formatted(config.getMaxItemCountInOrder());
            throw new OrderFailedException(message);
        }
    }

    private void validateNoOverdueBorrowRecords(List<BorrowRecord> borrowRecords) throws OrderFailedException {
        getOverdueBorrowRecords(borrowRecords).stream()
                .findFirst()
                .ifPresent(obr -> {
                    String message = "item %s is late by %s days".formatted(obr.item().title(), obr.overdueDays());
                    throw new OrderFailedException(message);
                });
    }

    private void validateBorrowedItemLimits(List<BorrowRecord> borrowRecords, List<Item> newItems) throws OrderFailedException {
        List<Item> allItems = Stream.concat(
                borrowRecords.stream().map(BorrowRecord::item),
                newItems.stream()
        ).toList();

        validateTotalItemCount(allItems.size());
        validateItemCountPerCategory(allItems);
    }

    private void validateTotalItemCount(int count) {
        if (count > config.getMaxBorrowedItemCount()) {
            String message = "you can only hold %s items at the same time".formatted(config.getMaxBorrowedItemCount());
            throw new OrderFailedException(message);
        }
    }

    private void validateItemCountPerCategory(List<Item> allItems) {
        Map<ItemCategory, Integer> categoryToMaxBorrowedItemCount = config.getCategoryToMaxBorrowedItemCount();
        allItems.stream()
                .collect(Collectors.groupingBy(Item::category, Collectors.counting()))
                .forEach((category, count) -> {
                    int maxCount = categoryToMaxBorrowedItemCount.getOrDefault(category, Integer.MAX_VALUE);
                    if (count > maxCount) {
                        String message = "you can only hold %s items of category %s at the same time".formatted(maxCount, category);
                        throw new OrderFailedException(message);
                    }
                });
    }

    private void validateNoUnpaidLateFees(long userId) throws OrderFailedException {
        if (dao.selectUnpaidLateFeesExist(userId)) {
            throw new OrderFailedException("You have unpaid late fees!");
        }
    }

    private int daysBetween(Instant start, Instant end) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate startDate = start.atZone(zone).toLocalDate();
        LocalDate endDate = end.atZone(zone).toLocalDate();
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }
}
