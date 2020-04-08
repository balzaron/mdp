package com.miotech.mdp.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.common.model.bo.UserInfo;
import com.miotech.mdp.common.model.dao.UsersEntity;
import com.miotech.mdp.common.persistent.UsersRepository;
import com.miotech.mdp.admin.vo.UserVO;
import com.miotech.mdp.quality.exception.ResourceNotFoundException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SecurityService implements InitializingBean {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();

    @Autowired
    UsersRepository usersRepository;

    public synchronized UserInfo saveUser(UserInfo userInfo) {
        if (!isNewUser(userInfo.getUsername())) {
            return getUser(userInfo.getUsername());
        }
        UsersEntity entity = new UsersEntity();
        entity.setUsername(userInfo.getUsername());
        UsersEntity savedEntity = usersRepository.saveAndFlush(entity);
        userCache.put(savedEntity.getUsername(), convertToUserInfo(savedEntity));
        return userCache.get(savedEntity.getUsername());
    }

    public boolean isNewUser(String username) {
        return !userCache.containsKey(username);
    }

    public List<UsersEntity> getUsers() {
        return usersRepository.findAll();
    }

    public UserInfo getUser(String username) {
        if (userCache.containsKey(username)) {
            return userCache.get(username);
        }
        return convertToUserInfo(usersRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist.")));
    }

    public UserInfo convertToUserInfo(UsersEntity usersEntity) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(usersEntity.getId());
        userInfo.setUsername(usersEntity.getUsername());
        return userInfo;
    }

    public List<UserVO> convertToUserVOs(List<UsersEntity> usersEntities) {
        return usersEntities.stream().map(usersEntity -> {
            UserVO userVO = new UserVO();
            userVO.setUsername(usersEntity.getUsername());
            userVO.setId(usersEntity.getId());
            return userVO;
        }).collect(Collectors.toList());
    }

    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            UserVO user = new UserVO();
            user.setUsername(authentication.getName());
            objectMapper.writeValue(response.getWriter(), Result.success("Login Successfully.", user));

            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(authentication.getName());
            saveUser(userInfo);
        };
    }

    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            objectMapper.writeValue(response.getWriter(), Result.error("Login Failed."));
        };
    }

    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            objectMapper.writeValue(response.getWriter(), Result.success("Logout Successfully."));
        };
    }

    @Override
    public void afterPropertiesSet() {
        userCache.putAll(getUsers().stream().collect(Collectors.toMap(UsersEntity::getUsername, this::convertToUserInfo)));
    }
}
