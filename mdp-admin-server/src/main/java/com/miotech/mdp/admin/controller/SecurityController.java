package com.miotech.mdp.admin.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.common.model.bo.UserInfo;
import com.miotech.mdp.admin.vo.UserVO;
import com.miotech.mdp.admin.security.SecurityService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api")
public class SecurityController {

    @Autowired
    SecurityService securityService;

    @ApiOperation("Only for api doc")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserInfo userInfo) {
        UserVO userVO = new UserVO();
        userVO.setUsername(userInfo.getUsername());
        return Result.success(userVO);
    }

    @ApiOperation("Only for api doc")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("Logout successfully.");
    }

    @ApiOperation("get current user info")
    @GetMapping("/user/me")
    public Result<UserVO> whoami() {
        UserVO userVO = new UserVO();
        userVO.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.success(userVO);
    }

    @ApiOperation("get all user info")
    @GetMapping("/users")
    public Result<List<UserVO>> users() {
        return Result.success(securityService.convertToUserVOs(securityService.getUsers()));
    }
}
