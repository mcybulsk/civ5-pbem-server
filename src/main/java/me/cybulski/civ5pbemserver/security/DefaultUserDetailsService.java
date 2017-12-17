package me.cybulski.civ5pbemserver.security;

import me.cybulski.civ5pbemserver.user.UserAccount;
import me.cybulski.civ5pbemserver.user.UserAccountApplicationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Michał Cybulski
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultUserDetailsService implements UserDetailsService {

    private final UserAccountApplicationService userAccountApplicationService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountApplicationService.findUserByEmail(username)
                       .map(this::convertToUser)
                       .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with username " + username));
    }

    public UserDetails loadUserByAccessToken(String accessToken) {
        return userAccountApplicationService.findUserByToken(accessToken)
                       .map(this::convertToUser)
                       .orElseThrow(() -> new BadCredentialsException("Wrong access token value"));
    }

    private User convertToUser(UserAccount userAccount) {
        ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(userAccount.getUsername(), userAccount.getCurrentAccessToken(), roles);
    }
}
