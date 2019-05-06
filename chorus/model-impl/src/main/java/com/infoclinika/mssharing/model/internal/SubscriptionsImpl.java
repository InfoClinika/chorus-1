package com.infoclinika.mssharing.model.internal;

import com.infoclinika.mssharing.model.Subscriptions;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@Service
@Transactional
public class SubscriptionsImpl implements Subscriptions {

    @Inject
    private UserRepository userRepository;

    @Override
    public Subscription get(long actor) {
        User user = userRepository.findOne(actor);
        return new Subscription(user.getSubscription().getStatus(), user.getSubscription().getLastStatusChange());
    }

    @Override
    public void update(long actor, Subscription.Status status) {
        User user = userRepository.findOne(actor);
        user.getSubscription().setStatus(status);
        user.getSubscription().setLastStatusChange(new Date());
        userRepository.save(user);
    }
}
