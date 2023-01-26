package modules.model.service

import com.google.inject.Singleton
import env.{RegistryProvider, VirtualFileSystem, VirtualRegistryExtension}
import modicio.nativelang.input.NativeDSLParser

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ModelService {

  /*
    FileSystem:         variant/running -> model

    RegistryExtension:  variant -> name
                        running -> name
   */

  // variant -> name
  def getAllStoredVariants: Seq[(String, String)] = {
    VirtualRegistryExtension.getVariantNames.toSeq
  }

  // running -> name
  def getAllStoredVersionsOfVariant(variantId: String): Seq[(String, String)] = {
      VirtualFileSystem.findByPrefix(variantId)
        .map(v => v.split("/")(1))
        .map(v => (v, VirtualRegistryExtension.getVersionNameOf(v)))
  }

  def getStoredHistory: Map[(String, String), Seq[(String, String)]] = {
    getAllStoredVariants.map(variant => (variant, getAllStoredVersionsOfVariant(variant._1))).toMap
  }

  def getActivatedVariant: (String, String) = {
    val variant = VirtualRegistryExtension.getActivatedVariant
    (variant, VirtualRegistryExtension.getVariantNameOf(variant))
  }

  def setActivatedVariant(v: String): Unit = {
    VirtualRegistryExtension.setActivatedVariant(v)
  }

  def getActivatedVersion: (String, String) = {
    val version = VirtualRegistryExtension.getActivatedVersion
    (version, VirtualRegistryExtension.getVersionNameOf((version)))
  }

  def setActivatedVersion(v: String): Unit = {
    VirtualRegistryExtension.setActivatedVersion(v)
  }

  def commit(versionName: String): Future[Any] = {
    for {
      registry <- RegistryProvider.getRegistry
      refTime <- registry.getReferenceTimeIdentity
      decomposedModel <- RegistryProvider.transformer.get.decomposeModel()
    } yield {
      val fileContent = NativeDSLParser.produceString(decomposedModel)
      VirtualRegistryExtension.addVersionName(refTime.runningTime.toString, versionName)
      VirtualFileSystem.write(refTime.variantId + "/" + refTime.runningTime, fileContent)
      setActivatedVersion(refTime.runningTime.toString)
    }
  }


}
