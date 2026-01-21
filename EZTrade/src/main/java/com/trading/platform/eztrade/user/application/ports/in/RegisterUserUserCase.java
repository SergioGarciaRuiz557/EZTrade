package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.springframework.data.crossstore.ChangeSetPersister;

public interface RegisterUserUserCase {
    User registerUser(User user) throws UserExistsException;
}
