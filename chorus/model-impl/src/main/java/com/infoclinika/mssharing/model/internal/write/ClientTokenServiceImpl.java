package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.ClientTokenService;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * @author Vladislav Kovchug
 */
@Service
public class ClientTokenServiceImpl implements ClientTokenService {

    private static final ShaPasswordEncoder encoder = new ShaPasswordEncoder();
    @Inject
    private UserRepository userRepository;


    @Override
    public ClientToken generateTokenForUser(long id) {
        final String clientToken = UUID.randomUUID().toString();
        final User user = userRepository.findOne(id);
        user.setClientToken(encoder.encodePassword(clientToken, null));
        userRepository.save(user);

        return new ClientToken(clientToken);
    }

    @Override
    public Long readUserByToken(ClientToken clientToken) {
        final User user = userRepository.findByClientToken(encoder.encodePassword(clientToken.token, null));
        return user != null ? user.getId() : null;
    }
}
