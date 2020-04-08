package com.miotech.mdp.flow.util;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;

@Component
@Slf4j
public class SSHUtil {

    private static Resource PUBLIC_KEY;

    private static long TIME_OUT;

    /**
     * 根据公钥进行SSH连接
     */
    public static Connection loginByPublicKey(String hostname, int port, String username, String password) {
        //获取连接
        Connection conn = new Connection(hostname, port);
        try {
            // 连接
            conn.connect();
            // 通过publicKey file登录
            boolean isAuthenticated = conn.authenticateWithPublicKey(username, PUBLIC_KEY.getFile(), password);
            //登陆失败，返回错误
            if (!isAuthenticated) {
                log.error("SSH authenticate failed");

            }
        } catch (IOException e) {
            throw new RuntimeException("Login remote host failed.", e);
        }
        return conn;
    }

    public static void execute(Connection connection, String command) {
        InputStream stdOut;
        InputStream stdErr;
        String outStr;
        String outErr;
        if (connection != null) {
            try {
                Session session = connection.openSession();
                session.execCommand(command);
                session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
                stdOut = new StreamGobbler(session.getStdout());
                outStr = processStream(stdOut, Charset.defaultCharset().toString());

                stdErr = new StreamGobbler(session.getStderr());
                outErr = processStream(stdErr, Charset.defaultCharset().toString());

                if (StringUtils.isNotEmpty(outStr)) {
                    log.info("SSH execute stdout:\n" + outStr);
                }
                if (StringUtils.isNotEmpty(outErr)) {
                    log.error("SSH execute stderr:\n" + outErr);
                }

                int execStatus = session.getExitStatus();
                if (execStatus != 0) {
                    throw new RuntimeException("Execute command failed. " + outErr);
                }
            } catch (Exception e) {
                throw new RuntimeException("Execute command failed.", e);
            }
        }
    }

    private static String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readLength = in.read(buf);
            if (readLength <= 0) {
                break;
            }
            sb.append(new String(buf, 0, readLength, charset));
        }
        return sb.toString();
    }

    @Value("classpath:id_rsa")
    public void setPublicKeyFile(Resource publicKey) {
        PUBLIC_KEY = publicKey;
    }

    @Value("${ssh.time-out}")
    public void setTimeOut(Duration timeOut) {
        TIME_OUT = timeOut.toMillis();
    }

}
