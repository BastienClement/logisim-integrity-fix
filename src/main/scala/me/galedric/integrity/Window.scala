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