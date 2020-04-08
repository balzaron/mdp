package com.miotech.mdp.flow.controller;

import com.miotech.mdp.flow.service.AirflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/airflow")
public class AirflowController {

    @Autowired
    AirflowService airflowService;

    @PostMapping("/createDAG/{id}")
    public void createDAG(@PathVariable String id) {
        airflowService.createDAG(id);
    }

    @DeleteMapping("/deleteDAG/{id}")
    public void deleteDAG(@PathVariable String id) {
        airflowService.deleteDAG(id);
    }

    @PostMapping("/pauseDAG/{id}")
    public void pauseDAG(@PathVariable String id) {
        airflowService.pauseDAG(id);
    }

    @PostMapping("/unpauseDAG/{id}")
    public void unpauseDAG(@PathVariable String id) {
        airflowService.unpauseDAG(id);
    }
}
