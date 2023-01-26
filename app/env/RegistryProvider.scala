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

package env

import modicio.core.{InstanceFactory, ModelElement, Registry, Rule, TimeIdentity, TypeFactory}
import modicio.nativelang.defaults.{SimpleDefinitionVerifier, SimpleMapRegistry, SimpleModelVerifier}
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}
import modules.model.service.ModelService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

object RegistryProvider {

  val modelVerifier = new SimpleModelVerifier()
  val definitionVerifier = new SimpleDefinitionVerifier()

  val typeFactory = new TypeFactory(definitionVerifier, modelVerifier)
  val instanceFactory = new InstanceFactory(definitionVerifier, modelVerifier)

  private var registry: Option[Registry] = None

  var transformer: Option[NativeDSLTransformer] = None

  private def init(): Future[Any] = {

    //Rules generate their own UUIDs:ss
    Rule.enableAutoID()

    registry = Some(new SimpleMapRegistry(typeFactory, instanceFactory))

    typeFactory.setRegistry(registry.get)
    instanceFactory.setRegistry(registry.get)

    val source = Source.fromFile("resources/issue_model.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    transformer = Some(new NativeDSLTransformer(registry.get, definitionVerifier, modelVerifier))

    val preset = for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TimeIdentity.create))
      _ <- registry.get.setType(root)
      _ <- transformer.get.extend(initialInput)
      _ <- registry.get.incrementVariant
      allReferences <- registry.get.getReferences
      unfoldedReferences <- Future.sequence(allReferences.filter(t => !t.getIsTemplate).map(_.unfold()))
      _ <- Future.sequence(unfoldedReferences.filter(_.isConcrete).map(_.updateSingletonRoot()))
      refTime <- registry.get.getReferenceTimeIdentity
    } yield {
      val variantId = refTime.variantId
      val versionTime = refTime.runningTime

      VirtualRegistryExtension.addVariantName(variantId, "BASE")
      VirtualRegistryExtension.setActivatedVariant(variantId)
      VirtualRegistryExtension.setActivatedVersion(versionTime.toString)
    }

    preset flatMap(_ => {
      val modelService = new ModelService()
      modelService.commit("BASE")
    })
  }

  def getRegistry: Future[Registry] = {
    if (registry.isDefined) {
      Future.successful(registry.get)
    } else {
      init() map (_ => registry.get)
    }

  }

}
