package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.entity.Cart;
import com.bookstore.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 购物车联表查询：cart JOIN book，一次 SQL 带出图书信息，直接映射到 CartVO
     */
    @Select("SELECT ct.id, ct.user_id AS userId, ct.book_id AS bookId, ct.quantity, " +
            "b.title AS bookTitle, b.cover_image AS bookCover, " +
            "b.price, b.discount, b.promo_price AS promoPrice, " +
            "b.promo_start_time AS promoStartTime, b.promo_end_time AS promoEndTime, " +
            "b.grab_start_time AS grabStartTime, b.stock " +
            "FROM cart ct " +
            "JOIN book b ON ct.book_id = b.id AND b.deleted = 0 " +
            "WHERE ct.user_id = #{userId} AND ct.deleted = 0 " +
            "ORDER BY ct.create_time DESC")
    List<CartVO> selectCartVOList(@Param("userId") Long userId);

    @Select("SELECT id, user_id, book_id, quantity, create_time, update_time, deleted FROM cart WHERE user_id = #{userId} AND book_id = #{bookId}")
    Cart selectIncludingDeleted(Long userId, Long bookId);

    @Update("UPDATE cart SET quantity = #{quantity}, deleted = 0, update_time = NOW() WHERE id = #{id}")
    void restoreAndUpdate(Long id, Integer quantity);
}
