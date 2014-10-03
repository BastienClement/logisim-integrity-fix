package me.galedric.integrity

import java.io.{ File, FileOutputStream, StringWriter }
import java.text.SimpleDateFormat
import java.util.UUID

import scala.util.Random

import org.w3c.dom.{ Document, Element, Node, NodeList }

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.{ XPathConstants, XPathFactory }

object Main extends App {
	Window.open()

	val xpath = XPathFactory.newInstance().newXPath()

	def read(f: File): Document = {
		val docbf = DocumentBuilderFactory.newInstance()
		docbf.setNamespaceAware(true)
		docbf.newDocumentBuilder().parse(f)
	}

	def serialize(e: Node) = {
		val tr = TransformerFactory.newInstance().newTransformer()
		val sw = new StringWriter

		tr.setOutputProperty("omit-xml-declaration", "yes")
		tr.transform(new DOMSource(e), new StreamResult(sw))

		sw.toString()
	}

	def randomUUID = UUID.randomUUID().toString()

	def selectAttribute(name: String)(implicit component: Element) = {
		val el = xpath.compile(s"a[@name='$name']").evaluate(component, XPathConstants.NODE).asInstanceOf[Element]
		val value = xpath.compile(s"a[@name='$name']/@val").evaluate(component)
		(el, value)
	}

	def nudgeDate(date_str: String) = {
		val format = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		val date = format.parse(date_str)
		val time = date.getTime()
		val offset = Math.round(Random.nextGaussian() * 15000)
		date.setTime(time + offset)
		format.format(date)
	}

	def fixComponents(root: Element, randomize_uuid: Boolean, nudge_dates: Boolean) = {
		val components = xpath.compile("//*[./a[@name='integrity']]").evaluate(root, XPathConstants.NODESET).asInstanceOf[NodeList]

		for (i <- 0 until components.getLength()) {
			implicit val component = components.item(i).asInstanceOf[Element]

			val (owner_el, owner) = selectAttribute("owner")
			val (date_el, date) = selectAttribute("date")
			val (version_el, version) = selectAttribute("version")
			val (uuid_el, uuid) = selectAttribute("uuid")
			val (integrity_el, integrity) = selectAttribute("integrity")

			val new_date = if (nudge_dates) nudgeDate(date) else date
			val new_uuid = if (randomize_uuid) randomUUID else uuid

			date_el.setAttribute("val", new_date)
			uuid_el.setAttribute("val", new_uuid)
			integrity_el.setAttribute("val", Hash.compute(owner + new_date + version + new_uuid))
		}
	}

	def fixTopLevel(root: Element) = {
		root.removeAttribute("integrity")
		root.setAttribute("integrity", Hash.compute(serialize(root)))
		root
	}

	def write(f: File, doc: Document) = {
		val trf = TransformerFactory.newInstance()
		try { trf.setAttribute("ident-number", 2) } catch { case e: Throwable => /* ignore */ }

		val tr = trf.newTransformer()

		tr.setOutputProperty("encoding", "UTF-8")
		tr.setOutputProperty("indent", "yes")
		try {
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
		} catch { case e: Throwable => /* ignore */ }

		val fos = new FileOutputStream(f)
		tr.transform(new DOMSource(doc), new StreamResult(fos))
	}

	def process(f: File, randomize_uuid: Boolean, nudge_dates: Boolean)(done: => Unit) = {
		val doc = read(f)
		val root = doc.getDocumentElement()

		fixComponents(root, randomize_uuid, nudge_dates)
		fixTopLevel(root)

		write(f, doc)
		done
	}
}