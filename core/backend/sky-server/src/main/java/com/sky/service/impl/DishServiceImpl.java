package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.utils.StorefrontImageResolver;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;

    private final SetmealDishMapper setMealDishMapper;

    private final DishFlavorMapper dishFlavorMapper;

    private final CategoryMapper categoryMapper;

    private final StorefrontImageResolver storefrontImageResolver;

    private final MultiMerchantSchemaSupport schemaSupport;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Long merchantId = schemaSupport.supportsDishScope()
                ? MerchantScopeUtils.resolveRequiredMerchantId(dishDTO.getMerchantId())
                : null;
        validateCategoryOwnership(dishDTO.getCategoryId(), merchantId);

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setMerchantId(merchantId);
        if (schemaSupport.supportsDishScope()) {
            dishMapper.insert(dish);
        } else {
            dishMapper.insertLegacy(dish);
        }

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(item -> item.setDishId(dish.getId()));
            dishMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO queryDTO) {
        if (schemaSupport.supportsDishScope()) {
            queryDTO.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(queryDTO.getMerchantId()));
        } else {
            queryDTO.setMerchantId(null);
        }
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(queryDTO);
        List<DishVO> records = page.getResult();
        if (records != null) {
            records.forEach(this::fillDisplayImage);
        }
        return new PageResult(page.getTotal(), records);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = getAccessibleDish(id);
            if (StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new BaseException("璧峰敭涓殑鑿滃搧涓嶈兘鍒犻櫎");
            }
        }

        List<SetmealDish> setmealIds = setMealDishMapper.getSetmealsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new BaseException("褰撳墠鑿滃搧鍏宠仈浜嗗椁愶紝涓嶈兘鍒犻櫎");
        }

        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatch(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = getAccessibleDish(id);
        dishMapper.update(Dish.builder().id(dish.getId()).status(status).build());
    }

    @Override
    @Transactional
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = getAccessibleDish(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        fillDisplayImage(dishVO);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish existing = getAccessibleDish(dishDTO.getId());
        Long merchantId = null;
        if (schemaSupport.supportsDishScope()) {
            merchantId = MerchantScopeUtils.resolveQueryMerchantId(dishDTO.getMerchantId());
            if (merchantId == null) {
                merchantId = existing.getMerchantId();
            }
        }
        validateCategoryOwnership(dishDTO.getCategoryId(), merchantId);

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setId(existing.getId());
        dish.setMerchantId(merchantId);
        dishMapper.update(dish);
        dishFlavorMapper.deleteById(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(item -> item.setDishId(dishDTO.getId()));
            dishMapper.insertBatch(flavors);
        }
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        Category category = categoryMapper.getById(categoryId);
        if (category != null && schemaSupport.supportsDishScope()) {
            MerchantScopeUtils.assertAccessible(category.getMerchantId());
            dish.setMerchantId(category.getMerchantId());
        } else {
            dish.setMerchantId(null);
        }
        return dishMapper.list(dish);
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        if (schemaSupport.supportsDishScope()) {
            dish.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(dish.getMerchantId()));
        } else {
            dish.setMerchantId(null);
        }
        List<Dish> dishList = dishMapper.list(dish);
        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish item : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(item, dishVO);
            dishVO.setFlavors(dishFlavorMapper.getByDishId(item.getId()));
            fillDisplayImage(dishVO);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    private Dish getAccessibleDish(Long id) {
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            throw new BaseException("鑿滃搧涓嶅瓨鍦?");
        }
        MerchantScopeUtils.assertAccessible(dish.getMerchantId());
        return dish;
    }

    private void validateCategoryOwnership(Long categoryId, Long merchantId) {
        if (categoryId == null || !schemaSupport.supportsCategoryScope()) {
            return;
        }
        Category category = categoryMapper.getById(categoryId);
        if (category == null) {
            throw new BaseException("鍒嗙被涓嶅瓨鍦?");
        }
        if (merchantId != null && category.getMerchantId() != null && !merchantId.equals(category.getMerchantId())) {
            throw new BaseException("鍒嗙被涓嶅睘浜庡綋鍓嶅晢鎴?");
        }
    }

    private void fillDisplayImage(DishVO dishVO) {
        dishVO.setImage(storefrontImageResolver.resolve(dishVO.getImage()));
    }
}
