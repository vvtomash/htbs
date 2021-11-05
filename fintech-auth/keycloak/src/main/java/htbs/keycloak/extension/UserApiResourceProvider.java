package htbs.keycloak.extension;

import htbs.keycloak.extension.model.SignUpCompleteRequest;
import htbs.keycloak.extension.model.SignUpInitialRequest;
import htbs.keycloak.extension.model.SignUpResponse;
import org.jboss.logging.Logger;
import org.keycloak.common.util.RandomString;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.Pbkdf2Sha256PasswordHashProviderFactory;
import org.keycloak.models.*;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.CredentialHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

public class UserApiResourceProvider implements RealmResourceProvider {
    private static final Logger logger = Logger.getLogger(UserApiResourceProvider.class);
    private final KeycloakSession session;

    public UserApiResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    @POST
    @Path("signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signup_initiate(SignUpInitialRequest request) {
        RealmModel realm = session.getContext().getRealm();
        String mobileNumber = request.getMobile();

        // TODO Check Digital User Db against mobile number

        try {
            // Basic user creation. Username is required hence using mobile number as username
            UserModel user = session.users().addUser(realm, mobileNumber);

            // Mandatory attribute for SMS Authenticator
            user.setAttribute("mobile_number", Collections.singletonList(mobileNumber));

            // Make sure user is disabled until OTP is validated
            user.setEnabled(false);

            // SHA256 Hashed password. Salt auto-generated inside
            PasswordHashProvider provider = new Pbkdf2Sha256PasswordHashProviderFactory().create(session);
            PasswordCredentialModel credential = provider.encodedCredential(request.getPassword(), -1);
            session.userCredentialManager().createCredential(realm, user, credential);

            // Generate OTP
            String code = RandomString.randomCode(6); // TODO Length to configuration
            AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
            authSession.setAuthNote("code", code);
            authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + 300_000L)); // TODO TTL to configuration

            // TODO Route to SMS Gateway
            logger.info(String.format("***** SIMULATION MODE ***** Would send SMS to %s with text: %s", mobileNumber, code));

            // 202 Accepted
            SignUpResponse response = new SignUpResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            return Response.accepted(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception ex) {
            logger.error("signup initiating failed " + ex.getMessage(), ex);

            // 202 Accepted too :-)
            return Response.accepted().type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @PATCH
    @Path("signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signup_complete(SignUpCompleteRequest request) {
        RealmModel realm = session.getContext().getRealm();
        try {
            UserModel user = session.users().getUserByUsername(realm, request.getMobile());
            if (user == null)
                throw new RuntimeException("Invalid user");

            // We expect user is disabled at this state
            if (user.isEnabled()) {
                throw new RuntimeException("User already activated");
            }

            AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
            String code = authSession.getAuthNote("code");
            String ttl = authSession.getAuthNote("ttl");
            if (code == null || ttl == null) {
                throw new RuntimeException("Code or ttl is empty");
            }
            boolean isValid = code.equalsIgnoreCase(request.getOtpCode());
            if (isValid) {
                if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                    throw new RuntimeException("Code is expired");
                }
            } else {
                throw new RuntimeException("Code is invalid");
            }

            // Otherwise, activate user
            user.setEnabled(true);

            // TODO Activate user in Digital Db

            //final OTPCredentialModel otpCredentialModel = OTPCredentialModel.createFromPolicy(realm, request.getOtpCode(), request.getDeviceName());
            //CredentialHelper.createOTPCredential(this.session, realm, user, dto.getInitialCode(), otpCredentialModel);

        } catch (Exception ex) {
            logger.error("signup completion failed " + ex.getMessage(), ex);
        }
        return Response.accepted().type(MediaType.APPLICATION_JSON_TYPE).build();
    }


}
