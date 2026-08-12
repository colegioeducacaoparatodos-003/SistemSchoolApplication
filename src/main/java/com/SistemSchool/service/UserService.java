package com.SistemSchool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;
import com.SistemSchool.mapper.UserMapper;
import com.SistemSchool.model.User;
import com.SistemSchool.repository.UserRepository;

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

        // Se veio uma nova password no DTO de update, tem de passar pelo encoder aqui
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

    // ========== AUTENTICAÇÃO (agora com verificação real) ==========
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
}