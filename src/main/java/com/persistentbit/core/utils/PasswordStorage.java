package com.persistentbit.core.utils;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.LogEntryPrinter;
import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.result.Result;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Utility class to convert and verify passwords with a salted hash.<br>
 */
public final class PasswordStorage {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    // These constants may be changed without breaking existing hashes.
    private static final int SALT_BYTE_SIZE = 24;
    private static final int HASH_BYTE_SIZE = 18;
    private static final int PBKDF2_ITERATIONS = 64000;
    // These constants define the encoding and may not be changed.
    private static final int HASH_SECTIONS = 5;
    private static final int HASH_ALGORITHM_INDEX = 0;
    private static final int ITERATION_INDEX = 1;
    private static final int HASH_SIZE_INDEX = 2;
    private static final int SALT_INDEX = 3;
    private static final int PBKDF2_INDEX = 4;

    /**
     * Create a Secure Random String.
     *
     * @param length The length in bytes of the random code. (Not the length of the string)
     * @return The base64 string of the generated code.
     */
    public static String createRandomCode(int length) {
        if(length<0){
            length = 0;
        }
        SecureRandom random = new SecureRandom();
        byte[] result = new byte[length];
        random.nextBytes(result);
        return toBase64(result);
    }

    private static String toBase64(byte[] array) {
        return DatatypeConverter.printBase64Binary(array);
    }



    public static Result<String> createHash(String password){
        return Log.function("<password>").code(l -> {
            if(password == null){
                return Result.failure("password is null");
            }
            return createHash(password.toCharArray());
        });
    }

    public static Result<Nothing> verifyPassword(String password, String correctHash){
        return Log.function("<password>",correctHash).code(l -> {
            if(password == null){
                return Result.failure("password is null");
            }
            if(correctHash == null){
                return Result.failure("correct hash is null");
            }
            return verifyPassword(password.toCharArray(), correctHash);
        });

    }

    public static Result<String> createHash(char[] password) {
        return Log.function("<password>").code(l-> {
            if(password == null){
                return Result.failure("password is null");
            }
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTE_SIZE];
            random.nextBytes(salt);

            // Hash the password
            byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
            int hashSize = hash.length;

            // format: algorithm:iterations:hashSize:salt:hash
            return Result.success("sha256:" +
                                      PBKDF2_ITERATIONS +
                                      ":" + hashSize +
                                      ":" +
                                      toBase64(salt) +
                                      ":" +
                                      toBase64(hash));
        });

    }

    public static Result<Nothing> verifyPassword(char[] password, String correctHash) {
        return Log.function("<password>",correctHash).code(l-> {
            if (password == null) {
                return Result.failure("password is null");
            }
            if (correctHash == null) {
                return Result.failure("correctHash is null");
            }
            // Decode the hash into its parameters
            String[] params = correctHash.split(":");
            if (params.length != HASH_SECTIONS) {
                return Result.failure(new InvalidHashException("Fields are missing from the password hash."));
            }


            if (!params[HASH_ALGORITHM_INDEX].equals("sha256")) {
                return Result.failure(new CannotPerformOperationException("Unsupported hash type."));
            }
            return NumberUtils.parsInt(params[ITERATION_INDEX])
                .mapError(ex -> new InvalidHashException("Could not parse the iteration count as an integer.",ex))
                .flatMap(iterations -> {
                    if(iterations < 1){
                        return Result.failure(new InvalidHashException("Invalid number of iterations. Must be >= 1."));
                    }
                    return fromBase64(params[SALT_INDEX]).flatMap(salt ->
                          fromBase64(params[PBKDF2_INDEX]).flatMap(hash ->
                               NumberUtils.parsInt(params[HASH_SIZE_INDEX])
                                   .verify(storedHashSize -> storedHashSize == hash.length,(v)->new InvalidHashException("Hash length doesn't match stored hash length."))
                                   .flatMap(storedHashSize ->
                                            {
                                                // Compute the hash of the provided password, using the same salt,
                                                // iteration count, and hash length
                                                byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
                                                // Compare the hashes in constant time. The password is correct if
                                                // both hashes match.
                                                if(slowEquals(hash,testHash)){
                                                    return Result.success(Nothing.inst);
                                                }
                                                return Result.failure("HashVerification failed");
                                            })
                          )
                    );
                });

        });

    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws CannotPerformOperationException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new CannotPerformOperationException(
                    "Hash algorithm not supported.",
                    ex
            );
        } catch (InvalidKeySpecException ex) {
            throw new CannotPerformOperationException(
                    "Invalid key spec.",
                    ex
            );
        }
    }

    private static Result<byte[]> fromBase64(String hex) {
        return Log.function(StringUtils.present(hex,20)).code(l-> {
            if(hex == null){
                return Result.failure("bas64 string is null");
            }
            return Result.success(DatatypeConverter.parseBase64Binary(hex));
        });

    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    @SuppressWarnings("serial")
    public static class InvalidHashException extends RuntimeException {

        public InvalidHashException(String message) {
            super(message);
        }

        public InvalidHashException(String message, Throwable source) {
            super(message, source);
        }
    }

    @SuppressWarnings("serial")
    public static class CannotPerformOperationException extends RuntimeException {

        public CannotPerformOperationException(String message) {
            super(message);
        }

        public CannotPerformOperationException(String message, Throwable source) {
            super(message, source);
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String... args) {
        LogEntryPrinter lp     = LogPrinter.consoleInColor();
        String          result = createHash("PeterMuys20").orElseThrow();
        System.out.println(result);
        lp.print(createHash((String)null));
        lp.print(verifyPassword("PeterMuys20", result));
        lp.print(verifyPassword("PeterMuys21", result));
        lp.print(verifyPassword("PeterMuys20", null));
        lp.print(verifyPassword((String)null , result));
        lp.print(verifyPassword("PeterMuys20" , "sha256:1:12:???:base64string"));
    }
}