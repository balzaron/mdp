package com.miotech.mdp.graph.ontology.model.vo;


import com.miotech.mdp.common.model.vo.GraphVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GraphOntologyVO extends
        GraphVO<GraphOntologyVertexVO, GraphOntologyEdgeVO> {
}
