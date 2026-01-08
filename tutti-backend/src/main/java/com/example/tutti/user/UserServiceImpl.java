package com.example.tutti.user;

import com.example.tutti.exception.BadRequestException;
import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.membership.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipService membershipService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Użytkownik o id " + userId + " nie istnieje"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Użytkownik o email " + email + " nie istnieje"));
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Użytkownik z tym emailem już istnieje");
        }
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Użytkownik o id " + userId + " nie istnieje"));

        if (request.getRole() != null && user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            checkIfLastAdmin(userId);
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email " + request.getEmail() + " jest już zajęty");
            }
            user.setEmail(request.getEmail().toLowerCase().trim());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getRole() != null) user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        validateUserDeletion(user);

        if (user.getMemberships() != null) {
            user.getMemberships().forEach(membershipService::releaseResources);
        }

        userRepository.delete(user);
    }

    private void validateUserDeletion(User user) {
        if (!user.getOwnedOrchestras().isEmpty()) {
            throw new IllegalStateException("Użytkownik jest właścicielem orkiestry - najpierw przekaż własność.");
        }
        if (user.getRole() == Role.ADMIN) {
            checkIfLastAdmin(user.getId());
        }
    }

    private void checkIfLastAdmin(Long userId) {
        long adminCount = userRepository.countByRoleAndIsActiveTrue(Role.ADMIN);
        if (adminCount <= 1) {
            throw new BadRequestException("Nie można usunąć ostatniego administratora!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
}