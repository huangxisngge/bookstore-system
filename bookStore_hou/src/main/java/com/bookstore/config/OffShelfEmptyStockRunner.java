package com.bookstore.config;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bookstore.entity.Book;
import com.bookstore.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时自愈维护：把"库存 <= 0 但仍处于上架态(1)"的图书统一自动下架(0)。
 * 保证即便存在由历史数据/直连 SQL 造成的库存为 0 而未下架的脏数据，
 * 重启后也会自动恢复一致，避免前端点击售罄图书后反复转圈。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OffShelfEmptyStockRunner implements ApplicationRunner {

    private final BookMapper bookMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            int rows = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                    .set(Book::getStatus, 0)
                    .eq(Book::getDeleted, 0)
                    .le(Book::getStock, 0)
                    .eq(Book::getStatus, 1));
            if (rows > 0) {
                log.info("库存为0的图书已自动下架 {} 本", rows);
            }
        } catch (Exception e) {
            log.warn("库存为0自动下架自愈任务执行失败：{}", e.getMessage(), e);
        }
    }
}