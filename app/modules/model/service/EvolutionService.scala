package modules.model.service

import modicio.core.util.IdentityProvider

import java.io.{BufferedWriter, File, FileWriter}
import sys.process._

class EvolutionService {

  def compileFeatureRequest(rawRequest: String): Seq[String] = {

    //1. generate file name for input
    val fileID = IdentityProvider.newRandomId()
    val fileName = "./" + fileID + ".featurelang"
    val file = new File(fileName)

    //2. write raw request into file
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(rawRequest)
    bufferedWriter.close()

    //3. call jar and return result
    val response = Process("java -jar ./resources/featurecompiler/featurelang.jar " +
      fileName + " " +
      "./resources/featurecompiler/out").!!

    val lines = response.split('\n').filter(!_.isBlank)
    val deltaProgram = lines.slice(lines.indexOf(">>>")+1, lines.indexOf("<<<")).toSeq
    println(deltaProgram)

    deltaProgram
  }

}
