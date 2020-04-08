package com.miotech.mdp.quality.util;

import cn.hutool.http.HttpStatus;
import com.miotech.mdp.common.model.BaseEntity;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.common.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

/**
 * @author: shanyue.gao
 * @date: 2020/3/11 7:30 PM
 */
public abstract class BaseController<T extends BaseEntity> {

    private static final String SUCCESS = "success";
    private static final String NO_ENTITY_FOUND = "No entity found";
    @Autowired
    private BaseService<T> baseService = null;

    @GetMapping("/{id}")
    public Result<T> getById(@PathVariable String id) {
        return Optional.ofNullable(baseService.find(id))
                .map(BaseController::getSuccessResult)
                .orElseGet(BaseController::getNullResult);
    }

    @GetMapping("/")
    public Result<T> getByObject(T object){
        return baseService.find(Example.of(object))
                .map(BaseController::getSuccessResult)
                .orElseGet(BaseController::getNullResult);
    }

    @DeleteMapping("/del/{id}")
    public Result<T> deleteById(@PathVariable String id) {
        baseService.delete(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<T> updateById(@RequestBody @Valid T object) {
        return Optional.ofNullable(object.getId())
                .map(i -> BaseController.getSuccessResult(baseService.update(object)))
                .orElseGet(BaseController::getNullResult);
    }

    @PostMapping("/")
    public Result<T> create(@RequestBody @Valid T object) {
        return Optional.ofNullable(object.getId())
                .map(i -> BaseController.getSuccessResult(baseService.save(object)))
                .orElseGet(BaseController::getNullResult);
    }

    @GetMapping("/list")
    public Result<Page<T>> list(@RequestParam @NotBlank int pageNum,
                                @RequestParam @NotBlank int pageSize,
                                @RequestParam String sortedField) {
        return Result.success(baseService.page(pageNum, pageSize, sortedField));
    }

    private static<T> Result<T> getSuccessResult(T o) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMessage(SUCCESS);
        r.setResult(o);
        return r;
    }

    private static<T> Result<T> getNullResult() {
        Result<T> r = new Result<>();
        r.setCode(HttpStatus.HTTP_NOT_FOUND);
        r.setResult(null);
        r.setMessage(NO_ENTITY_FOUND);
        return r;
    }
}
