package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private MultiMerchantSchemaSupport schemaSupport;

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.DISABLE);
        if (schemaSupport.supportsCategoryScope()) {
            category.setMerchantId(MerchantScopeUtils.resolveRequiredMerchantId(categoryDTO.getMerchantId()));
            categoryMapper.insert(category);
            return;
        }
        category.setMerchantId(null);
        categoryMapper.insertLegacy(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        if (schemaSupport.supportsCategoryScope()) {
            categoryPageQueryDTO.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(categoryPageQueryDTO.getMerchantId()));
        } else {
            categoryPageQueryDTO.setMerchantId(null);
        }
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteById(Long id) {
        Category category = getAccessibleCategory(id);
        Integer dishCount = dishMapper.countByCategoryId(category.getId());
        if (dishCount != null && dishCount > 0) {
            throw new BaseException("褰撳墠鍒嗙被鍏宠仈浜嗚彍鍝侊紝涓嶈兘鍒犻櫎");
        }

        Integer setmealCount = setmealMapper.countByCategoryId(category.getId());
        if (setmealCount != null && setmealCount > 0) {
            throw new BaseException("褰撳墠鍒嗙被鍏宠仈浜嗗椁愶紝涓嶈兘鍒犻櫎");
        }

        categoryMapper.deleteById(category.getId());
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = getAccessibleCategory(categoryDTO.getId());
        Category toUpdate = new Category();
        BeanUtils.copyProperties(categoryDTO, toUpdate);
        toUpdate.setId(category.getId());
        if (schemaSupport.supportsCategoryScope()) {
            Long merchantId = MerchantScopeUtils.resolveQueryMerchantId(categoryDTO.getMerchantId());
            toUpdate.setMerchantId(merchantId == null ? category.getMerchantId() : merchantId);
        } else {
            toUpdate.setMerchantId(null);
        }
        categoryMapper.update(toUpdate);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = getAccessibleCategory(id);
        categoryMapper.update(Category.builder()
                .id(category.getId())
                .status(status)
                .build());
    }

    @Override
    public List<Category> list(Integer type, Long merchantId) {
        Long resolvedMerchantId = schemaSupport.supportsCategoryScope()
                ? MerchantScopeUtils.resolveQueryMerchantId(merchantId)
                : null;
        return categoryMapper.list(type, resolvedMerchantId);
    }

    private Category getAccessibleCategory(Long id) {
        Category category = categoryMapper.getById(id);
        if (category == null) {
            throw new BaseException("鍒嗙被涓嶅瓨鍦?");
        }
        MerchantScopeUtils.assertAccessible(category.getMerchantId());
        return category;
    }
}
