package modules.model.service

import modicio.core.{ModelElement, TypeHandle}
import modicio.core.rules.{AssociationRule, AttributeRule, ConnectionInterface, ParentRelationRule, Slot}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DeltaProgramInterpreter(abstractController: AbstractController, operationTimeout: Duration) {

  private def await[T](f: Future[T]): T = Await.result(f, operationTimeout)

  def run(deltaProgram: Seq[String]): Unit = {
    val statements: ListBuffer[Seq[String]] = ListBuffer()
    var block: ListBuffer[String] = ListBuffer()
    deltaProgram.foreach(entry => {
      if(entry == "&"){
        statements.addOne(block.toSeq)
        block = ListBuffer()
      }else{
        block.addOne(entry)
      }
    })
    statements.addOne(block.toSeq)
    statements.foreach(interpretStatement)
  }


  private def interpretStatement(statement: Seq[String]): Unit = {

    var cachedClass: Option[TypeHandle] = None
    var cachedAttribute: Option[AttributeRule] = None

    statement.foreach {
      case o if o.startsWith("OPEN CLASS") =>
        cachedClass = Some(await(abstractController.loadClass(subjectName(o))))
      case o if o.startsWith("CLOSE CLASS") && cachedClass.isDefined =>
        await(abstractController.storeClass(cachedClass.get))
        cachedClass = None
      case o if o.startsWith("DELETE CLASS") && cachedClass.isDefined =>
        await(abstractController.deleteClass(subjectName(o)))
        cachedClass = None
      case o if o.startsWith("CREATE CLASS") =>
        await(abstractController.createClass(subjectName(o)))
      case o if o.startsWith("ADD ASSOCIATION") && cachedClass.isDefined => {
        val (relation, target) = parseAddAssociation(o)
        cachedClass.get.applyRule(AssociationRule.create(relation, target, "*",
          new ConnectionInterface(mutable.Buffer(Slot(target, ">0")))))
      }
      case o if o.startsWith("DELETE ASSOCIATION") && cachedClass.isDefined => {
        val associations = cachedClass.get.getAssociations
        val relation = subjectName(o)
        val association = associations.find(_.associationName == relation)
        if(association.isDefined) cachedClass.get.removeRule(association.get)
      }
      //add parent to class
      case o if o.startsWith("ADD PARENT_RELATION") && cachedClass.isDefined => {
        val parent = subjectName(o)
        cachedClass.get.applyRule(ParentRelationRule.create(parent, ModelElement.REFERENCE_IDENTITY))
      }
      //remove parent from class
      case o if o.startsWith("DELETE PARENT_RELATION") && cachedClass.isDefined => {
        val parents = cachedClass.get.getParentRelations
        val parentName = subjectName(o)
        val parent = parents.find(_.parentName == parentName)
        if (parent.isDefined) cachedClass.get.removeRule(parent.get)
      }
      //create attribute in class
      case o if o.startsWith("ADD ATTRIBUTE") && cachedClass.isDefined => {
        val attributeName = subjectName(o)
        cachedClass.get.applyRule(AttributeRule.create(attributeName, "string", nonEmpty = false))
      }
      //delete attribute from class
      case o if o.startsWith("DELETE ATTRIBUTE") && cachedClass.isDefined => {
        val attributes = cachedClass.get.getAttributes
        val attributeName = subjectName(o)
        val attribute = attributes.find(_.name == attributeName)
        if (attribute.isDefined) cachedClass.get.removeRule(attribute.get)
      }
      //load attribute
      case o if o.startsWith("OPEN ATTRIBUTE") && cachedClass.isDefined =>
        val attributes = cachedClass.get.getAttributes
        val attributeName = subjectName(o)
        cachedAttribute =  attributes.find(_.name == attributeName)
      //close attribute
      case o if o.startsWith("CLOSE ATTRIBUTE") && cachedAttribute.isDefined =>
        cachedAttribute = None
      //set type => change datatype of attribute in class
      case o if o.startsWith("SET TYPE") && cachedAttribute.isDefined && cachedClass.isDefined =>
        val typeValue = subjectName(o)
        val newType = {
          if(typeValue == "number"){
            "NUMBER"
          }else if(typeValue == "time"){
            "DATETIME"
          }else{
            "STRING"
          }
        }
        val old = cachedAttribute.get
        val newAttribute = AttributeRule.create(old.name, typeValue, old.nonEmpty)
        cachedClass.get.removeRule(old)
        cachedClass.get.applyRule(newAttribute)
    }

  }

  private def subjectName(operation: String): String = {
    val parts = operation.split(" ").toSeq
    if(parts.size == 2){
      parts(1)
    }else{
      parts(2)
    }
  }

  private def parseAddAssociation(operation: String): (String, String) = {
    val parts = operation.split(" ").toSeq
    (parts(2), parts(4))
  }

}
