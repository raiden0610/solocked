package com.ninja.ninjaccount.security.srp;

import com.ninja.ninjaccount.service.exceptions.SRP6Exception;
import com.ninja.ninjaccount.service.util.SrpCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class SRP6ServerWorkflow {

    private final Logger log = LoggerFactory.getLogger(SRP6ServerWorkflow.class);
    private final SecureRandom random;
    @Value("${application.srp.N}")
    private BigInteger N;
    @Value("${application.srp.g}")
    private BigInteger g;
    @Value("${application.srp.k}")
    private String k;
    private CacheManager cacheManager;

    private SrpCacheService srpCacheService;

    public SRP6ServerWorkflow(CacheManager cacheManager, SrpCacheService srpCacheService) {
        this.cacheManager = cacheManager;
        this.srpCacheService = srpCacheService;
        random = new SecureRandom();
    }

    public String step1(String login, BigInteger v) {

        //Generate private value
        BigInteger b = generatePrivateValue(N, random);
        //Generate public value
        BigInteger B = computePublicServerValue(N, g, new BigInteger(k, 16), v, b);

        srpCacheService.putbInCache(b.toString(16), login);
        srpCacheService.putBInCache(B.toString(16), login);

        return B.toString(16);
    }

    public BigInteger step2(BigInteger A, BigInteger M1, BigInteger verifier, String login) throws SRP6Exception {
        // Check arguments
        if (A == null)
            throw new IllegalArgumentException("The client public value 'A' must not be null");

        if (M1 == null)
            throw new IllegalArgumentException("The client evidence message 'M1' must not be null");

        String AHex = A.toString(16);
        String BHex = cacheManager.getCache("B").get(login, String.class);
        String bHex = cacheManager.getCache("b").get(login, String.class);

        BigInteger B = new BigInteger(BHex, 16);
        BigInteger b = new BigInteger(bHex, 16);

        // Check A validity
        if (!isValidPublicValue(N, A))
            throw new SRP6Exception("Bad client public value 'A'", SRP6Exception.CauseType.BAD_PUBLIC_VALUE);

        String uHex = Sha512DigestUtils.shaHex(AHex + BHex);
        BigInteger u = new BigInteger(uHex, 16);

        BigInteger S = computeSessionKey(N, verifier, u, A, b);
        String SHex = S.toString(16);

        // Compute the own client evidence message 'M1'
        String m1SHex = Sha512DigestUtils.shaHex((AHex + BHex + SHex));

        BigInteger M1S = new BigInteger(m1SHex, 16);

        log.info("A (HEX): " + AHex);
        log.info("A: " + A);
        log.info("Client M1: " + M1);
        log.info("Client M1 (HEX): " + M1.toString(16));
        log.info("Server M1: " + M1S);
        log.info("Server M1 (HEX): " + m1SHex);
        log.info("V: " + verifier);

        log.info("B (HEX): " + BHex);
        log.info("b (HEX): " + bHex);
        log.info("B: " + B);
        log.info("b: " + b);
        log.info("uhash: " + uHex);
        log.info("u: " + u);
        log.info("S: " + SHex);

        cacheManager.getCache("B").evict(login);
        cacheManager.getCache("b").evict(login);

        if (!M1S.equals(M1)) {
            throw new SRP6Exception("Bad client credentials", SRP6Exception.CauseType.BAD_CREDENTIALS);
        }

        String M2Hex = Sha512DigestUtils.shaHex((A.toString(16) + M1.toString(16) + SHex));

        return new BigInteger(M2Hex, 16);
    }

    /**
     * Generates a random SRP-6a client or server private value ('a' or
     * 'b') which is in the range [1,N-1] generated by a random number of
     * at least 256 bits.
     *
     * <p>Specification: RFC 5054.
     *
     * @param N      The prime parameter 'N'. Must not be {@code null}.
     * @param random Source of randomness. Must not be {@code null}.
     * @return The resulting client or server private value ('a' or 'b').
     */
    public BigInteger generatePrivateValue(final BigInteger N,
                                           final SecureRandom random) {

        final int minBits = Math.max(256, N.bitLength());

        BigInteger r = BigInteger.ZERO;

        while (BigInteger.ZERO.equals(r)) {
            r = (new BigInteger(minBits, random)).mod(N);
        }

        return r;
    }

    /**
     * Computes the public server value B = k * v + g^b (mod N)
     *
     * <p>Specification: RFC 5054.
     *
     * @param N The prime parameter 'N'. Must not be {@code null}.
     * @param g The generator parameter 'g'. Must not be {@code null}.
     * @param k The SRP-6a multiplier 'k'. Must not be {@code null}.
     * @param v The password verifier 'v'. Must not be {@code null}.
     * @param b The private server value 'b'. Must not be {@code null}.
     * @return The public server value 'B'.
     */
    public BigInteger computePublicServerValue(final BigInteger N,
                                               final BigInteger g,
                                               final BigInteger k,
                                               final BigInteger v,
                                               final BigInteger b) {

        // Original from Bouncy Castle, modified:
        // return k.multiply(v).add(g.modPow(b, N));

        // Below from http://srp.stanford.edu/demo/demo.html
        return g.modPow(b, N).add(v.multiply(k)).mod(N);
    }

    /**
     * Computes the session key S = (A * v^u) ^ b (mod N) from server-side
     * parameters.
     *
     * <p>Specification: RFC 5054
     *
     * @param N The prime parameter 'N'. Must not be {@code null}.
     * @param v The password verifier 'v'. Must not be {@code null}.
     * @param u The random scrambling parameter 'u'. Must not be
     *          {@code null}.
     * @param A The public client value 'A'. Must not be {@code null}.
     * @param b The private server value 'b'. Must not be {@code null}.
     * @return The resulting session key 'S'.
     */
    public BigInteger computeSessionKey(final BigInteger N,
                                        final BigInteger v,
                                        final BigInteger u,
                                        final BigInteger A,
                                        final BigInteger b) {

        return v.modPow(u, N).multiply(A).modPow(b, N);
    }

    /**
     * Validates an SRP6 client or server public value ('A' or 'B').
     *
     * <p>Specification: RFC 5054.
     *
     * @param N     The prime parameter 'N'. Must not be {@code null}.
     * @param value The public value ('A' or 'B') to validate.
     * @return {@code true} on successful validation, else {@code false}.
     */
    public boolean isValidPublicValue(final BigInteger N,
                                      final BigInteger value) {

        // check that value % N != 0
        return !value.mod(N).equals(BigInteger.ZERO);
    }

    public BigInteger computeX(String salt,
                               String username,
                               String password) {

        String hashTmp = Sha512DigestUtils.shaHex(username + ":" + password);
        String hash = Sha512DigestUtils.shaHex(salt + hashTmp);

        return new BigInteger(hash, 16);
    }

    /**
     * Generates a new verifier 'v' from the specified parameters.
     *
     * <p>The verifier is computed as v = g^x (mod N). If a custom
     * {@link #setXRoutine 'x' computation routine} is set it will be used
     * instead of the {@link SRP6Routines#computeX default one}.
     *
     * <p>Tip: To convert a string to a byte array you can use
     * {@code String.getBytes()} or
     * {@code String.getBytes(java.nio.charset.Charset)}. To convert a big
     * integer to a byte array you can use {@code BigInteger.toByteArray()}.
     *
     * @param salt     The salt 's'. Must not be {@code null}.
     * @param userID   The user identity 'I'. May be {@code null} if the
     *                 default 'x' routine is used or the custom one
     *                 ignores it.
     * @param password The user password 'P'. Must not be {@code null}.
     * @return The resulting verifier 'v'.
     */
    public BigInteger generateVerifier(String salt, String username, String password) {

        BigInteger x = computeX(salt, username, password);

        return g.modPow(x, N);
    }
}
