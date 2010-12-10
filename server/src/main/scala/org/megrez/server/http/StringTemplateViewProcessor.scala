package org.megrez.server.http

import com.sun.jersey.spi.template.ViewProcessor
import com.sun.jersey.api.view.Viewable
import java.lang.String
import javax.ws.rs.ext.Provider
import io.Source
import java.io.{OutputStreamWriter, File, OutputStream}
import org.antlr.stringtemplate.{AutoIndentWriter, StringTemplate}

@Provider
class StringTemplateViewProcessor extends ViewProcessor[String] {
  def resolve(path: String) = path

  def writeTo(template: String, view: Viewable, stream: OutputStream) {
    val folder = Representations.getTemplateFolder(view.getModel.getClass)    
    val stringTemplate = new StringTemplate(Source.fromFile(new File(folder, view.getTemplateName + ".html")).mkString)
    stringTemplate.setAttribute("it", view.getModel)
    val writer = new OutputStreamWriter(stream)
    stringTemplate.write(new AutoIndentWriter(writer))
    writer.flush
  }
}