package com.SistemSchool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;
import com.SistemSchool.mapper.UserMapper;
import com.SistemSchool.model.User;
import com.SistemSchool.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO.UserResponseDTO createUser(UserDTO.CreateUserDTO createUserDTO) {
        logger.info("Criando novo usuário com email: {}", createUserDTO.getEmail());

        if (userRepository.existsByEmailNative(createUserDTO.getEmail()) > 0) {
            throw new RuntimeException("Email já está em uso");
        }

        User user = userMapper.toEntity(createUserDTO);
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        User savedUser = userRepository.save(user);
        logger.info("Usuário criado com ID: {}", savedUser.getPkUser());

        return userMapper.toResponseDTO(savedUser);
    }

    public UserDTO.UserResponseDTO getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));
        return userMapper.toResponseDTO(user);
    }

    public List<UserDTO.UserResponseDTO> getAllActiveUsers() {
        return userRepository.findActiveUsersNative().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO.UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmailNative(email).map(userMapper::toResponseDTO);
    }

    public UserDTO.UserResponseDTO updateUser(UserDTO.UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(updateUserDTO.getPkUser())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + updateUserDTO.getPkUser()));

        if (updateUserDTO.getEmail() != null &&
                !updateUserDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmailNative(updateUserDTO.getEmail()) > 0) {
            throw new RuntimeException("Novo email já está em uso");
        }

        userMapper.updateFromDTO(user, updateUserDTO);

        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }

    public void updateUserStatus(int userId, boolean active) {
        int updated = userRepository.updateUserStatusNative(userId, active);
        if (updated == 0) {
            throw new RuntimeException("Usuário não encontrado com ID: " + userId);
        }
    }

    public void updateDeviceToken(int userId, String deviceToken) {
        int updated = userRepository.updateDeviceTokenNative(userId, deviceToken);
        if (updated == 0) {
            throw new RuntimeException("Usuário não encontrado com ID: " + userId);
        }
    }

    public UserDTO.UserResponseDTO authenticate(UserDTO.LoginDTO loginDTO) {
        List<Object[]> results = userRepository.findUserCredentialsNative(loginDTO.getEmail());

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Credenciais inválidas");
        }

        Object[] credentials = results.get(0);
        int userId = ((Number) credentials[0]).intValue();
        String storedPasswordHash = (String) credentials[1];

        if (!passwordEncoder.matches(loginDTO.getPassword(), storedPasswordHash)) {
            throw new RuntimeException("Credenciais inválidas");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        logger.info("Usuário autenticado com sucesso: {}", userId);
        return userMapper.toResponseDTO(user);
    }

    public List<UserDTO.UserResponseDTO> getUsersByPerfil(Perfil perfil) {
        return userRepository.findByPerfil(perfil).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public long countActiveUsersByPerfil(Perfil perfil) {
        return userRepository.countActiveUsersByPerfilNative(perfil.name());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailNative(email) > 0;
    }

    public List<UserDTO.UserResponseDTO> getUsersCreatedAfter(Date date) {
        return userRepository.findUsersCreatedAfterDateNative(date).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateFkPerson(int pkUser, Integer pkPerson) {
        if (pkUser <= 0 || pkPerson == null || pkPerson <= 0) {
            throw new IllegalArgumentException("IDs inválidos para atualização de fk_person");
        }
        userRepository.updateFkPerson(pkUser, pkPerson);
    }

    public boolean existsAnyAdmin() {
        return userRepository.countAdminUsersNative() > 0;
    }

    @Transactional(readOnly = true)
    public long countAdmins() {
        String sql = "SELECT COUNT(*) FROM tb_user WHERE perfil = 'ADMIN' AND active = true";
        Number result = (Number) entityManager.createNativeQuery(sql).getSingleResult();
        return result.longValue();
    }

    // ========== LAZY MODEL — PAGINAÇÃO MANUAL VIA ENTITYMANAGER ==========

    @Transactional(readOnly = true)
    public Page<UserDTO.UserResponseDTO> findUsersPage(int first, int pageSize, String sortField,
            org.primefaces.model.SortOrder sortOrder, String filterEmail, Perfil filterPerfil, Boolean filterActive) {

        // Mapeia camelCase (entidade) → snake_case (banco)
        String field = (sortField != null && !sortField.isBlank()) ? sortField : "pk_user";
        field = switch (field) {
            case "pkUser" -> "pk_user";
            case "userCreationDate" -> "user_creation_date";
            case "userModificationDate" -> "user_modification_date";
            case "fkPerson" -> "fk_person";
            case "fkCustomer" -> "fk_customer";
            case "deviceToken" -> "device_token";
            default -> field;
        };

        String direction = (sortOrder != null && sortOrder == org.primefaces.model.SortOrder.DESCENDING)
                ? "DESC" : "ASC";

        // --- COUNT ---
        String countSql = "SELECT COUNT(*) FROM tb_user WHERE " +
                "(:email IS NULL OR LOWER(email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
                "(:perfil IS NULL OR perfil = :perfil) AND " +
                "(:active IS NULL OR active = :active)";

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("email", (filterEmail == null || filterEmail.isBlank()) ? null : filterEmail);
        countQuery.setParameter("perfil", filterPerfil != null ? filterPerfil.name() : null);
        countQuery.setParameter("active", filterActive);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // --- DATA ---
        String sql = "SELECT * FROM tb_user WHERE " +
                "(:email IS NULL OR LOWER(email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
                "(:perfil IS NULL OR perfil = :perfil) AND " +
                "(:active IS NULL OR active = :active) " +
                "ORDER BY " + field + " " + direction + " " +
                "LIMIT :limit OFFSET :offset";

        Query query = entityManager.createNativeQuery(sql, User.class);
        query.setParameter("email", (filterEmail == null || filterEmail.isBlank()) ? null : filterEmail);
        query.setParameter("perfil", filterPerfil != null ? filterPerfil.name() : null);
        query.setParameter("active", filterActive);
        query.setParameter("limit", pageSize);
        query.setParameter("offset", first);

        @SuppressWarnings("unchecked")
        List<User> resultList = query.getResultList();

        List<UserDTO.UserResponseDTO> content = resultList.stream()
                .map(userMapper::toResponseDTO)
                .toList();

        return new PageImpl<>(content, PageRequest.of(first / pageSize, pageSize), total);
    }

    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }
}