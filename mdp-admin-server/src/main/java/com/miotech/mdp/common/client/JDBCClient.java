package com.miotech.mdp.common.client;

import java.sql.Connection;

interface JDBCClient {
    boolean canConnect();

    Connection getConnection();

}
