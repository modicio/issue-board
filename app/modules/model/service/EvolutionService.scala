package modules.model.service

import modicio.core.util.IdentityProvider

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import sys.process._

class EvolutionService {

  def compileFeatureRequest(rawRequest: String): Seq[String] = {

    //0. get JDK path for featurelang JAR
    val javaConfig = Source.fromFile("./resources/featurecompiler/javaconf.txt")
    val javaPath = javaConfig.getLines().toSeq.head
    javaConfig.close()

    //1. generate file name for input
    val fileID = IdentityProvider.newRandomId()
    val fileName = "./" + fileID + ".featurelang"
    val file = new File(fileName)

    //2. write raw request into file
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(rawRequest)
    bufferedWriter.close()

    //3. call jar and return result
    val response = Process(javaPath + " -jar ./resources/featurecompiler/featurelang.jar " +
      fileName + " " +
      "./resources/featurecompiler/out").!!

    val lines = response.split('\n').filter(!_.isBlank)
    val deltaProgram = lines.slice(lines.indexOf(">>>")+1, lines.indexOf("<<<")).toSeq
    println(deltaProgram)

    deltaProgram
  }

}
