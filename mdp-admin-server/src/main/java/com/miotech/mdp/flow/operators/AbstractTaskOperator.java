package com.miotech.mdp.flow.operators;

import ch.qos.logback.classic.Logger;
import com.miotech.mdp.flow.entity.dao.TaskInstance;

public abstract class AbstractTaskOperator {
    public abstract void run(TaskInstance instance, Logger logger);
}
