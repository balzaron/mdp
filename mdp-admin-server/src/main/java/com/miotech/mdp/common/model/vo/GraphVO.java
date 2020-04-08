package com.miotech.mdp.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GraphVO<V extends VertexVO, E extends EdgeVO> {

    List<V> vertices;

    List<E> edges;

}
