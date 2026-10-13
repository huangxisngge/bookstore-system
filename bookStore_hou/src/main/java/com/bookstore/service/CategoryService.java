package com.bookstore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> getAllCategories();

    void addCategory(Category category);

    void updateCategory(Category category);

    void deleteCategory(Long id);
}
