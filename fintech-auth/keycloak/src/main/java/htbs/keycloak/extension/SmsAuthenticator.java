package htbs.keycloak.extension;

import htbs.keycloak.extension.gateway.SmsServiceFactory;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.RandomString;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;
import org.keycloak.utils.TotpUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(SmsAuthenticator.class);
    private static final String TPL_CODE = "login-sms.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        boolean headless = Boolean.parseBoolean(config.getConfig().getOrDefault("headless", "true"));
        try {
            KeycloakSession session = context.getSession();
            RealmModel realm = context.getRealm();
            UserModel user = context.getUser();

            String mobileNumber = user.getFirstAttribute("mobile_number");
            // mobileNumber of course has to be further validated on proper format, country code, ...

            int length = Integer.parseInt(config.getConfig().get("length"));
            int ttl = Integer.parseInt(config.getConfig().get("ttl"));

            String code = RandomString.randomCode(length);
            AuthenticationSessionModel authSession = context.getAuthenticationSession();
            authSession.setAuthNote("code", code);
            authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
            authSession.setAuthNote("formless", String.valueOf(headless));

            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);
            String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
            String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

            SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);

            final String totpSecret = HmacOTP.generateSecret(length);
            final String totpSecretQrCode = TotpUtils.qrCode(totpSecret, realm, user);
            System.out.println("*** totpSecret = " + totpSecret);
            System.out.println("*** totpSecretQrCode = " + totpSecretQrCode);

//            Response response;
            if (headless) {
//                response = Response.ok().type(MediaType.APPLICATION_JSON_TYPE).build();
                context.success();
            } else {
                LoginFormsProvider formProvider = context.form().setAttribute("realm", context.getRealm());
                Response response = formProvider.createForm(TPL_CODE);
                context.challenge(response);
            }
        } catch (Exception e) {
            logger.error("authenticate failed with " + e.getMessage(), e);
            Response response;
            if (headless) {
                response = Response.serverError().type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                response = context.form()
                        .setError("smsAuthSmsNotSent", e.getMessage())
                        .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
            }
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, response);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        try {
            String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

            AuthenticationSessionModel authSession = context.getAuthenticationSession();
            boolean headless = Boolean.parseBoolean(authSession.getAuthNote("headless"));
            String code = authSession.getAuthNote("code");
            String ttl = authSession.getAuthNote("ttl");

            if (code == null || ttl == null) {
                Response response;
                if (headless) {
                    response = Response.serverError().type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    response = context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
                }
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, response);
                return;
            }

            boolean isValid = enteredCode.equals(code);
            if (isValid) {
                if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                    Response response;
                    if (headless) {
                        response = Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
                    } else {
                        response = context.form()
                                .setError("smsAuthCodeExpired")
                                .createErrorPage(Response.Status.BAD_REQUEST);
                    }
                    // expired
                    context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, response);
                } else {
                    // valid
                    context.success();
                }
            } else {
                // invalid
                AuthenticationExecutionModel execution = context.getExecution();
                if (execution.isRequired()) {
                    Response response;
                    if (headless) {
                        response = Response.ok().type(MediaType.APPLICATION_JSON_TYPE).build();
                    } else {
                        response = context.form().setAttribute("realm", context.getRealm())
                                .setError("smsAuthCodeInvalid")
                                .createForm(TPL_CODE);
                    }
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, response);
                } else if (execution.isConditional() || execution.isAlternative()) {
                    context.attempted();
                }
            }
        } catch (Exception e) {
            logger.error("action failed with " + e.getMessage(), e);
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;//user.getFirstAttribute("mobile_number") != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

}
