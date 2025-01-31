package org.mrshoffen.cloudstorage.security.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.user.dto.StorageUserEditDto;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

//todo maybe there is a better way to update auth context in redis
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


    @EventListener
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserUpdatedEvent(StorageUser updatedUser) {

        StorageUserDetails storageUserDetails = new StorageUserDetails(updatedUser);
        UsernamePasswordAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(storageUserDetails, null, List.of());
        updateAuthInContext(authenticated);
    }

}
