package env

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

object VirtualRegistryExtension {

  private val namedVariants = new ConcurrentHashMap[String, String]()

  private val namedVersions = new ConcurrentHashMap[String, String]()

  private var activatedVariant = "HEAD"
  private var activatedVersion = "HEAD"

  def getVariantNames: Map[String, String] = namedVariants.asScala.toMap

  def addVariantName(variantId: String, name: String): Unit = namedVariants.put(variantId, name)

  def getVariantNameOf(variantId: String): String = namedVariants.get(variantId)

  def getVersionNames: Map[String, String] = namedVariants.asScala.toMap

  def addVersionName(runningId: String, name: String): Unit = namedVersions.put(runningId, name)

  def getVersionNameOf(runningId: String): String = namedVersions.get(runningId)

  def setActivatedVariant(variant: String): Unit = activatedVariant = variant

  def setActivatedVersion(version: String): Unit = activatedVersion = version

  def getActivatedVariant: String = activatedVariant

  def getActivatedVersion: String = activatedVersion

}
