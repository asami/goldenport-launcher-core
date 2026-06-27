package org.goldenport.launcher

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final case class LauncherConfigSource(
  path: Path,
  values: Map[String, Vector[String]]
)

object LauncherConfigLoader {
  def existingConfigFiles(
    paths: LauncherPaths,
    spec: LauncherProductSpec
  ): Vector[Path] =
    (spec.globalConfigCandidates(paths) ++ _workspace_files(paths, spec)).
      map(_.toAbsolutePath.normalize).
      filter(Files.isRegularFile(_)).
      distinct

  def load(
    paths: LauncherPaths,
    spec: LauncherProductSpec,
    explicitfiles: Vector[String] = Vector.empty
  ): Vector[LauncherConfigSource] = {
    val discovered = existingConfigFiles(paths, spec)
    val explicit = explicitfiles.map(file => paths.cwd.resolve(file).normalize.toAbsolutePath.normalize)
    val missing = explicit.filterNot(Files.isRegularFile(_))
    if (missing.nonEmpty)
      throw LauncherCoreException(s"launcher config file not found: ${missing.head}")
    (discovered ++ explicit).distinct.map(_load_file)
  }

  def existingFiles(
    paths: LauncherPaths,
    candidates: Path => Vector[Path]
  ): Vector[Path] =
    _workspace_dirs(paths).flatMap(candidates).map(_.toAbsolutePath.normalize).filter(Files.isRegularFile(_)).distinct

  private def _workspace_files(
    paths: LauncherPaths,
    spec: LauncherProductSpec
  ): Vector[Path] =
    _workspace_dirs(paths).flatMap(spec.workspaceConfigCandidates)

  private def _workspace_dirs(paths: LauncherPaths): Vector[Path] = {
    val cwd = paths.cwd.toAbsolutePath.normalize
    val home = paths.home.toAbsolutePath.normalize
    Iterator.iterate(Option(cwd))(_.flatMap(p => Option(p.getParent))).
      takeWhile(_.nonEmpty).
      flatten.
      takeWhile(p => p != home.getParent).
      toVector.
      reverse
  }

  private def _load_file(path: Path): LauncherConfigSource = {
    val text = Files.readString(path, StandardCharsets.UTF_8)
    LauncherConfigSource(path, LauncherConfigParser.parse(path, text))
  }
}
