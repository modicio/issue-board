package modules.model.service

import com.google.inject.Inject
import env.RegistryProvider
import modicio.core.{ModelElement, TypeHandle}
import modicio.core.util.IdentityProvider

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.io.Source
import sys.process._


class EvolutionService @Inject()(modelService: ModelService) {

  val deltaProgramInterpreter = new DeltaProgramInterpreter(new AbstractController {

    override def loadClass(name: String): Future[TypeHandle] = {
      RegistryProvider.getRegistry flatMap(registry => {
        registry.getType(name, ModelElement.REFERENCE_IDENTITY) map (typeOption => typeOption.get)})
    }

    override def createClass(name: String): Future[Any] = {
      RegistryProvider.getRegistry flatMap(registry => {
        RegistryProvider.typeFactory.newType(name, ModelElement.REFERENCE_IDENTITY, isTemplate = false) flatMap (newType =>
          registry.setType(newType))
      })
    }

    override def deleteClass(name: String): Future[Any] = {
      RegistryProvider.getRegistry flatMap (registry => {
        if (name != ModelElement.ROOT_NAME &&
          name != "Issue" &&
          name != "Milestone") {
          registry.autoRemove(name, ModelElement.REFERENCE_IDENTITY)
        }else{
          Future.failed(new IllegalArgumentException("Cannot delete Issue, Milestone or ROOT!"))
        }
      })
    }

    override def storeClass(typeHandle: TypeHandle): Future[Any] = {
      typeHandle.commit()
    }

  }, Duration.create(10, TimeUnit.SECONDS))

  def compileFeatureRequest(rawRequest: String): (Seq[String], String) = {

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
    //println("response", response)
    println(deltaProgram)
    (deltaProgram, response)
  }

  def interpretDeltaProgram(program: Seq[String]): Unit = {
    deltaProgramInterpreter.run(program)
  }

}
