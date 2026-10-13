package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.entity.Book;
import com.bookstore.vo.BookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 联表分页查询：book LEFT JOIN category，直接产出 BookVO
     */
    Page<BookVO> selectBookVOPage(Page<BookVO> page,
                                  @Param("categoryId") Long categoryId,
                                  @Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("sort") String sort);

    /**
     * 联表查详情
     */
    BookVO selectBookVOById(@Param("id") Long id);

    /**
     * 联表查热销榜
     */
    List<BookVO> selectHotBookVOs(@Param("limit") int limit);

    /**
     * 联表查"即将开抢"列表（开抢时间在未来，按开抢时间从近到远）
     */
    List<BookVO> selectUpcomingGrabVOs(@Param("limit") int limit);

    /**
     * 悲观锁：SELECT ... FOR UPDATE，在事务内锁定该行，
     * 其他事务并发读取同一本书时会阻塞等待，直到本事务提交/回滚后释放锁。
     * 必须在 @Transactional 事务内调用。
     */
    @Select("SELECT * FROM book WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Book selectByIdForUpdate(@Param("id") Long id);

    /**
     * 下单前的"无锁预判"：普通 SELECT 快速读取当前库存与上架状态（不加锁）。
     * 仅用于把明显没货/已下架的请求挡在 FOR UPDATE 行锁之外，降低锁竞争。
     * 注意：此处读到的可能是稍旧的值，属于"快捷路径"，真正的正确性
     * 仍由后续的 selectByIdForUpdate（悲观锁）+ deductStock（原子扣减）保证。
     */
    @Select("SELECT id, title, status, stock, grab_start_time FROM book WHERE id = #{id} AND deleted = 0")
    Book selectStockForPreCheck(@Param("id") Long id);

    /**
     * 原子扣库存：WHERE stock >= quantity 作为兜底防线，
     * 并发下不会扣成负数。返回受影响行数：0 = 库存不足或图书不可售。
     * 当剩余库存 <= 0 时，自动将该图书下架（status = 0），
     * 使前端"已售罄/已下架"状态一致，避免售罄后仍可触发下单导致界面一直等待。
     */
    @Update("UPDATE book SET stock = stock - #{quantity}, sales = sales + #{quantity}, " +
            "status = CASE WHEN stock - #{quantity} <= 0 THEN 0 ELSE status END " +
            "WHERE id = #{id} AND deleted = 0 AND status = 1 AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回补库存（取消订单/退货审核通过）
     */
    @Update("UPDATE book SET stock = stock + #{quantity} WHERE id = #{id} AND deleted = 0")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 清除开抢时间：开抢书由管理员手动恢复为普通售卖状态（grab_start_time -> NULL）
     */
    @Update("UPDATE book SET grab_start_time = NULL WHERE id = #{id} AND deleted = 0")
    int clearGrabStartTime(@Param("id") Long id);
}
