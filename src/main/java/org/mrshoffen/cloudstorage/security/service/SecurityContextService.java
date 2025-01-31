package org.mrshoffen.cloudstorage.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository contextRepository;

    public void saveAuthToContext(Authentication authentication) {
        ServletRequestAttributes attributes = getRequestAttributes();

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        contextRepository.saveContext(context, attributes.getRequest(), attributes.getResponse());
    }

    public void updateAuthInContext(Authentication authentication) {
        ServletRequestAttributes attributes = getRequestAttributes();
        SecurityContext context = securityContextHolderStrategy.getContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        contextRepository.saveContext(context, attributes.getRequest(), attributes.getResponse());
    }

    private ServletRequestAttributes getRequestAttributes() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request context is not available");
        }
        return attributes;
    }


//    public void handleUserUpdatedEvent(UserUpdateEvent event) {
//        StorageUserDetails storageUserDetails = new StorageUserDetails(event.getUser());
//        UsernamePasswordAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(storageUserDetails, null, List.of());
//        updateAuthInContext(authenticated);
//    }

}
