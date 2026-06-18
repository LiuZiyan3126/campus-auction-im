package com.ca.module.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.config.FileStorageService;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.product.dto.ProductCreateDTO;
import com.ca.module.product.dto.ProductUpdateDTO;
import com.ca.module.product.service.ProductService;
import com.ca.module.product.vo.ProductCardVO;
import com.ca.module.product.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ca.module.product.entity.Category;
import com.ca.module.product.mapper.CategoryMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final CategoryMapper categoryMapper;

    @GetMapping("/categories")
    public ResultVO<java.util.List<Category>> categories() {
        return ResultVO.ok(categoryMapper.selectList(null));
    }

    @GetMapping
    public ResultVO<PageVO<ProductCardVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "created_at") String sort) {
        IPage<ProductCardVO> result = productService.list(page, size, sort);
        return ResultVO.ok(PageVO.of(result));
    }

    @PostMapping
    public ResultVO<ProductVO> create(Authentication auth,
                                       @Valid @RequestPart("data") ProductCreateDTO dto,
                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = (Long) auth.getPrincipal();
        List<String> urls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile file : images) {
                urls.add(fileStorageService.upload(file));
            }
        }
        return ResultVO.ok(productService.create(userId, dto, urls));
    }

    @GetMapping("/{id}")
    public ResultVO<ProductVO> getById(@PathVariable Long id) {
        return ResultVO.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResultVO<ProductVO> update(Authentication auth,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ProductUpdateDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(productService.update(userId, id, dto));
    }

    @GetMapping("/search")
    public ResultVO<PageVO<ProductCardVO>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<ProductCardVO> result = productService.search(q, categoryId, minPrice, maxPrice, type, sort, page, size);
        return ResultVO.ok(PageVO.of(result));
    }

    @PostMapping("/{id}/favorite")
    public ResultVO<Boolean> toggleFavorite(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok(productService.toggleFavorite(userId, id));
    }

    @GetMapping("/favorites")
    public ResultVO<PageVO<ProductCardVO>> myFavorites(Authentication auth,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        IPage<ProductCardVO> result = productService.myFavorites(userId, page, size);
        return ResultVO.ok(PageVO.of(result));
    }

    @GetMapping("/my")
    public ResultVO<PageVO<ProductCardVO>> myProducts(Authentication auth,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String status) {
        Long userId = (Long) auth.getPrincipal();
        IPage<ProductCardVO> result = productService.myProducts(userId, page, size, status);
        return ResultVO.ok(PageVO.of(result));
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        productService.delete(userId, id);
        return ResultVO.ok(null);
    }
}
