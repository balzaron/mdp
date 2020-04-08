package com.miotech.mdp.common.service;

import com.miotech.mdp.common.exception.ResourceNotFoundException;
import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.common.model.bo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T> {

    @Autowired
    BaseRepository<T> jpaRepository = null;

    /**
     * 添加记录
     *
     * @param entity  保存对象
     * @return T 返回值
     */
    @Transactional
    public T save(T entity) {
        return jpaRepository.save(entity);
    }

    /**
     * 根据Id删除数据
     *
     * @param id 删除id
     */
    @Transactional
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }

    /**
     * 实体批量删除
     *
     * @param entities 删除对象
     */
    @Transactional
    public void delete(List<T> entities) {
        jpaRepository.deleteInBatch(entities);
    }

    /**
     * 根据Id更新数据
     *
     * @param entity
     * @tparam T
     * @return
     */
    @Transactional
    public T update(T entity) {
        return jpaRepository.save(entity);
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @tparam T
     * @return
     */
    public T find(String id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " id not found"));
    }


    /**
     * 根据example filter查询
     *
     * @param example
     * @tparam T
     * @return
     */
    public Optional<T> find(Example<T> example) {
        return jpaRepository.findOne(example);
    }

    /**
     * 查询所有数据
     *
     * @tparam T
     * @return
     */
    public List<T> findAll() {
        return jpaRepository.findAll();
    }

    /**
     * 集合Id查询数据
     *
     * @param ids
     * @tparam S
     * @return
     */
    public List<T> findAll(List<String> ids) {
        return jpaRepository.findAllById(ids);
    }

    /**
     * 统计大小
     *
     * @return
     */
    public long count() {
            return jpaRepository.count();
    }

    /**
     * 判断数据是否存在
     *
     * @param id
     * @return
     */
    public boolean exists(String id) {
        return jpaRepository.existsById(id);
    }

    /**
     * 查询分页
     *
     * @param page     起始页
     * @param pageSize 每页大小
     * @tparam S
     * @return
     */
    public Page<T> page(Integer page, Integer pageSize, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sortField).descending());

        return jpaRepository.findAll(pageRequest);
    }

    public Page<T> page(Specification<T> specification, Pageable pageable) {
        return jpaRepository.findAll(specification, pageable);
    }

    public Page<T> page(PageRequest pageRequest) {
        return jpaRepository.findAll(pageRequest);
    }


    public UserInfo getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((UserInfo) authentication.getDetails());
    }
}
