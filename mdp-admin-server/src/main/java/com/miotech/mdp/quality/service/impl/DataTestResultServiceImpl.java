package com.miotech.mdp.quality.service.impl;

import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.test.DataTestResultPageVo;
import com.miotech.mdp.quality.models.vo.test.DataTestResultVo;
import com.miotech.mdp.quality.exception.RequestAttributeErrorException;
import com.miotech.mdp.quality.exception.ResourceNotFoundException;
import com.miotech.mdp.quality.repository.DataTestResultRepository;
import com.miotech.mdp.quality.service.DataTestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: shanyue.gao
 * @date: 2020/1/6 1:11 PM
 */

@Service
public class DataTestResultServiceImpl implements DataTestResultService {

    @Autowired
    private DataTestResultRepository dataTestResultRepository;

    @Override
    public Page<DataTestResult> findAll(Integer pageNum, Integer pageSize, String sortField) {

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(sortField).descending());

        return Optional.of(pageable)
                .map(c -> dataTestResultRepository.findAll(c))
                .orElseThrow(() -> new RequestAttributeErrorException("Request parameter may be incorrect!"));
    }

    @Override
    public DataTestResult insert(DataTestResultVo dataTestResultVo) {
        DataTestResult dataTestResult = new DataTestResult();

        return Optional.ofNullable(dataTestResultVo)
                .map(c -> {
                    dataTestResult.setCaseId(dataTestResultVo.getCaseId())
                            .setErrorCatchLog(dataTestResultVo.getErrorCatchLog())
                            .setPassed(dataTestResultVo.getPassed());
                    return dataTestResultRepository.saveAndFlush(dataTestResult);
                })
                .orElseThrow(() -> new RequestAttributeErrorException("request payload is incorrect!"));
    }


    @Override
    public DataTestResult findById(String id) {
        return dataTestResultRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No id " + id + "found!"));
    }

    @Override
    public DataTestResultPageVo findByCaseId(String id, Integer pageNum, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("id").descending());
        Page<DataTestResult> dataTestResults = dataTestResultRepository.findAllByCaseId(id, pageable);
        List<DataTestResultVo> dataTestResultVos = dataTestResults.stream().map(a -> {
            DataTestResultVo v = new DataTestResultVo();
            v.setCaseId(a.getCaseId())
                    .setErrorCatchLog(a.getErrorCatchLog())
                    .setPassed(a.getPassed())
                    .setId(a.getId());
            return v;
        }).collect(Collectors.toList());
        DataTestResultPageVo pageVo = new DataTestResultPageVo().setDataTestResultVos(dataTestResultVos);
        pageVo.setPageNum(dataTestResults.getNumber());
        pageVo.setPageSize(dataTestResults.getSize());
        pageVo.setTotalCount(dataTestResults.getTotalElements());
        return pageVo;
    }
}
