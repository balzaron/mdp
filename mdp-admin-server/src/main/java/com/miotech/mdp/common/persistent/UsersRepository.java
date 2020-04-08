package com.miotech.mdp.common.persistent;

import com.miotech.mdp.common.model.dao.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, String> {

    @Query("SELECT u from UsersEntity u where u.username = ?1")
    Optional<UsersEntity> findByUsername(String userName);
}
