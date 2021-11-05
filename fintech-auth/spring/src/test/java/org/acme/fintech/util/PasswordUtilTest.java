package org.acme.fintech.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordUtilTest {

    @Test
    public void encodePassword() {
        String rawPassword = RandomStringUtils.randomAscii(20);
        System.out.println("password raw: " + rawPassword);

        // First attempt
        byte[] rawSaltOrig = PasswordUtil.randomSalt();
        System.out.println("salt original bytes: " + Arrays.toString(rawSaltOrig));

        String encSaltOrig = PasswordUtil.encodeBase64(rawSaltOrig);
        System.out.println("salt original string: " + encSaltOrig);

        byte[] rawSaltRest = PasswordUtil.decodeBase64(encSaltOrig);
        System.out.println("salt restored bytes: " + Arrays.toString(rawSaltRest));

        String encSaltRest = PasswordUtil.encodeBase64(rawSaltRest);
        System.out.println("salt restored string: " + encSaltRest);

        String encodePassword = PasswordUtil.encodePassword(rawPassword, rawSaltOrig);
        System.out.println("password encoded: " + encodePassword);

        String restoredPassword = PasswordUtil.encodePassword(rawPassword, rawSaltRest);
        System.out.println("password restored: " + restoredPassword);

        assertArrayEquals(rawSaltOrig, rawSaltRest);
        assertEquals(encodePassword, restoredPassword);
    }
}