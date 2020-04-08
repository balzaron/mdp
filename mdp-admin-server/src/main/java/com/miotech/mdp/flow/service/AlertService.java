package com.miotech.mdp.flow.service;

import com.miotech.mdp.common.client.ZhongdaClient;
import com.miotech.mdp.common.model.dao.UsersEntity;
import com.miotech.mdp.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private UserService userService;

    @Autowired
    private ZhongdaClient zhongdaClient;

    public void sendMessageToUser(String message, UsersEntity userName) {
        sendMessageToUsers(message, Collections.singletonList(userName));
    }

    public void sendMessageToUserIds(String message, List<String> userIds) {
        sendMessageToUsers(message, userService.findUsers(userIds));
    }

    public void sendMessageToUsers(String message, List<UsersEntity> userName) {
        zhongdaClient.sendMessage(
                message,
                "",
                userName.stream().map(UsersEntity::getUsername)
                        .collect(Collectors.toList()));

    }
}
