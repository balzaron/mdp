package com.miotech.mdp.common.service;

import com.miotech.mdp.common.model.dao.UsersEntity;
import com.miotech.mdp.common.persistent.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    public List<UsersEntity> findUsers(List<String> userIds) {
        return usersRepository.findAllById(userIds);
    }
}
