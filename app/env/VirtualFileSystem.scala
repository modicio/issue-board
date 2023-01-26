package env

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

object VirtualFileSystem {

  private val map = new ConcurrentHashMap[String, String]()

  def exists(uri: String): Boolean = {
    map.containsKey(uri)
  }

  def write(uri: String, content: String): Unit = {
    map.put(uri, content)
  }

  def delete(uri: String): Unit = {
    map.remove(uri)
  }

  def read(uri: String): String = {
    map.get(uri)
  }

  def findByPrefix(prefixUri: String): Seq[String] = {
    map.asScala.keySet.filter(_.startsWith(prefixUri)).toSeq
  }

}
