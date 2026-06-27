package org.goldenport.launcher

import java.nio.file.{Files, Path}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final class LauncherCoreSpec extends AnyWordSpec with Matchers {
  "LauncherConfigParser" should {
    "parse light yaml and properties" in {
      val yaml = LauncherConfigParser.parse(Path.of("launcher.yaml"), "runtime:\n  version: 1.0\nrepositories:\n  maven:\n    - central\n")
      yaml("runtime.version") shouldBe Vector("1.0")
      yaml("repositories.maven") shouldBe Vector("central")

      val props = LauncherConfigParser.parse(Path.of("launcher.properties"), "runtime.version=2.0\n")
      props("runtime.version") shouldBe Vector("2.0")
    }
  }

  "LauncherConfigLoader" should {
    "load global ancestor and cwd configs in precedence order" in {
      val root = Files.createTempDirectory("launcher-core-")
      try {
        val home = root.resolve("home")
        val workspace = root.resolve("workspace")
        val child = workspace.resolve("samples").resolve("a")
        Files.createDirectories(home.resolve(".cncf"))
        Files.createDirectories(workspace.resolve(".cncf"))
        Files.createDirectories(child.resolve(".cncf"))
        Files.writeString(home.resolve(".cncf").resolve("launcher.yaml"), "runtime:\n  version: global\n")
        Files.writeString(workspace.resolve(".cncf").resolve("launcher.yaml"), "runtime:\n  version: root\n")
        Files.writeString(child.resolve(".cncf").resolve("launcher.yaml"), "runtime:\n  version: child\n")

        val paths = LauncherPaths(home = home, cwd = child)
        val sources = LauncherConfigLoader.load(paths, LauncherProductSpec.hiddenLauncherYaml("cncf", "CNCF"))
        sources.map(_.values("runtime.version").head) shouldBe Vector("global", "root", "child")
      } finally {
        _delete_tree(root)
      }
    }

    "let explicit config files come after discovered configs" in {
      val root = Files.createTempDirectory("launcher-core-")
      try {
        val home = root.resolve("home")
        val cwd = root.resolve("project")
        Files.createDirectories(home.resolve(".cozy"))
        Files.createDirectories(cwd)
        Files.writeString(home.resolve(".cozy").resolve("launcher.yaml"), "runtime:\n  version: global\n")
        Files.writeString(cwd.resolve("override.conf"), "runtime.version = explicit\n")

        val paths = LauncherPaths(home = home, cwd = cwd)
        val sources = LauncherConfigLoader.load(paths, LauncherProductSpec.hiddenLauncherYaml("cozy", "COZY"), Vector("override.conf"))
        sources.map(_.values("runtime.version").head) shouldBe Vector("global", "explicit")
      } finally {
        _delete_tree(root)
      }
    }
  }

  private def _delete_tree(path: Path): Unit =
    if (Files.exists(path)) {
      val files = Files.walk(path)
      try {
        files.sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p))
      } finally {
        files.close()
      }
    }
}
