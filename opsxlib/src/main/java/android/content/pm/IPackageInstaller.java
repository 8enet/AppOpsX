package android.content.pm;

/**
 * Created by zl on 2017/7/31.
 */

public interface IPackageInstaller {
  PackageInstaller.SessionInfo getSessionInfo(int sessionId);
  void registerCallback(IPackageInstallerCallback callback, int userId);
  void unregisterCallback(IPackageInstallerCallback callback);
}
