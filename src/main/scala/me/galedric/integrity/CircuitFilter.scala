package me.galedric.integrity

import javax.swing.filechooser.FileFilter
import java.io.File

class CircuitFilter extends FileFilter {
	def accept(f: File): Boolean = f.isDirectory() || f.getName().endsWith(".circ")
	def getDescription(): String = "Logisim circuit (.circ)"
}