package pl.apurtak.streams;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class HashingServiceImpl implements HashingService {
  @Override
  public String hash(String value) {
    return Hashing.sha256().hashString(value, Charsets.UTF_8).toString();
  }
}
