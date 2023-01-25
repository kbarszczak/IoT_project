package com.agh.iot.mobile.encoding;

import androidx.annotation.Nullable;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SymmetricKey {
    private static final String ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private IvParameterSpec ivParameterSpec;

    public SymmetricKey() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256, secureRandom);
            cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            ivParameterSpec = new IvParameterSpec(createInitializationVector());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SecretKey generateNewKey() {
        return keyGenerator.generateKey();
    }

    public byte[] createInitializationVector() {
        // Used with encryption
        byte[] initializationVector = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return initializationVector;
    }

    @Nullable
    public byte[] encrypt(SecretKey key, String message) {
        byte[] encryptedMessage = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    @Nullable
    public String decrypt(SecretKey key, byte[] message) {
        String decryptedMessage = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            decryptedMessage = new String(cipher.doFinal(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }
}

