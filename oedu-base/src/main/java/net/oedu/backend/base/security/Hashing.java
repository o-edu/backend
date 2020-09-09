package net.oedu.backend.base.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Hashing {
    private static final BCrypt.Hasher HASHER = BCrypt.withDefaults();
    private static final BCrypt.Verifyer VERIFYER = BCrypt.verifyer();

    public static String hash(final String content) {
        return HASHER.hashToString(12, content.toCharArray());
    }

    public static boolean verify(final String content, final String hash) {
        return VERIFYER.verify(content.toCharArray(), hash.toCharArray()).verified;
    }
}
