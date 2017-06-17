package com.zzzmode.adblib;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by zl on 2017/2/16.
 */

public final class AdbConnector {

  private static final String TAG = "AdbConnector";

  private static AdbBase64 getBase64Impl() {
    return new AdbBase64() {
      @Override
      public String encodeToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
      }
    };
  }

  // This function loads a keypair from the specified files if one exists, and if not,
  // it creates a new keypair and saves it in the specified files
  private static AdbCrypto setupCrypto(String pubKeyFile, String privKeyFile)
      throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
    File pub = new File(pubKeyFile);
    File priv = new File(privKeyFile);
    AdbCrypto c = null;

    // Try to load a key pair from the files
    if (pub.exists() && priv.exists()) {
      try {
        c = AdbCrypto.loadAdbKeyPair(getBase64Impl(), priv, pub);
      } catch (Exception e) {
        // Failed to read from file
        c = null;
      }
    }

    if (c == null) {
      // We couldn't load a key, so let's generate a new one
      c = AdbCrypto.generateAdbKeyPair(getBase64Impl());

      // Save it
      c.saveAdbKeyPair(priv, pub);
      Log.d(TAG, "Generated new keypair");
    } else {
      Log.d(TAG, "Loaded existing keypair");
    }

    return c;
  }


  public static AdbConnection connection(Context context, String host, int port) throws Exception {

    // Setup the crypto object required for the AdbConnection
    String path = context.getCacheDir().getAbsolutePath();
    Log.e(TAG, "connection path " + path);

    AdbCrypto crypto = setupCrypto(path + File.separatorChar + "pub.key",
        path + File.separatorChar + "priv.key");

    Log.e(TAG, "Socket connecting...");
    Socket sock = new Socket(host, port);
    // Connect the socket to the remote host

    Log.e(TAG, "Socket connected");

    // Construct the AdbConnection object

    AdbConnection adb = AdbConnection.create(sock, crypto);

    // Start the application layer connection process
    Log.e(TAG, "ADB connecting...");

    adb.connect();

    Log.e(TAG, "ADB connected");

    return adb;
  }


  public static AdbConnection buildConnect(Context context, String host, int port)
      throws Exception {

    // Setup the crypto object required for the AdbConnection
    String path = context.getCacheDir().getAbsolutePath();
    Log.e(TAG, "connection path " + path);

    AdbCrypto crypto = setupCrypto(path + File.separatorChar + "pub.key",
        path + File.separatorChar + "priv.key");

    Log.e(TAG, "Socket connecting...");
    Socket sock = new Socket(host, port);
    // Connect the socket to the remote host

    Log.e(TAG, "Socket connected");

    // Construct the AdbConnection object

    return AdbConnection.create(sock, crypto);
  }


  public static AdbStream openShell(Context context, String host, int port) throws Exception {
    AdbConnection connection = connection(context, host, port);
    return connection.open("shell:");
  }


}
