package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.PageResult;
import com.bookstore.common.Result;
import com.bookstore.entity.Book;
import com.bookstore.service.BookService;
import com.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/list")
    public Result<PageResult<BookVO>> getBookList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Page<Book> page = new Page<>(current, size);
        Page<BookVO> result = bookService.getBookList(page, categoryId, keyword, status, sort);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/detail/{id}")
    public Result<BookVO> getBookDetail(@PathVariable Long id) {
        return Result.success(bookService.getBookDetail(id));
    }

    @GetMapping("/hot")
    public Result<List<BookVO>> getHotBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @GetMapping("/upcoming-grab")
    public Result<List<BookVO>> getUpcomingGrabs(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getUpcomingGrabs(limit));
    }

    @PostMapping("/add")
    public Result<?> addBook(@RequestBody Book book) {
        bookService.addBook(book);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> updateBook(@RequestBody Book book) {
        bookService.updateBook(book);
        return Result.success("更新成功");
    }

    @PutMapping("/clear-grab/{id}")
    public Result<?> clearGrab(@PathVariable Long id) {
        bookService.clearGrab(id);
        return Result.success("已转为普通售卖");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success("删除成功");
    }
}
