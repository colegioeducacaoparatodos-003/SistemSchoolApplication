package com.SistemSchool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.SistemSchool.io.Perfil;
import com.SistemSchool.model.User;
import com.SistemSchool.modulo_dashboard_charts.dto.ProfileCountDTO;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT * FROM tb_user WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);

    @Query(value = "SELECT * FROM tb_user WHERE active = true ORDER BY user_creation_date DESC", nativeQuery = true)
    List<User> findActiveUsersNative();

    @Query(value = "SELECT * FROM tb_user WHERE perfil = :perfil", nativeQuery = true)
    List<User> findByPerfilNative(@Param("perfil") String perfil);

    @Query(value = "SELECT * FROM tb_user WHERE fk_person = :personId", nativeQuery = true)
    Optional<User> findByPersonIdNative(@Param("personId") int personId);

    @Query(value = "SELECT * FROM tb_user WHERE user_creation_date > :date", nativeQuery = true)
    List<User> findUsersCreatedAfterDateNative(@Param("date") Date date);

    @Modifying
    @Transactional
    @Query(value = "UPDATE tb_user SET active = :active, user_modification_date = NOW() WHERE pk_user = :userId", nativeQuery = true)
    int updateUserStatusNative(@Param("userId") int userId, @Param("active") boolean active);

    @Modifying
    @Transactional
    @Query(value = "UPDATE tb_user SET device_token = :deviceToken, user_modification_date = NOW() WHERE pk_user = :userId", nativeQuery = true)
    int updateDeviceTokenNative(@Param("userId") int userId, @Param("deviceToken") String deviceToken);

    @Query(value = "SELECT COUNT(*) FROM tb_user WHERE perfil = :perfil AND active = true", nativeQuery = true)
    long countActiveUsersByPerfilNative(@Param("perfil") String perfil);

    // Login: já não expomos password/salt separadamente — com BCrypt basta o hash
    @Query(value = "SELECT pk_user, password FROM tb_user WHERE email = :email AND active = true", nativeQuery = true)
    List<Object[]> findUserCredentialsNative(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM tb_user WHERE email = :email)", nativeQuery = true)
    int existsByEmailNative(@Param("email") String email);

    Optional<User> findByEmail(String email);

    List<User> findByActive(boolean active);

    List<User> findByPerfil(Perfil perfil);

    boolean existsByEmail(String email);

    @Modifying
    @Query(value = "UPDATE tb_user u SET u.fk_person = :userId where u.pk_user = :id", nativeQuery = true)
    void updateFkPerson(@Param("id") int id, @Param("userId") int userId);

    @Query("SELECT new com.SistemSchool.modulo_dashboard_charts.dto.ProfileCountDTO(u.perfil, COUNT(u)) " +
            "FROM User u " +
            "WHERE u.active = true " +
            "GROUP BY u.perfil")
    List<ProfileCountDTO> countUsersByPerfil();

    long countByActiveTrue();
}