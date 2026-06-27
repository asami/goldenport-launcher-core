package org.goldenport.launcher

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final case class CommonLauncherConfig(
  launcherDevDir: Option[String] = None,
  runtimeVersion: Option[String] = None,
  runtimeCatalogUrl: Option[String] = None,
  runtimeDevDir: Option[String] = None,
  developmentLauncherDevDir: Option[String] = None,
  developmentRuntimeDevDir: Option[String] = None,
  repositories: Map[String, Vector[String]] = Map.empty
)
