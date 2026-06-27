package org.goldenport.launcher

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
final case class LauncherCoreException(message: String, code: Int = 1)
    extends RuntimeException(message)
