package com.miotech.mdp.table.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.mdp.common.constant.GraphDirection;
import com.miotech.mdp.common.log.AuditLog;
import com.miotech.mdp.common.log.EnableAuditLog;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.table.model.bo.*;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.vo.*;
import com.miotech.mdp.table.service.DatabaseService;
import com.miotech.mdp.table.service.TableColumnService;
import com.miotech.mdp.table.service.TableRevisionService;
import com.miotech.mdp.table.service.TableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api")
@Api(tags = "metadata management")
@EnableAuditLog
public class TableController {

    @Autowired
    DatabaseService databaseService;

    @Autowired
    TableService tableService;

    @Autowired
    TableColumnService tableColumnService;

    @Autowired
    TableRevisionService tableRevisionService;


    @ApiOperation("search table and return table list")
    @PostMapping("/table/search")
    @AuditLog(ignore = true)
    public Result<TableSearchVO> tableSearch(@RequestBody TableSearchCondition tableSearchCondition) {
        return Result.success(tableService.convertToTableSearchVO(tableService.searchTable(tableSearchCondition)));
    }

    @ApiOperation("get table by id")
    @GetMapping("/table/{id}")
    @AuditLog(ignore = true)
    public Result<TableVO> table(@PathVariable String id) {
        return Result.success(tableService.getTable(id));
    }

    @ApiOperation("get dbType list")
    @GetMapping("/database/types")
    @AuditLog(ignore = true)
    public Result<List<String>> databaseTypes() {
        return Result.success(databaseService.getDatabaseTypes());
    }

    @ApiOperation("get db list")
    @PostMapping("/databases")
    @AuditLog(ignore = true)
    public Result<List<DatabaseVO>> databases(@RequestBody DatabaseInfo databaseInfo) {
        return Result.success(databaseService.convertToDatabaseVOs(databaseService.getDatabases(databaseInfo)));
    }

    @ApiOperation("get table columns info")
    @PostMapping("/table/columns")
    @AuditLog(ignore = true)
    public Result<List<TableColumnVO>> tableColumns(@RequestBody TableColumnInfo tableColumnInfo) {
        try {
            return Result.success(tableColumnService.getTableColumns(tableColumnInfo));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("create table")
    @PostMapping("/table/create")
    @AuditLog(topic = "create", model = "com/miotech/mdp/table")
    public Result<TableVO> tableCreate(@RequestBody TableInfo tableInfo) {
        try {
            MetaTableEntity entity = tableService.createTable(tableInfo);
            return Result.success(tableService.getTable(entity.getId()));
        } catch (Exception e) {
            log.error("Failed to create table.", e);
            return Result.error(String.format("Failed to create table: %s" , e.getMessage()));
        }
    }

    @ApiOperation("update table")
    @PutMapping("/table/{id}")
    @AuditLog(topic = "update", model = "com/miotech/mdp/table")
    public Result<TableVO> tableUpdate(@PathVariable String id,
                                       @RequestBody TableUpdate tableUpdate) {
        return Result.success(tableService.convertToTableVO(tableService.updateTable(id, tableUpdate)));
    }

    @ApiOperation("sync table columns info from remote service")
    @PutMapping("/table/{id}/columns/sync")
    @AuditLog(topic = "sync-columns", model = "com/miotech/mdp/table")
    public Result<List<TableColumnVO>> tableColumnsSync(@PathVariable String id) {
        try {
            TableVO tableVO = tableService.getTable(id);

            tableRevisionService.createTableRevision(tableVO);
            List<TableColumnVO> columns = tableColumnService.syncTableColumnInfo(id);
            return Result.success(columns);
        } catch (JsonProcessingException e) {
            log.error("Failed to sync table.", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("create table lineage")
    @PostMapping("/table/lineage/create")
    @AuditLog(topic = "create", model = "lineage-edge")
    public Result<TableEdgeVO> tableLineageCreate(@RequestBody TableEdgeInfo tableEdgeInfo) {
        return Result.success(tableService.convertToTableEdgeVO(tableService.createTableLineage(tableEdgeInfo)));
    }

    @ApiOperation("update table lineage")
    @PutMapping("/table/lineage/{id}/update")
    @AuditLog(topic = "update", model = "lineage-edge")
    public Result<TableEdgeVO> tableLineageUpdate(@PathVariable String id,
                                                  @RequestBody TableEdgeUpdate tableEdgeUpdate) {
        return Result.success(tableService.convertToTableEdgeVO(tableService.updateTableLineage(id, tableEdgeUpdate)));
    }

    @ApiOperation("get table lineage")
    @GetMapping("/table/{id}/lineage")
    @AuditLog(ignore = true)
    public Result<TableLineageVO> tableLineage(@PathVariable String id,
                                               @RequestParam(name = "layerNum", required = false, defaultValue = "1") Integer layerNum,
                                               @RequestParam(name = "direction", required = false, defaultValue = "both") String direction) {

        if (layerNum < 1) {
            layerNum = 1;
        }
        if (GraphDirection.fromName(direction) == null) {
            return Result.error("Unknown direction type : " + direction);
        }
        TableLineageInfo tableLineageInfo = new TableLineageInfo();
        tableLineageInfo.setTableId(id);
        tableLineageInfo.setLayerNum(layerNum);
        tableLineageInfo.setDirection(GraphDirection.fromName(direction));
        return Result.success(tableService.convertToTableLineageVO(tableService.getTableLineage(tableLineageInfo)));
    }

    @ApiOperation("remove table lineage")
    @DeleteMapping("/table/lineage/remove")
    @AuditLog(topic = "delete", model = "lineage-edge", modelKey = "edge")
    public Result<TableEdgeVO> tableLineageRemove(@RequestParam(name = "edgeId") String edgeId) {
        return Result.success(tableService.convertToTableEdgeVO(tableService.deleteTableLineage(edgeId)));
    }
}


