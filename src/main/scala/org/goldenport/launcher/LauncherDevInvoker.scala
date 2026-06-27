package org.goldenport.launcher

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
object LauncherDevInvoker {
  def invokeJavaMain(
    productname: String,
    devdir: Path,
    classpath: String,
    mainclass: String,
    args: Vector[String],
    cwd: Path,
    environment: Map[String, String],
    argsFileEnvironmentKey: String
  ): Int = {
    val argsfile = writeArgsFile(productname, args)
    try {
      val builder = new java.lang.ProcessBuilder("java", "-cp", classpath, mainclass)
      builder.directory(cwd.toAbsolutePath.normalize.toFile)
      builder.inheritIO()
      environment.foreach { case (key, value) => builder.environment().put(key, value) }
      builder.environment().put(argsFileEnvironmentKey, argsfile.toString)
      builder.start().waitFor()
    } finally {
      Files.deleteIfExists(argsfile)
    }
  }

  def writeArgsFile(
    productname: String,
    args: Vector[String]
  ): Path = {
    val argsfile = Files.createTempFile(s"$productname-launcher-args-", ".bin")
    val payload =
      if (args.isEmpty)
        ""
      else
        args.mkString("", "\u0000", "\u0000")
    Files.write(argsfile, payload.getBytes(StandardCharsets.UTF_8))
    argsfile
  }
}
