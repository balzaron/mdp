package com.miotech.mdp.graph.ontology.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyEdgeInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologySearchCondition;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexUpdate;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyEdgeVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologySummaryVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyVertexVO;
import com.miotech.mdp.graph.ontology.service.GraphOntologyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@ResponseBody
@RequestMapping("/api/graph/ontology")
@Api(tags = "graph ontology")
public class GraphOntologyController {

    @Autowired
    GraphOntologyService graphOntologyService;

    @ApiOperation("Summary graph")
    @GetMapping("/summary")
    public Result<GraphOntologySummaryVO> summary() {
        return Result.success(graphOntologyService.convertToSummaryVO(graphOntologyService.summary()));
    }

    @ApiOperation("Create graph vertex")
    @PostMapping("/vertex")
    public Result<GraphOntologyVertexVO> createVertex(@RequestBody GraphOntologyVertexInfo vertexInfo) {
        return Result.success(graphOntologyService.convertToVertexVO(graphOntologyService.createVertex(vertexInfo)));
    }

    @ApiOperation("Update graph vertex")
    @PutMapping("/vertex/{id}")
    public Result<GraphOntologyVertexVO> updateVertex(@PathVariable String id,
                                                      @RequestBody GraphOntologyVertexUpdate vertexUpdate) {
        return Result.success(graphOntologyService.convertToVertexVO(graphOntologyService.updateVertex(id, vertexUpdate)));
    }

    @ApiOperation("Delete graph vertex")
    @DeleteMapping("/vertex/{id}")
    public Result<Object> deleteVertex(@PathVariable String id) {
        graphOntologyService.deleteVertex(id);
        return Result.success();
    }

    @ApiOperation("Search graph")
    @PostMapping("/search")
    public Result<GraphOntologyVO> searchGraph(@RequestBody GraphOntologySearchCondition graphOntologySearchCondition) {
        if (graphOntologySearchCondition.getLayerNum() < 1) {
            graphOntologySearchCondition.setLayerNum(1);
        }
        return Result.success(graphOntologyService.convertToGraphVO(graphOntologyService.searchGraph(graphOntologySearchCondition)));
    }

    @ApiOperation("Create graph edge")
    @PostMapping("/edge")
    public Result<GraphOntologyEdgeVO> createEdge(@RequestBody GraphOntologyEdgeInfo edgeInfo) {
        return Result.success(graphOntologyService.convertToEdgeVO(graphOntologyService.createEdge(edgeInfo)));
    }

    @ApiOperation("Update graph edge")
    @PutMapping("/edge/{id}")
    public Result<GraphOntologyEdgeVO> updateEdge(@PathVariable String id,
                                                  @RequestBody GraphOntologyEdgeInfo edgeInfo) {
        return Result.success(graphOntologyService.convertToEdgeVO(graphOntologyService.updateEdge(id, edgeInfo)));
    }

    @ApiOperation("Delete graph edge")
    @DeleteMapping("/edge/{id}")
    public Result<Object> deleteEdge(@PathVariable String id) {
        graphOntologyService.deleteEdge(id);
        return Result.success();
    }

}
