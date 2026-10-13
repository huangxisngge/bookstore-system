package com.bookstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.entity.Book;
import com.bookstore.vo.BookVO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookService extends IService<Book> {

    BookVO getBookDetail(Long id);

    Page<BookVO> getBookList(Page<Book> page, Long categoryId, String keyword, Integer status, String sort);

    List<BookVO> getHotBooks(int limit);

    /**
     * 即将开抢的图书列表（开抢时间在未来，按开抢时间从近到远），用于首页倒计时专区
     */
    List<BookVO> getUpcomingGrabs(int limit);

    void addBook(Book book);

    void updateBook(Book book);

    void deleteBook(Long id);

    /**
     * 清除开抢时间，把开抢书恢复为普通售卖状态（库存>0 时上架可正常加购/下单）
     */
    void clearGrab(Long id);

    /**
     * 是否处于"未开抢"状态：设置了开抢时间且当前时间尚未到达。
     * 未开抢的图书禁止加购/下单/支付，前端统一展示倒计时。
     */
    default boolean isGrabNotStarted(Book book) {
        return book != null && book.getGrabStartTime() != null
                && LocalDateTime.now().isBefore(book.getGrabStartTime());
    }

    /**
     * 事务内悲观锁读取图书（SELECT ... FOR UPDATE），用于下单扣库存前的校验
     */
    Book getBookForUpdate(Long bookId);

    /**
     * 下单前的无锁库存预判（快速失败路径）：
     * 明显缺货或已下架时直接抛 409，拦截在进入 FOR UPDATE 行锁之前，降低锁竞争。
     * 可作为数据库行锁并发性能的优化手段；正确性仍由悲观锁+原子扣减保证。
     */
    void preCheckBookStock(Long bookId, Integer quantity);

    /**
     * 判断图书是否仍然存在（未删除）：用于下单前清理购物车中已删除图书的失效条目。
     */
    boolean isBookExisting(Long bookId);

    /**
     * 原子扣减库存并累加销量（SQL 层防超卖），失败抛业务异常
     */
    void deductStock(Long bookId, Integer quantity);

    /**
     * 回补库存（取消订单/退货入库）
     */
    void restoreStock(Long bookId, Integer quantity);

    List<Book> getLowStockBooks(int threshold);
}
