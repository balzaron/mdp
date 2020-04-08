package com.miotech.mdp.common.log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miotech.mdp.common.model.bo.LogInfo;
import com.miotech.mdp.common.model.dao.AuditLogEntity;
import com.miotech.mdp.common.persistent.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LogService {

    @Autowired
    AuditLogRepository auditLogRepository;

    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_NUM * 2;
    private static final int MAX_POOL_SIZE = CPU_NUM * 2;
    private static final int QUEUE_LENGTH = 1024;

    private static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_LENGTH),
            new ThreadFactoryBuilder().setNameFormat("[audit-log-worker-%d]").build(),
            (r, executor) -> {
                log.error("The audit-log task has been rejected by Executors which has {} active workers {} queue-length", executor.getActiveCount(), QUEUE_LENGTH);
            }));

    public static ListenableFuture submit(Runnable auditTask) {
        return executorService.submit(auditTask);
    }

    public void saveAuditLog(LogInfo logInfo) {
        submit(() -> {
            AuditLogEntity entity = new AuditLogEntity();
            entity.setTopic(logInfo.getTopic());
            entity.setUserId(logInfo.getUserId());
            entity.setModelId(logInfo.getModelId());
            entity.setModel(logInfo.getModel());
            entity.setDetails(logInfo.getDetails());
            auditLogRepository.save(entity);
        });
    }
}
