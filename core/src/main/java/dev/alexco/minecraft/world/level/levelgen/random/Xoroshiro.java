package dev.alexco.minecraft.world.level.levelgen.random;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Xoroshiro {
    private long s0, s1;

    private static final long STAFFORD_1 = -4658895280553007687L;
    private static final long STAFFORD_2 = -7723592293110705685L;

    /** Creates a new PRNG from a single 64-bit seed */
    public Xoroshiro(long seed) {
        long[] seeds = upgradeSeedTo128bit(seed);
        this.s0 = seeds[0];
        this.s1 = seeds[1];
    }

    /** Creates a PRNG from two 64-bit seeds */
    public Xoroshiro(long seedLo, long seedHi) {
        this.s0 = seedLo;
        this.s1 = seedHi;
    }

    /** Rotates left */
    private static long rotl(long x, int k) {
        return (x << k) | (x >>> (64 - k));
    }

    /** Mixes a 64-bit seed into pseudo-random bits using Stafford13 */
    private static long mixStafford13(long value) {
        value = (value ^ (value >>> 30)) * STAFFORD_1;
        value = (value ^ (value >>> 27)) * STAFFORD_2;
        return value ^ (value >>> 31);
    }

    /** Converts a single 64-bit seed into two 64-bit seeds for the 128-bit state */
    private static long[] upgradeSeedTo128bit(long seed) {
        long seedLo = mixStafford13(seed);
        long seedHi = mixStafford13(seedLo + 0x9E3779B97F4A7C15L); // golden ratio constant
        return new long[]{seedLo, seedHi};
    }

    /** Returns the next 64-bit value */
    public long nextLong() {
        long s0 = this.s0;
        long s1 = this.s1;
        long result = s0 + s1;

        s1 ^= s0;
        this.s0 = rotl(s0, 55) ^ s1 ^ (s1 << 14);
        this.s1 = rotl(s1, 36);

        return result;
    }

    /** Returns a 32-bit int in [0, max) using rejection sampling */
    public int nextInt(int max) {
        if (max <= 0) throw new IllegalArgumentException("max must be positive");

        long threshold = (0x100000000L % max);
        long r;
        do {
            r = nextLong() >>> 32; // upper 32 bits
        } while (r < threshold);
        return (int) (r % max);
    }

    /** Returns a float in [0,1) */
    public float nextFloat() {
        return (nextLong() >>> 40) * (1.0f / (1L << 24));
    }

    /** Returns a double in [0,1) */
    public double nextDouble() {
        return (nextLong() >>> 11) * (1.0 / (1L << 53));
    }

    /** Returns the next n bits */
    private long nextBits(int bits) {
        return nextLong() >>> (64 - bits);
    }

    /** Advances the generator state 'count' times */
    public void consume(int count) {
        for (int i = 0; i < count; i++) nextLong();
    }

    /** Creates a new independent PRNG using the next two outputs as seeds */
    public Xoroshiro fork() {
        return new Xoroshiro(nextLong(), nextLong());
    }
    public Xoroshiro forkPositional() {
        return fork();
    }

    /** Creates a PRNG deterministically from a string */
    public Xoroshiro fromHashOf(String name) {
        byte[] hash = md5(name);
        long lo = longFromBytes(hash, 0);
        long hi = longFromBytes(hash, 8);
        return new Xoroshiro(lo ^ s0, hi ^ s1);
    }

    private static byte[] md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    private static long longFromBytes(byte[] bytes, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, 8);
        return buffer.getLong();
    }

    /** Returns a string describing the current state */
    public String parityConfigString() {
        return "s0: " + s0 + ", s1: " + s1;
    }
}
