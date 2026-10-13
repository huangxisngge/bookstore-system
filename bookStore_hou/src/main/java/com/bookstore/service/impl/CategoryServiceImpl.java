package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final BookMapper bookMapper;

    @Override
    public List<Category> getAllCategories() {
        return this.list(new LambdaQueryWrapper<Category>().orderByAsc(Category::getId));
    }

    @Override
    public void addCategory(Category category) {
        Long count = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        this.save(category);
    }

    @Override
    public void updateCategory(Category category) {
        if (this.getById(category.getId()) == null) {
            throw new BusinessException("分类不存在");
        }
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getCategoryId, id)
                .orderByAsc(Book::getId));
        if (!books.isEmpty()) {
            String titles = books.stream()
                    .map(Book::getTitle)
                    .collect(Collectors.joining("、"));
            throw new BusinessException("该分类下仍有 " + books.size()
                    + " 本图书（" + titles + "），请先将图书移至其他分类或删除后再删除该分类");
        }
        this.removeById(id);
    }
}
