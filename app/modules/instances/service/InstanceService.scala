/*
 * This file is part of the "issue board" modicio case study software.
 * Copyright (C) 2022 Karl Kegel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * */

package modules.instances.service

import env.RegistryProvider
import modicio.core.datamappings.AssociationData
import modicio.core.{DeepInstance, ModelElement, TypeHandle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class InstanceService {

  def getAllKnownTypes: Future[Set[String]] = {
    RegistryProvider.getRegistry flatMap (_.getAllTypes)
  }

  def getExtraTypes: Future[Set[String]] = {
    for {
      referenceTypes <- getAllReferenceTypes()
      allKnownTypeNames <- getAllKnownTypes
    } yield {
      allKnownTypeNames diff referenceTypes.map(_.getTypeName)
    }
  }

  def getNoneTemplateExtraTypes: Future[Set[String]] = {
    getExtraTypes flatMap (elems =>
      Future.sequence(elems.map(elem => getIsTemplateInstancePart(elem))) map (results => {
        results.zip(elems).filter(_._1).map(_._2)
      }))
  }

  def getAllReferenceTypes(childOf: Option[String] = None): Future[Set[TypeHandle]] = {
    for {
      registry <- RegistryProvider.getRegistry
      allRefs <- registry.getReferences
      unfoldedRefs <- Future.sequence(allRefs.map(_.unfold()))
    } yield {
      if (childOf.isDefined) {
        unfoldedRefs.filter(_.getTypeClosure.contains(childOf.get))
      } else {
        unfoldedRefs
      }
    }
  }

  def getSubtypesOf(selection: String): Future[Seq[TypeHandle]] = {
    for {
      refs <- getAllReferenceTypes()
      unfoldedRefs <- Future.sequence(refs.map(_.unfold()))
    } yield {
      unfoldedRefs.filter(_.getTypeClosure.contains(selection)).toSeq.sortBy(_.getTypeName)
    }
  }

  def getLeavesOf(root: String): Future[Seq[TypeHandle]] = {
    getSubtypesOf(root) map (nodes => {
      if (nodes.size <= 1) {
        nodes
      } else {
        // get all types having root as a parent
        def isNotInClosure(node: TypeHandle): Boolean = {
          nodes.diff(Seq(node)).count(_.getTypeClosure.contains(node.getTypeName)) == 0
        }

        nodes.filter(isNotInClosure)
      }
    })
  }

  def newInstance(ofType: String): Future[String] = {
    RegistryProvider.getRegistry flatMap (registry => {
      registry.getType(ofType, ModelElement.REFERENCE_IDENTITY) flatMap (reference => {
        reference.get.hasSingleton flatMap (hasSingleton => {
          if (hasSingleton) {
            Future.failed(throw new UnsupportedOperationException("This model-element is a singleton-instance and can not be instantiated manually"))
          } else {
            val instanceFactory = RegistryProvider.instanceFactory
            instanceFactory.newInstance(ofType) map (_.getInstanceId)
          }
        })
      })
    })
  }

  def getUnfoldedInstance(instanceId: String): Future[DeepInstance] = {
    RegistryProvider.getRegistry flatMap (registry => {
      registry.get(instanceId) flatMap (instanceOption => {
        instanceOption.get.unfold()
      })
    })
  }

  def getIsTemplateInstancePart(typeName: String): Future[Boolean] = {
    RegistryProvider.getRegistry flatMap (registry => {
      registry.getAll(typeName) map (instances => {
        if (instances.isEmpty) {
          false
        } else {
          instances.head.getTypeHandle.getIsTemplate
        }
      })
    })
  }

  /*
  def getMostConcreteUnfoldedInstancesOfType(typeOf: String): Future[Seq[DeepInstance]] = {
    for{
      registry <- RegistryProvider.getRegistry
      types <- getLeavesOf(typeOf)
      instances <- Future.sequence({
        if(types.isEmpty){
          Seq(typeOf)
        }else{
          types.map(_.getTypeName)
        }
      }.map(registry.getAll))
    } yield {
      instances.flatten.sortBy(_.getInstanceId)
    }
  }
   */

  def getMostConcreteUnfoldedInstancesOfType(typeOf: String): Future[Seq[DeepInstance]] = {
    for {
      registry <- RegistryProvider.getRegistry
      abstractInstances <- registry.getAll(typeOf)
      concreteInstances <- Future.sequence(abstractInstances.map(i => registry.getRootOf(i)))
    } yield {
      concreteInstances.toSeq
    }
  }

  def getAssociationMap(unfoldedInstance: DeepInstance): Future[Set[(AssociationData, DeepInstance)]] = {
    RegistryProvider.getRegistry flatMap (registry => {
      Future.sequence(
        unfoldedInstance.getDeepAssociations.map(association =>
          registry.get(association.targetInstanceId) flatMap (instance =>
            if (instance.isEmpty) {
              Future(None)
            } else {
              instance.get.unfold() map (Some(_))
            }
            ))) map (associatedInstanceOptions => {
        val associatedInstances = associatedInstanceOptions.filter(_.isDefined).map(_.get)
        val associationData = unfoldedInstance.getDeepAssociations.filter(d => associatedInstances.exists(_.getInstanceId == d.targetInstanceId))
        associationData.map(data => (data, associatedInstances.find(_.getInstanceId == data.targetInstanceId).get))
      })
    })
  }

  def deleteInstance(instanceId: String): Future[Any] = {
    RegistryProvider.getRegistry flatMap (_.autoRemove(instanceId))
  }

  def updateAttribute(instanceId: String, attributeName: String, attributeValue: String): Future[Any] = {
    getUnfoldedInstance(instanceId) map (instance => {
      instance.assignDeepValue(attributeName, attributeValue)
    })
  }

  def addAssociation(instanceId: String, relation: String, associatedInstanceId: String, associateAsType: String): Future[Any] = {
    (for {
      targetInstance <- getUnfoldedInstance(instanceId)
      associatedInstance <- getUnfoldedInstance(associatedInstanceId)
    } yield (targetInstance, associatedInstance)) map (res =>
      res._1.associate(res._2, associateAsType, relation))
  }

}