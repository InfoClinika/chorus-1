package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.RestHelper;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.RestToken;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.internal.repository.RestTokenRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.function.Function;

import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature.ANALYSE_STORAGE;

@Service
public class RestHelperImpl implements RestHelper {

    @Inject
    private UserRepository userRepository;
    @Inject
    private RestTokenRepository restTokenRepository;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;

    private static Function<User, UserDetails> userDetailsTransformer = user -> {
        if (user == null) {
            return null;
        }

        RestToken restToken = user.getRestToken();
        final Token token = restToken != null ? new Token(
            restToken.getId(),
            restToken.getToken(),
            restToken.getExpirationDate()
        ) : null;
        final boolean hasLaboratories = user.getLabs().size() > 0;
        return new UserDetails(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            token,
            user.isEmailVerified(),
            hasLaboratories
        );
    };

    @Override
    public void updateToken(Token token) {
        RestToken restToken = restTokenRepository.findOne(token.id);
        restToken.setExpirationDate(new Date(new Date().getTime() + RestHelper.TOKEN_EXPIRATION_TIME));
        restTokenRepository.save(restToken);
    }

    @Override
    public void invalidateToken(Token token) {
        RestToken restToken = restTokenRepository.findOne(token.id);
        restToken.setExpirationDate(null);
        restTokenRepository.save(restToken);
    }

    @Override
    public boolean canUploadForInstrument(long instrumentId) {
        final Instrument instrument = instrumentRepository.findOne(instrumentId);
        final long labId = instrument.getLab().getId();
        return billingFeaturesHelper.isFeatureEnabled(labId, ANALYSE_STORAGE);
    }

    @Override
    public UserDetails getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return userDetailsTransformer.apply(user);
    }

    @Override
    public UserDetails getUserDetails(long id) {
        User user = userRepository.findOne(id);
        return userDetailsTransformer.apply(user);
    }

    @Override
    public Token findAndProlongToken(String email) {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }
        final RestToken restToken = restTokenRepository.findOne(user.getId());
        if (restToken == null) {
            return null;
        } else {
            Date expirationDate = new Date(new Date().getTime() + RestHelper.TOKEN_EXPIRATION_TIME);
            restToken.setExpirationDate(expirationDate);
            restTokenRepository.save(restToken);
            return new Token(restToken.getId(), restToken.getToken(), restToken.getExpirationDate());
        }
    }

    @Override
    public UserDetails checkToken(String token) {
        RestToken restToken = restTokenRepository.findByToken(token);
        if (restToken == null || restToken.getExpirationDate().compareTo(new Date()) <= 0) {
            return null;
        }
        User user = userRepository.findOne(restToken.getId());
        return userDetailsTransformer.apply(user);
    }

    @Override
    public Token generateToken(UserDetails userDetails) {
        String rawString = userDetails.email + new Date().toString() + userDetails.passwordHash + userDetails.id;
        String token = passwordEncoder.encode(rawString);
        Date expirationDate = new Date(new Date().getTime() + RestHelper.TOKEN_EXPIRATION_TIME);
        RestToken restToken =
            new RestToken(userDetails.id, token, expirationDate);
        restTokenRepository.save(restToken);
        return new Token(restToken.getId(), restToken.getToken(), restToken.getExpirationDate());
    }

    @Override
    public UserDetails getUserDetailsByToken(String token) {
        RestToken restToken = restTokenRepository.findByToken(token);
        if (restToken == null) {
            return null;
        }
        User user = userRepository.findOne(restToken.getId());
        return userDetailsTransformer.apply(user);
    }

}

