package me.galedric.integrity

import java.security.MessageDigest

object Hash {
	val salt = "6234ebf2-60c5-4f0e-b3cb-bb887cfd379b"

	def compute(str: String) = {
		val sha1 = MessageDigest.getInstance("SHA-1")
		val source = str.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\r", "") + salt
		val hash = sha1.digest(source.getBytes("UTF-8")) map { byte =>
			Integer.toString((byte & 0xFF) + 256, 16).substring(1)
		}

		hash.mkString
	}
}