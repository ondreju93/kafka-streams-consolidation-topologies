package pl.apurtak.streams;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashingServiceImpl implements HashingService {
  private static final Logger log = LoggerFactory.getLogger(HashingServiceImpl.class);
  private final MessageDigest digest;

  public HashingServiceImpl() {
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      log.error("Exception occured during MessageDigest instance creation: ", e);
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String hash(String value) {
    try {
      byte[] input = value.getBytes("UTF-8");
      byte[] output = this.digest.digest(input);
      return new String(output, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("Exception occured during hash calculation: [argument={}, exception={}]", value, e);
      throw new IllegalArgumentException(e);
    }
  }
}
