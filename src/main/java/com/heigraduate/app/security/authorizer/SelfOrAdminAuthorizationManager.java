package com.heigraduate.app.security.authorizer;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class SelfOrAdminAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String PATH_VARIABLE = "id";

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        var authentication = authenticationSupplier.get();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User principal)) {
            return new AuthorizationDecision(false);
        }

        if (UserRole.ADMIN.equals(principal.getRole())) {
            return new AuthorizationDecision(true);
        }

        var requestedId = context.getVariables().get(PATH_VARIABLE);
        var isSelf = requestedId != null && requestedId.equals(principal.getId().toString());
        return new AuthorizationDecision(isSelf);
    }
}