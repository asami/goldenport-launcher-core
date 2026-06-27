package org.goldenport.launcher

import java.nio.file.Path

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final case class LauncherProductSpec(
  productName: String,
  envPrefix: String,
  globalConfigCandidates: LauncherPaths => Vector[Path],
  workspaceConfigCandidates: Path => Vector[Path]
)

object LauncherProductSpec {
  def hiddenLauncherYaml(
    productname: String,
    envprefix: String
  ): LauncherProductSpec =
    LauncherProductSpec(
      productName = productname,
      envPrefix = envprefix,
      globalConfigCandidates = paths => Vector(paths.home.resolve(s".$productname").resolve("launcher.yaml")),
      workspaceConfigCandidates = dir => Vector(
        dir.resolve("conf").resolve(productname).resolve("launcher.yaml"),
        dir.resolve(s".$productname").resolve("launcher.yaml")
      )
    )

  def textusConfig: LauncherProductSpec =
    LauncherProductSpec(
      productName = "textus",
      envPrefix = "TEXTUS",
      globalConfigCandidates = paths => Vector(paths.home.resolve(".textus").resolve("config.yaml")),
      workspaceConfigCandidates = dir => Vector(dir.resolve(".textus").resolve("config.yaml"))
    )
}
