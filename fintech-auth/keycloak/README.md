# keycloak-sms-mfa-authenticator
Keycloak MFA(SMS) Authenticator Plugin

## Copy to 
```
cp target\dasniko-keycloak-sms-mfa-authenticator-1.0-SNAPSHOT.jar C:\Programs\keycloak-15.0.2\standalone\deployments
```

{{keycloakUrl}}/auth/realms/{{realmName}}/example/companies

@GET
@Path("your-end-point-to-fetch-the-qr")
@Produces({MediaType.APPLICATION_JSON})
public YourDtoWithSecretAndQr get2FASetup(@PathParam("username") final String username) {
final RealmModel realm = this.session.getContext().getRealm();
final UserModel user = this.session.users().getUserByUsername(username, realm);
final String totpSecret = HmacOTP.generateSecret(20);
final String to*t*pSecretQrCode = TotpUtils.qrCode(totpSecret, realm, user);
return new YourDtoWithSecretAndQr(totpSecret, totpSecretQrCode);
}

@POST
@Path("your-end-point-to-setup-2fa")
@Consumes("application/json")
public void setup2FA(@PathParam("username") final String username, final YourDtoWithData dto) {
final RealmModel realm = this.session.getContext().getRealm();
final UserModel user = this.session.users().getUserByUsername(username, realm);
final OTPCredentialModel otpCredentialModel = OTPCredentialModel.createFromPolicy(realm, dto.getSecret(), dto.getDeviceName());
CredentialHelper.createOTPCredential(this.session, realm, user, dto.getInitialCode(), otpCredentialModel);
}