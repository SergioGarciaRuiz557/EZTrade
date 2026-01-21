package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;

public interface GetUserUserCase {
    User getUser(String username) throws UserNotFoundException;
}
