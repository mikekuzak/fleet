/*
 * Copyright (c) 2019 LinuxServer.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.linuxserver.fleet.auth.security;

import io.linuxserver.fleet.auth.security.util.SaltGenerator;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * <p>
 * Uses the PKCS5S2 crypto algorithm to encode and verify hashed passwords.
 * </p>
 */
public class PKCS5S2PasswordEncoder implements PasswordEncoder {

    private static final int DEFAULT_HASH_WIDTH = 512;
    private static final int DEFAULT_ITERATIONS = 150051;

    private final SaltGenerator saltGenerator = new SaltGenerator();

    private final byte[] secret;
    private final int hashWidth;
    private final int iterations;

    public PKCS5S2PasswordEncoder(String secret) {
        this(secret, DEFAULT_HASH_WIDTH, DEFAULT_ITERATIONS);
    }

    public PKCS5S2PasswordEncoder(String secret, int hashWidth, int iterations) {

        this.secret     = secret.getBytes(StandardCharsets.UTF_8);
        this.hashWidth  = hashWidth;
        this.iterations = iterations;
    }

    @Override
    public String encode(String rawPassword) {

        if (null == rawPassword) {
            throw new IllegalArgumentException("Password must not be null");
        }

        return toBase64(encode(rawPassword, saltGenerator.generateSalt()));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {

        byte[] decodedHash  = fromBase64(encodedPassword);
        byte[] saltInHash   = extractSalt(decodedHash);
        byte[] hashToVerify = encode(rawPassword, saltInHash);

        return passwordsMatch(decodedHash, hashToVerify);
    }

    /**
     * <p>
     * Compares the two byte arrays by performing a bitwise equality check against each individual
     * element of both arrays.
     * </p>
     *
     * @implNote I looked at a couple of implementations for doing this, and I preferred how Spring had implemented it.
     */
    private boolean passwordsMatch(byte[] originalPassword, byte[] providedPassword) {

        if (originalPassword.length != providedPassword.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < originalPassword.length; i++) {
            result |= originalPassword[i] ^ providedPassword[i];
        }

        return result == 0;
    }

    /**
     * <p>
     * Performs the cryptographic hash against the raw password and the randomly generated salt value. This
     * also concatenates the provided secret into the salt.
     * </p>
     */
    private byte[] encode(String rawPassword, byte[] salt) {

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(
            PBEParametersGenerator.PKCS5PasswordToBytes(rawPassword.toCharArray()),
            joinArrays(salt, secret),
            iterations
        );

        return joinArrays(salt, ((KeyParameter) generator.generateDerivedMacParameters(hashWidth)).getKey());
    }

    /**
     * <p>
     * Converts a byte array into a base-64 encoded string.
     * </p>
     */
    private String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * <p>
     * Converts a base64-encoded string into its raw byte value
     * </p>
     */
    private byte[] fromBase64(String input) {
        return Base64.getDecoder().decode(input);
    }

    /**
     * <p>
     * Obtains the specific bytes which represent the salt used in a previous hashed password. This is to
     * enable the comparision between the existing and new password.
     * </p>
     */
    private byte[] extractSalt(byte[] decodedHash) {
        return extractFromArray(decodedHash, 0, saltGenerator.getKeyLength());
    }

    /**
     * <p>
     * Combines two byte arrays together in order.
     * </p>
     */
    private byte[] joinArrays(byte[] first, byte[] second) {

        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * <p>
     * Extracts a sub-array from the provided array.
     * </p>
     */
    private byte[] extractFromArray(byte[] array, int begin, int end) {

        int length = end - begin;
        byte[] subarray = new byte[length];
        System.arraycopy(array, begin, subarray, 0, length);
        return subarray;
    }
}
