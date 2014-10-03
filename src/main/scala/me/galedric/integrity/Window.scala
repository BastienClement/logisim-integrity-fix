/*
	Copyright (c) 2014 Bastien Clément <g@ledric.me>

	Permission is hereby granted, free of charge, to any person obtaining a
	copy of this software and associated documentation files (the
	"Software"), to deal in the Software without restriction, including
	without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to
	permit persons to whom the Software is furnished to do so, subject to
	the following conditions:

	The above copyright notice and this permission notice shall be included
	in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
	CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
	TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
	SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package me.galedric.integrity

import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ JButton, JFileChooser, JFrame, JLabel, JTextField, SpringLayout }
import javax.swing.JProgressBar
import java.io.File
import javax.swing.JOptionPane
import javax.swing.JCheckBox

object Window {
	def bind(btn: JButton)(action: => Unit) = {
		btn.addActionListener(new ActionListener {
			def actionPerformed(e: ActionEvent) = {
				action
			}
		})
	}

	def open() = {
		val win = new JFrame("Logisim Integrity Fix")
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		val p = win.getContentPane()

		// Input file
		val file_label = new JLabel("Input file: ")
		val file_text = new JTextField(20)

		file_label.setLabelFor(file_text)
		file_text.setEditable(false)

		p.add(file_label)
		p.add(file_text)

		// Options
		def create_option(labl: String, default: Boolean) = {
			val label = new JLabel(labl)
			val checkbox = new JCheckBox()
			checkbox.setSelected(default)
			p.add(label)
			p.add(checkbox)
			checkbox
		}

		val randomize_uuid = create_option("Randomize UUIDs: ", true)
		val nudge_dates = create_option("Nudge dates: ", false)

		// Buttons
		val browse_btn = new JButton("Select input file")
		val run_btn = new JButton("Fix integrity")

		val fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
		fc.setFileFilter(new CircuitFilter)

		bind(browse_btn) {
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				file_text.setText(fc.getSelectedFile().getPath())
				run_btn.setEnabled(true)
			}
		}

		bind(run_btn) {
			run_btn.setEnabled(false)
			browse_btn.setEnabled(false)
			val file = new File(file_text.getText())
			Main.process(file, randomize_uuid.isSelected(), nudge_dates.isSelected()) {
				JOptionPane.showMessageDialog(win, "Done")
				file_text.setText("")
				browse_btn.setEnabled(true)
			}
		}

		run_btn.setEnabled(false)

		p.add(browse_btn)
		p.add(run_btn)

		p.setLayout(new SpringLayout())
		SpringUtilities.makeCompactGrid(p, 4, 2, 6, 6, 6, 6)

		win.pack()
		win.setResizable(false)
		win.setLocationRelativeTo(null)
		win.setVisible(true)
	}
}