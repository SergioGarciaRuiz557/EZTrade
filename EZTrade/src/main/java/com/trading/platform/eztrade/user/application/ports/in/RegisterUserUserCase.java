package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.data.crossstore.ChangeSetPersister;

public interface RegisterUserUserCase {
    String registerUser(User user) throws ChangeSetPersister.NotFoundException;
}
