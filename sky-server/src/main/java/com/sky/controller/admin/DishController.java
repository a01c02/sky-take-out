package com.sky.controller.admin;

import com.github.pagehelper.PageHelper;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.dto.DishDTO;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        //清除所有缓存数据,所有以dish开头的key
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //清除所有缓存数据,所有以dish开头的key
        cleanCache("dish_*");
        return Result.success();
    }
    /**
     * 批量起售停售
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        //清除所有缓存数据，所有以dish开头的key
        cleanCache("dish_*");
        return Result.success();
    }
    /**
     * 清理缓存数据
     */
    private void cleanCache(String pattern) {
        //清理所有缓存数据,所有以dish开头的key
        Set keys=redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
