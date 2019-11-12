package io.reconquest.bitbucket.labels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import com.atlassian.plugin.util.ClassLoaderUtils;
import com.google.common.base.Charsets;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseValidator {
  private static final String PUBLIC_KEY = ""
      + "MIIDRjCCAjkGByqGSM44BAEwggIsAoIBAQCXNVVR/55M+fXGU6GmpW6RmSIIxi+V\n"
      + "65651FSMztGZYUAcLKpVBopXLB+SZamNDsXbMVklog/umUa5mKRUQjZD2dXrgLrt\n"
      + "jbs9EIpWF9jvDkcjlTSgdFSsrN+w1zJt+ImG6zLVeWRF36ozlBB/9w1CCszQe7Vt\n"
      + "4x/JgUuGHu0xJGoJHNhFsj1KncG06fqz/vQml08KCQfIbBbgt7ahfmGPqL9we6Ka\n"
      + "bpe2vflH/j9UOH+6GWXBbBtLWay0JoJ58fc6+mdm0cv0EL/Vki7qjK3d7V282TqD\n"
      + "fR+PCXfIJN3EL6u/0ZJkQIkEcKw1Zq5mqlU7DZYZdtCb6TQcBJy9kct7AiEArjwA\n"
      + "vCPwp4IaI5XiOSb40v3tu+lyMLvHzQZYyub0sbUCggEAIqbSiYZP1sIazTbl5AYb\n"
      + "znseWYp1zCc9rJRVhbT8Kj8ap6yXlANRPHAKcIrl+NzkyIit0P9+f+MoKeFPV7us\n"
      + "LyN3tgHOudOG1Ha3KBrYP9rL0EoSL8lpl4W03f1csKXYa2t9W1UwVZyuYZA/x7vU\n"
      + "dZC2CrWehLfAFZXJjRSrF6H5vGQ8IxcRBjVWloUs2w/0SB4Zd+/EwC7BRwQACfyY\n"
      + "LEDEDJkE6d5DA2ww6htrccXcUOGCCUwl0DEc1Xn8ek9qLVwWGnY7D1teCqdtn5Ao\n"
      + "rrBdpptxf2D9fD3j8nc3/XKxDd9OdO+XQqm5RZPXnL7KRu5xmyzixXUdL2Om/5ey\n"
      + "owOCAQUAAoIBAECfmKkgneq7Lm4o/YkTAHBtx8DDcDHjnJNMwnsahVg2+b2r2CCF\n"
      + "DM6r8L6+xXkXhDCpA4Y+V8my0/G7nxthBU8nJd9Z2cdq8qbITnXYnaSGx6OSk5T0\n"
      + "V6eP8ck2CtePSoADhenvIoeFgC+4biFQsCLF/NWckudPr5/Nx4773c3b7oe2mC3A\n"
      + "QE35aoGYg6d4kIOtIvlxopIVyXPhEqGcvoP5RNWsn/PwGaq8rgiFa92RjX4Xd9Xc\n"
      + "ZJXFZ3lnW1fqyDe7KIaTZw3sGrBZ/4IhpvnvGHVxBYz1rBahL3KpYt1b6E6N65t4\n"
      + "2nOMzOgp6Glhr++St20VeNwfwV2PTN5Je80=";
  private static Logger log = LoggerFactory.getLogger(LicenseValidator.class.getSimpleName());
  private Signature dsa;

  public LicenseValidator() {
    try {
      dsa = Signature.getInstance("DSA");
    } catch (NoSuchAlgorithmException e) {
      log.error("Unable to init Signature verifier", e);
    }

    String license = getResource("license");

    try {
      PublicKey key = getPublicKey();

      System.err.printf("XXXXXXX LicenseValidator.java:56 key %s \n", key);
    } catch (IOException | GeneralSecurityException e) {
      log.error("Unable to decode public dsa key", e);
    }
  }

  private DSAPublicKey getPublicKey() throws IOException, GeneralSecurityException {
    byte[] encoded = Base64.decodeBase64(PUBLIC_KEY);
    KeyFactory kf = KeyFactory.getInstance("DSA");
    DSAPublicKey pubKey = (DSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
    return pubKey;
  }

  private String getResource(String name) {
    InputStream resource = ClassLoaderUtils.getResourceAsStream(name, this.getClass());
    if (resource == null) {
      return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    String line = null;

    try {
      BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(resource, Charsets.UTF_8));
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }

      return stringBuilder.toString();
    } catch (IOException e) {
      return null;
    }
  }

  public boolean isDefined() {
    return true;
    // Option<PluginLicense> licenseOption = pluginLicenseManager.getLicense();
    // return licenseOption.isDefined();
  }

  public boolean isValid() {
    return true;
    // Option<PluginLicense> licenseOption = pluginLicenseManager.getLicense();
    // if (!licenseOption.isDefined()) {
    //  return false;
    // }

    // PluginLicense pluginLicense = licenseOption.get();
    // return pluginLicense.isValid();
  }
}
