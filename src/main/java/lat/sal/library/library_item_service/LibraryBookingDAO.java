package lat.sal.library.library_item_service;

import lat.sal.library.library_item_service.dto.BorrowRecord;
import lat.sal.library.library_item_service.dto.Item;
import lat.sal.library.library_item_service.dto.OverdueBorrowRecord;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface LibraryBookingDAO {

    @SqlQuery("""
               SELECT i.id, i.category, i.title, ISNULL(cbi.user_id) as borrowed
               FROM item i
               LEFT JOIN currently_borrowed_item cbi ON i.id = cbi.item_id
               WHERE i.id in (<itemIds>)
            """)
    List<Item> selectItemsByIds(@BindList("itemIds") List<Long> itemIds);

    @SqlQuery("""
                SELECT i.id, i.category, i.title, o.created
                FROM currently_borrowed_item cbi
                INNER JOIN item i ON cbi.item_id = i.id
                INNER JOIN user_order o ON cbi.order_id = o.id
                WHERE cbi.user_id = :userId
            """)
    List<BorrowRecord> selectCurrentBorrowRecords(@Bind("userId") long userId);

    @SqlQuery("""
                SELECT i.id, i.category, i.title, o.created
                FROM currently_borrowed_item cbi
                INNER JOIN item i ON cbi.item_id = i.id
                INNER JOIN user_order o ON cbi.order_id = o.id
                WHERE cbi.user_id = :userId AND i.id in (<itemIds>)
            """)
    List<BorrowRecord> selectCurrentBorrowRecordsByIds(@Bind("userId") long userId, @BindList("itemIds") List<Long> itemIds);

    @SqlQuery("""
                SELECT COUNT(*) > 0
                FROM late_fee
                WHERE user_id = :userId AND paid = FALSE
            """)
    boolean selectUnpaidLateFeesExist(@Bind("userId") long userId);

    @SqlUpdate("""
                INSERT INTO user_order (user_id, type)
                VALUES (:userId, 'BORROW')
            """)
    @GetGeneratedKeys
    long insertBorrowOrder(long userId);

    @SqlBatch("""
                INSERT INTO order_to_item (order_id, item_id)
                VALUES (:orderId, :itemId)
            """)
    void insertOrderItems(@Bind("orderId") long orderId,
                          @Bind("itemId") List<Long> itemIds);

    @SqlBatch("""
                INSERT INTO currently_borrowed_item (item_id, user_id, order_id)
                VALUES (:itemId, :userId, :orderId)
            """)
    void insertCurrentlyBorrowed(@Bind("itemId") List<Long> itemIds,
                                 @Bind("userId") long userId,
                                 @Bind("orderId") long orderId);


    @SqlUpdate("""
                INSERT INTO user_order (user_id, type)
                VALUES (:userId, 'RETURN')
            """)
    @GetGeneratedKeys
    long insertReturnOrder(long userId);

    @SqlBatch("""
                DELETE FROM currently_borrowed_item
                WHERE item_id = :itemId
            """)
    void deleteCurrentlyBorrowed(@Bind("itemId") List<Long> itemIds);

    @SqlBatch("""
                INSERT INTO late_fee (user_id, days, paid)
                VALUES (:userId, :days, FALSE)
            """)
    void insertLateFees(@Bind("userId") long userId,
                        @BindBean List<OverdueBorrowRecord> overdueBorrowRecords);
}
