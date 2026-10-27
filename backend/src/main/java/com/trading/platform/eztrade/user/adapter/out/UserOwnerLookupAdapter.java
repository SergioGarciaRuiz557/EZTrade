package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.api.UserOwnerLookupPort;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that exposes a minimal user lookup to other modules.
 */
@Component
class UserOwnerLookupAdapter implements UserOwnerLookupPort {

    private final GetUserUserCase getUserUserCase;

    UserOwnerLookupAdapter(GetUserUserCase getUserUserCase) {
        this.getUserUserCase = getUserUserCase;
    }

    @Override
    public Optional<String> findOwner(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(getUserUserCase.getUser(identifier.trim()).getEmail());
        } catch (UserNotFoundException ex) {
            return Optional.empty();
        }
    }
}
