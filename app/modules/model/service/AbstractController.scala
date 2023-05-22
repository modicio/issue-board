package modules.model.service

import modicio.core.TypeHandle

import scala.concurrent.Future

trait AbstractController {

  def loadClass(name: String): Future[TypeHandle]

  def createClass(name: String): Future[Any]

  def deleteClass(name: String): Future[Any]

  def storeClass(typeHandle: TypeHandle): Future[Any]

}
