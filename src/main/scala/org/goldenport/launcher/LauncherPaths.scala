package org.goldenport.launcher

import java.nio.file.{Path, Paths}

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final case class LauncherPaths(
  home: Path = Paths.get(sys.props.getOrElse("user.home", ".")).toAbsolutePath.normalize,
  cwd: Path = Paths.get("").toAbsolutePath.normalize
) {
  def withCwd(path: Path): LauncherPaths =
    copy(cwd = path.toAbsolutePath.normalize)
}
