package com.miotech.mdp.flow.util;

import com.google.common.collect.Maps;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.FlowListVO;
import com.miotech.mdp.flow.entity.vo.FlowRunListVO;
import com.miotech.mdp.flow.entity.vo.FlowVO;
import com.miotech.mdp.flow.entity.vo.TaskListVO;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.stream.Collectors;

public class Converter {
    public static Map<String,String> colorMap;
    static {
        colorMap = Maps.newHashMap();
        colorMap.put("Black", "#000000");
        colorMap.put("Red", "#FF0000");
        colorMap.put("Blue", "#0000FF");
        colorMap.put("Lime", "#00FF00");
        colorMap.put("White", "#FFFFFF");
        colorMap.put("Yellow", "#FFFF00");
        colorMap.put("Green", "#008000");
    }

    public static String convertStateToColor(String state) {
        if(state == null) return null;
        switch (state) {
            case "CREATED":
            case "ACCEPTED":
                return colorMap.get("Yellow");
            case "FINISHED":
            case "SUCCESS":
            case "SUCCEEDED":
                return colorMap.get("Green");
            case "RUNNING":
                return colorMap.get("Lime");
            case "FAILED":
                return colorMap.get("Red");
            case "KILLED":
                return colorMap.get("Black");
                default:
                    return colorMap.get("White");
        }
    }

    public static FlowVO convert2FlowVO(Flow flow) {
        FlowVO vo = new FlowVO();
        ModelMapper mapper = new ModelMapper();
        mapper.map(flow, vo);
        if (flow.getTags() != null) {
            vo.setTags(flow.getTags().stream()
                    .map(TagsEntity::getName)
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    public static FlowListVO convert2FlowList(Page<Flow> flowPage) {
        FlowListVO vo = new FlowListVO();
        vo.setFlows(flowPage.stream()
                .map(Converter::convert2FlowVO)
                .collect(Collectors.toList()));
        vo.setPageNum(flowPage.getNumber());
        vo.setPageSize(flowPage.getSize());
        vo.setTotalCount(flowPage.getTotalElements());
        return vo;
    }

    public static TaskListVO convert2TaskList(Page<TaskInstance> taskPage) {
        TaskListVO vo = new TaskListVO();
        vo.setTasks(taskPage.stream().collect(Collectors.toList()));
        vo.setPageNum(taskPage.getNumber());
        vo.setPageSize(taskPage.getSize());
        vo.setTotalCount(taskPage.getTotalElements());
        return vo;
    }


    public static FlowRunListVO convert2FlowRunList(Page<FlowRun> taskPage) {
        FlowRunListVO vo = new FlowRunListVO();
        vo.setRuns(taskPage.stream().collect(Collectors.toList()));
        vo.setPageNum(taskPage.getNumber());
        vo.setPageSize(taskPage.getSize());
        vo.setTotalCount(taskPage.getTotalElements());
        return vo;
    }
}
