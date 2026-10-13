package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.mapper.BookMapper;
import com.bookstore.service.BookService;
import com.bookstore.service.CategoryService;
import com.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private final CategoryService categoryService;

    @Override
    public BookVO getBookDetail(Long id) {
        BookVO vo = this.getBaseMapper().selectBookVOById(id);
        if (vo == null) {
            throw new BusinessException("图书不存在");
        }
        return fillPromotionInfo(vo);
    }

    @Override
    public Page<BookVO> getBookList(Page<Book> page, Long categoryId, String keyword, Integer status, String sort) {
        Page<BookVO> voPage = this.getBaseMapper()
                .selectBookVOPage(new Page<>(page.getCurrent(), page.getSize()), categoryId, keyword, status, sort);
        voPage.getRecords().forEach(this::fillPromotionInfo);
        return voPage;
    }

    @Override
    public List<BookVO> getHotBooks(int limit) {
        List<BookVO> list = this.getBaseMapper().selectHotBookVOs(limit);
        list.forEach(this::fillPromotionInfo);
        return list;
    }

    @Override
    public List<BookVO> getUpcomingGrabs(int limit) {
        List<BookVO> list = this.getBaseMapper().selectUpcomingGrabVOs(limit);
        list.forEach(this::fillPromotionInfo);
        return list;
    }

    @Override
    public void addBook(Book book) {
        // 新增时忽略前端可能附带/残留的 id，交由数据库自增，避免主键冲突
        book.setId(null);
        // 库存为 0 的图书自动下架（status=0），避免上架后用户点击售罄书卡顿
        if (book.getStock() != null && book.getStock() <= 0) {
            book.setStatus(0);
        }
        this.save(book);
    }

    @Override
    public void updateBook(Book book) {
        Book current = this.getById(book.getId());
        if (current == null) {
            throw new BusinessException("图书不存在");
        }
        Integer stock = book.getStock();
        // 尝试上架(status=1)但库存为 0：明确拒绝并给出原因，而不是静默成功
        if (book.getStatus() != null && book.getStatus() == 1
                && stock != null && stock <= 0) {
            throw new BusinessException("库存为 0，禁止上架该图书，请先补充库存");
        }
        // 库存为 0（含设为 0）的图书强制自动下架，防止售罄但未下架的书被用户反复点击卡顿
        if (stock != null && stock <= 0) {
            book.setStatus(0);
        }
        // 原库存为 0（被自动下架）的书，本次补货至 >0 时自动重新上架，
        // 与"库存为 0 自动下架"对称，补货后无需手动切开关即可正常售卖。
        // 仅当库里当前库存确实为 0 时触发，避免覆盖管理员通过开关主动执行的"下架"操作。
        if (current.getStock() != null && current.getStock() <= 0
                && stock != null && stock > 0) {
            book.setStatus(1);
        }
        this.updateById(book);
    }

    @Override
    public void clearGrab(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("图书不存在");
        }
        int rows = this.getBaseMapper().clearGrabStartTime(id);
        if (rows == 0) {
            throw new BusinessException("清除开抢时间失败");
        }
        // 库存 > 0 时确保恢复为上架，与"库存 0 自动下架 / 补货自动上架"保持一致
        Book current = this.getById(id);
        if (current != null && current.getStock() != null && current.getStock() > 0
                && current.getStatus() != null && current.getStatus() != 1) {
            current.setStatus(1);
            this.updateById(current);
        }
    }

    @Override
    public void deleteBook(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("图书不存在");
        }
        this.removeById(id);
    }

    @Override
    public Book getBookForUpdate(Long bookId) {
        return this.getBaseMapper().selectByIdForUpdate(bookId);
    }

    @Override
    public boolean isBookExisting(Long bookId) {
        return this.getById(bookId) != null;
    }

    /**
     * 无锁库存预判：在请求进入 SELECT...FOR UPDATE 排队之前，用普通 SELECT
     * 快速读取库存，明显没货/已下架时直接抛 409 拦截，避免无谓等待行锁。
     *
     * 本方法故意不加锁，读到的可能是"刚刚被别人抢光、但数据库还没反映出来"的旧值，
     * 所以只用于"早拦截"，不能替代后续 FOR UPDATE + 原子扣减的正确性保证。
     * 即：本方法漏掉了（少量刚好被捕获的边角 case），仍由主线兜住，不会超卖。
     */
    @Override
    public void preCheckBookStock(Long bookId, Integer quantity) {
        Book book = this.getBaseMapper().selectStockForPreCheck(bookId);
        if (book == null) {
            throw new BusinessException(OrderServiceImpl.STOCK_CONFLICT_CODE, "图书不存在或已下架");
        }
        if (book.getStatus() != 1) {
            throw new BusinessException(OrderServiceImpl.STOCK_CONFLICT_CODE, "图书已下架");
        }
        if (isGrabNotStarted(book)) {
            throw new BusinessException(OrderServiceImpl.STOCK_CONFLICT_CODE,
                    "「" + book.getTitle() + "」尚未开抢，暂不可下单");
        }
        if (book.getStock() <= 0) {
            throw new BusinessException(OrderServiceImpl.STOCK_CONFLICT_CODE,
                    "手慢了！「" + book.getTitle() + "」已被抢光");
        }
        if (book.getStock() < quantity) {
            throw new BusinessException(OrderServiceImpl.STOCK_CONFLICT_CODE,
                    "「" + book.getTitle() + "」库存仅剩 " + book.getStock() + " 件，不足");
        }
    }

    /**
     * SQL: UPDATE ... SET stock = stock - #{q} WHERE stock >= #{q}
     * 数据库行锁保证同一行的并发更新串行执行，WHERE 条件保证库存永不为负。
     * 返回 0 行说明库存不足，抛出异常使整个事务回滚（BEGIN/ROLLBACK 由 Spring 在 SQL 层面执行）。
     */
    @Override
    public void deductStock(Long bookId, Integer quantity) {
        int rows = this.getBaseMapper().deductStock(bookId, quantity);
        if (rows == 0) {
            throw new BusinessException(409, "手慢了！您抢购的商品已被抢光，未获得购买资格");
        }
    }

    @Override
    public void restoreStock(Long bookId, Integer quantity) {
        int rows = this.getBaseMapper().restoreStock(bookId, quantity);
        if (rows == 0) {
            throw new BusinessException("回补库存失败：图书不存在");
        }
    }

    @Override
    public List<Book> getLowStockBooks(int threshold) {
        return this.list(new LambdaQueryWrapper<Book>()
                .le(Book::getStock, threshold)
                .eq(Book::getStatus, 1)
                .orderByAsc(Book::getStock));
    }

    /**
     * 促销状态和最终售价依赖当前时间，属于计算字段，统一在服务层填充
     */
    private BookVO fillPromotionInfo(BookVO vo) {
        LocalDateTime now = LocalDateTime.now();
        boolean onPromo = Boolean.TRUE.equals(isOnPromo(vo, now));
        vo.setOnPromo(onPromo);
        BigDecimal finalPrice = onPromo ? vo.getPromoPrice()
                : vo.getPrice().multiply(vo.getDiscount());
        vo.setFinalPrice(finalPrice);

        // 开抢倒计时：设置了开抢时间且当前时间未到达 → 未开抢，附带剩余秒数供前端渲染
        long remainSeconds = 0L;
        boolean notStarted = false;
        LocalDateTime grabTime = vo.getGrabStartTime();
        if (grabTime != null && now.isBefore(grabTime)) {
            notStarted = true;
            remainSeconds = Math.max(0L, Duration.between(now, grabTime).getSeconds());
        }
        vo.setGrabNotStarted(notStarted);
        vo.setGrabRemainSeconds(remainSeconds);
        return vo;
    }

    private Boolean isOnPromo(BookVO vo, LocalDateTime now) {
        return vo.getPromoStartTime() != null
                && vo.getPromoEndTime() != null
                && vo.getPromoPrice() != null
                && !now.isBefore(vo.getPromoStartTime())
                && !now.isAfter(vo.getPromoEndTime());
    }
}
