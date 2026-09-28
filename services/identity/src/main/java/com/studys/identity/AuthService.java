package com.studys.identity;

import com.studys.db.entity.UserEntity;
import com.studys.db.repository.UserRepository;
import com.studys.schemas.auth.AuthResponse;
import com.studys.schemas.auth.LoginRequest;
import com.studys.schemas.auth.RegisterRequest;
import com.studys.schemas.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int SENHA_MINIMA = 8;
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final JwtProperties properties;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, JwtProperties properties) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.properties = properties;
    }

    @Transactional
    public UserResponse registrar(RegisterRequest request) {
        String nome = texto(request.getNome(), "Nome é obrigatório");
        String email = texto(request.getEmail(), "Email é obrigatório").toLowerCase();
        String senha = request.getSenha();
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new IllegalArgumentException("Senha deve ter no mínimo " + SENHA_MINIMA + " caracteres");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new EmailEmUsoException("Email já cadastrado");
        }
        UserEntity salvo = users.save(new UserEntity(nome, email, encoder.encode(senha)));
        return new UserResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getCriadoEm());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().toLowerCase();
        String senha = request.getSenha() == null ? "" : request.getSenha();
        UserEntity user = users.findByEmail(email)
                .filter(u -> encoder.matches(senha, u.getSenhaHash()))
                .orElseThrow(() -> new CredenciaisInvalidasException("Email ou senha inválidos"));
        String token = jwt.gerar(user.getId(), user.getEmail());
        return new AuthResponse(token, "Bearer", properties.getJwtExpirationMs() / 1000);
    }

    private String texto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }
}
