package org.adamalang.frontend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.adamalang.ErrorCodes;
import org.adamalang.api.*;
import org.adamalang.extern.ExternNexus;
import org.adamalang.mysql.frontend.Role;
import org.adamalang.mysql.frontend.Spaces;
import org.adamalang.mysql.frontend.Users;
import org.adamalang.runtime.contracts.ExceptionLogger;
import org.adamalang.runtime.exceptions.ErrorCodeException;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

public class RootHandlerImpl implements RootHandler {
    private final ExternNexus nexus;
    private final SecureRandom rng;
    private final ExceptionLogger logger;

    public RootHandlerImpl(ExternNexus nexus) throws Exception {
        this.nexus = nexus;
        this.rng = SecureRandom.getInstanceStrong();
        this.logger = nexus.makeLogger(RootHandler.class);
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int j = 0; j < 2; j++) {
            int val = rng.nextInt(26 * 26 * 26 * 26);
            for (int k = 0; k < 4; k++) {
                code.append(('A' + (val % 26)));
                val /= 26;
            }
        }
        return code.toString();
    }

    @Override
    public void handle(ProbeRequest request, SimpleResponder responder) {
        responder.complete();
    }

    @Override
    public WaitingForEmailHandler handle(InitStartRequest startRequest, SimpleResponder startResponder) {
        String generatedCode = generateCode();
        return new WaitingForEmailHandler() {

            @Override
            public void bind() {
                nexus.email.sendCode(startRequest.email, generatedCode);
            }

            @Override
            public void handle(InitGenerateIdentityRequest request, InitiationResponder responder) {
                try {
                    if (generatedCode.equals(request.code)) {
                        KeyPair pair = Keys.keyPairFor(SignatureAlgorithm.ES256);
                        String publicKey = new String(Base64.getEncoder().encode(pair.getPublic().getEncoded()));
                        try {
                            Users.addKey(nexus.base, startRequest.userId, publicKey, new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60));
                        } catch (Exception ex) {
                            responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_INIT_GENERATE_UNKNOWN_EXCEPTION, ex, logger));
                            return;
                        }
                        responder.complete(Jwts.builder().setSubject("" + startRequest.userId).setIssuer("adama").signWith(pair.getPrivate()).compact());
                    } else {
                        responder.error(new ErrorCodeException(ErrorCodes.API_INIT_GENERATE_CODE_MISMATCH));
                    }
                } finally {
                    startResponder.complete();
                }
            }

            @Override
            public void handle(InitRevokeAllRequest request, SimpleResponder responder) {
                if (generatedCode.equals(request.code)) {
                    try {
                        Users.removeAllKeys(nexus.base, startRequest.userId);
                    } catch (Exception ex) {
                        responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_INIT_REVOKE_ALL_UNKNOWN_EXCEPTION, ex, logger));
                        return;
                    }
                    responder.complete();
                } else {
                    responder.error(new ErrorCodeException(ErrorCodes.API_INIT_REVOKE_ALL_CODE_MISMATCH));
                }
            }

            @Override
            public void disconnect(long id) {
            }
        };
    }
    @Override
    public void handle(AuthorityCreateRequest request, ClaimResultResponder responder) {

    }

    @Override
    public void handle(AuthoritySetRequest request, ClaimResultResponder responder) {

    }

    @Override
    public void handle(AuthorityTransferRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(AuthorityListRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(AuthorityDestroyRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(SpaceCreateRequest request, SimpleResponder responder) {
        try {
            Spaces.createSpace(nexus.base, request.who.id, request.space);
            responder.complete();
        } catch (Exception ex) {
            ex.printStackTrace(); // TODO: LOG THIS
            responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_SPACE_CREATE_UNKNOWN_EXCEPTION, ex));
        }
    }

    @Override
    public void handle(SpaceGetRequest request, PlanResponder responder) {
        System.err.println("get space space:" + request.space);
        responder.error(new ErrorCodeException(134));
    }

    @Override
    public void handle(SpaceUpdateRequest request, SimpleResponder responder) {
        try {
            if (request.policy.canUserSetPlan(request.who)) {
                Spaces.setPlan(nexus.base, request.policy.id, request.plan.toString());
                responder.complete();
            } else {
                throw new ErrorCodeException(ErrorCodes.API_SPACE_UPDATE_PERMISSION_FAILURE);
            }
        } catch (Exception ex) {
            // TODO: LOG IF not ErrorCode... need to pass a Logger into the DetectOrWrap.. BOOM
            responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_SPACE_UPDATE_UNKNOWN_EXCEPTION, ex));
        }
    }

    @Override
    public void handle(SpaceDeleteRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(SpaceSetRoleRequest request, SimpleResponder responder) {
        try {
            Role role = Role.from(request.role);
            if (request.policy.canUserSetRole(request.who)) {
                Spaces.setRole(nexus.base, request.policy.id, request.userId, role);
                responder.complete();
            } else {
                throw new ErrorCodeException(ErrorCodes.API_SPACE_SET_ROLE_PERMISSION_FAILURE);
            }
        } catch (Exception ex) {
            // TODO: LOG IF not ErrorCode... need to pass a Logger into the DetectOrWrap.. BOOM
            responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_SPACE_SET_ROLE_UNKNOWN_EXCEPTION, ex));
        }
    }

    @Override
    public void handle(SpaceOwnerSetRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(SpaceReflectRequest request, SimpleResponder responder) {
    }

    @Override
    public void handle(SpaceListRequest request, SpaceListingResponder responder) {
        try {
            for (Spaces.Item item : Spaces.list(nexus.base, request.who.id, request.marker, request.limit == null ? 100 : request.limit)) {
                responder.next(item.name, item.callerRole, item.billing, item.created);
            }
            responder.finish();
        } catch (Exception ex) {
            ex.printStackTrace(); // TODO: LOG THIS
            responder.error(ErrorCodeException.detectOrWrap(ErrorCodes.API_SPACE_LIST_UNKNOWN_EXCEPTION, ex));
        }
    }

    @Override
    public void handle(DocumentCreateRequest request, SimpleResponder responder) {

    }

    @Override
    public void handle(DocumentListRequest request, SimpleResponder responder) {

    }

    @Override
    public DocumentStreamHandler handle(ConnectionCreateRequest request, DataResponder responder) {
        return null;
    }

    @Override
    public AttachmentUploadHandler handle(AttachmentStartRequest request, SimpleResponder responder) {
        return null;
    }

    @Override
    public void disconnect() {

    }
}