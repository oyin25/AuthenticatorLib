package com.HP.authenticator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Locale;
import java.util.Arrays;

public class VerificationCodeUtil {

    // ===================== BASE32 =====================
    public static class Base32 {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        private static final int SECRET_SIZE = 10;
        private static final char[] DIGITS = ALPHABET.toCharArray();
        private static final int MASK = DIGITS.length - 1;
        private static final int SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
        private static final HashMap<Character, Integer> CHAR_MAP = new HashMap<>();

        static {
            for (int i = 0; i < DIGITS.length; i++) {
                CHAR_MAP.put(DIGITS[i], i);
            }
        }

        public static byte[] decode(String encoded) throws DecodingException {
            encoded = encoded.trim().replaceAll("-", "").replaceAll(" ", "").replaceFirst("[=]*$", "").toUpperCase(Locale.US);
            if (encoded.length() == 0) return new byte[0];

            int outLength = encoded.length() * SHIFT / 8;
            byte[] result = new byte[outLength];
            int buffer = 0, next = 0, bitsLeft = 0;

            for (char c : encoded.toCharArray()) {
                if (!CHAR_MAP.containsKey(c)) throw new DecodingException("Illegal character: " + c);
                buffer <<= SHIFT;
                buffer |= CHAR_MAP.get(c) & MASK;
                bitsLeft += SHIFT;
                if (bitsLeft >= 8) {
                    result[next++] = (byte) (buffer >> (bitsLeft - 8));
                    bitsLeft -= 8;
                }
            }
            return result;
        }

        public static String encode(byte[] data) {
            if (data.length == 0) return "";

            StringBuilder result = new StringBuilder((data.length * 8 + SHIFT - 1) / SHIFT);
            int buffer = data[0], next = 1, bitsLeft = 8;

            while (bitsLeft > 0 || next < data.length) {
                if (bitsLeft < SHIFT) {
                    if (next < data.length) {
                        buffer <<= 8;
                        buffer |= (data[next++] & 0xff);
                        bitsLeft += 8;
                    } else {
                        int pad = SHIFT - bitsLeft;
                        buffer <<= pad;
                        bitsLeft += pad;
                    }
                }
                int index = MASK & (buffer >> (bitsLeft - SHIFT));
                bitsLeft -= SHIFT;
                result.append(DIGITS[index]);
            }
            return result.toString();
        }

        public static String generateRandomSecret() {
            byte[] buffer = new byte[SECRET_SIZE];
            new SecureRandom().nextBytes(buffer);
            return encode(buffer);
        }

        public static class DecodingException extends Exception {
            public DecodingException(String msg) {
                super(msg);
            }
        }
    }

    // ===================== OTP CODE GENERATION =====================

    public static String generateCode(String secret, int digits, int timeStepSeconds, String algorithm) {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long counter = currentTimeSeconds / timeStepSeconds;

        byte[] key;
        try {
            key = Base32.decode(secret);
        } catch (Base32.DecodingException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));

            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(counter);
            byte[] hash = mac.doFinal(buffer.array());

            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateCode(String secret) {
        return generateCode(secret, 6, 30, "HmacSHA1");
    }

    // ===================== OTPAUTH URI =====================

    public static String getOtpAuthUrl(String label, String issuer, String secret) {
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + issuer;
    }
}