package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.utils.StorefrontImageResolver;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;

    private final DishMapper dishMapper;

    private final SetmealDishMapper setmealDishMapper;

    private final CategoryMapper categoryMapper;

    private final StorefrontImageResolver storefrontImageResolver;

    private final MultiMerchantSchemaSupport schemaSupport;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        if (schemaSupport.supportsSetmealScope()) {
            setmealPageQueryDTO.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(setmealPageQueryDTO.getMerchantId()));
        } else {
            setmealPageQueryDTO.setMerchantId(null);
        }
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Long merchantId = schemaSupport.supportsSetmealScope()
                ? MerchantScopeUtils.resolveRequiredMerchantId(setmealDTO.getMerchantId())
                : null;
        validateCategoryOwnership(setmealDTO.getCategoryId(), merchantId);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setMerchantId(merchantId);
        if (schemaSupport.supportsSetmealScope()) {
            setmealMapper.insert(setmeal);
        } else {
            setmealMapper.insertLegacy(setmeal);
        }

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null) {
            setmealDishes.forEach(item -> item.setSetmealId(setmeal.getId()));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = getAccessibleSetmeal(id);
        if (StatusConstant.ENABLE.equals(status)) {
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null) {
                for (Dish dish : dishList) {
                    if (StatusConstant.DISABLE.equals(dish.getStatus())) {
                        throw new BaseException("濂楅鍐呭寘鍚湭璧峰敭鑿滃搧锛屾棤娉曞惎鍞?");
                    }
                }
            }
        }
        setmealMapper.update(Setmeal.builder().id(setmeal.getId()).status(status).build());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = getAccessibleSetmeal(id);
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                throw new BaseException("璧峰敭涓殑濂楅涓嶈兘鍒犻櫎");
            }
        }
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBatch(ids);
    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = getAccessibleSetmeal(id);
        List<SetmealDish> dishes = setmealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal existing = getAccessibleSetmeal(setmealDTO.getId());
        Long merchantId = null;
        if (schemaSupport.supportsSetmealScope()) {
            merchantId = MerchantScopeUtils.resolveQueryMerchantId(setmealDTO.getMerchantId());
            if (merchantId == null) {
                merchantId = existing.getMerchantId();
            }
        }
        validateCategoryOwnership(setmealDTO.getCategoryId(), merchantId);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setId(existing.getId());
        setmeal.setMerchantId(merchantId);
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null) {
            setmealDishes.forEach(item -> item.setSetmealId(setmealDTO.getId()));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        if (schemaSupport.supportsSetmealScope()) {
            setmeal.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(setmeal.getMerchantId()));
        } else {
            setmeal.setMerchantId(null);
        }
        List<Setmeal> list = setmealMapper.list(setmeal);
        if (list != null) {
            list.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
        }
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        Setmeal setmeal = getAccessibleSetmeal(id);
        List<DishItemVO> list = setmealMapper.getDishItemBySetmealId(setmeal.getId());
        if (list != null) {
            list.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
        }
        return list;
    }

    private Setmeal getAccessibleSetmeal(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            throw new BaseException("濂楅涓嶅瓨鍦?");
        }
        MerchantScopeUtils.assertAccessible(setmeal.getMerchantId());
        return setmeal;
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
}
