package gt.app.config.security;

import gt.app.domain.User;
import gt.app.modules.user.UserService;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AppKeycloakAuthProvider extends KeycloakAuthenticationProvider {
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    final UserService userService;

    public AppKeycloakAuthProvider(UserService userService) {
        this.userService = userService;
    }

    public void setGrantedAuthoritiesMapper(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;

        IDToken idToken = token.getAccount().getKeycloakSecurityContext().getIdToken();

        String id = idToken.getSubject();
        String userId = idToken.getPreferredUsername();
        String firstName = idToken.getGivenName();
        String lastName = idToken.getFamilyName();

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (String role : token.getAccount().getRoles()) {
            grantedAuthorities.add(new KeycloakRole(role));
        }

        User user = new User(id, userId, firstName, lastName);
        userService.updateUserIfNeeded(user);

        AppUserDetails userDetails = new AppUserDetails(user, grantedAuthorities);

        return new CurrentUserToken(token.getAccount(), token.isInteractive(), mapAuthorities(grantedAuthorities), userDetails);

    }

    private Collection<? extends GrantedAuthority> mapAuthorities(
        Collection<? extends GrantedAuthority> authorities) {
        return grantedAuthoritiesMapper != null
            ? grantedAuthoritiesMapper.mapAuthorities(authorities)
            : authorities;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return KeycloakAuthenticationToken.class.isAssignableFrom(aClass);
    }

}
