package com.miotech.mdp.quality;

import com.miotech.mdp.MiotechMDPServer;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author: shanyue.gao
 * @date: 2020/3/27 9:42 AM
 */

@SpringBootTest(classes = MiotechMDPServer.class)
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
public abstract class BaseTest {
}
