package com.ca.module.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.module.product.dto.ProductCreateDTO;
import com.ca.module.product.dto.ProductUpdateDTO;
import com.ca.module.product.vo.ProductCardVO;
import com.ca.module.product.vo.ProductVO;

import java.util.List;

public interface ProductService {
    ProductVO create(Long sellerId, ProductCreateDTO dto, List<String> imageUrls);
    ProductVO getById(Long productId);
    ProductVO update(Long userId, Long productId, ProductUpdateDTO dto);
    IPage<ProductCardVO> list(int page, int size, String sort);
    IPage<ProductCardVO> search(String q, Integer categoryId, Integer minPrice, Integer maxPrice, String type, String sort, int page, int size);
    boolean toggleFavorite(Long userId, Long productId);
    IPage<ProductCardVO> myFavorites(Long userId, int page, int size);
    IPage<ProductCardVO> myProducts(Long userId, int page, int size, String status);
    void delete(Long userId, Long productId);
}
