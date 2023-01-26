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

package controllers

import env.{RegistryProvider, VirtualFileSystem, VirtualRegistryExtension}
import modicio.core.ModelElement
import modicio.core.rules._
import modicio.core.values.ConcreteValue
import modicio.nativelang.input.NativeDSLParser
import modules.instances.service.InstanceService
import modules.model.formdata._
import modules.model.service.ModelService
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ModelController @Inject()(cc: ControllerComponents, modelService: ModelService, instanceService: InstanceService) extends
  AbstractController(cc) with I18nSupport with Logging {

  def index: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      registry <- RegistryProvider.getRegistry
      references <- registry.getReferences
    } yield {

      val allVariants = modelService.getAllStoredVariants
      val activatedVariant = modelService.getActivatedVariant
      val activatedVersion = modelService.getActivatedVersion
      val history = modelService.getStoredHistory

      //for debug:
      val fs = VirtualFileSystem
      val rx = VirtualRegistryExtension

      Ok(views.html.pages.model_overview(references.toSeq,
        references.find(_.getTypeName == ModelElement.ROOT_NAME).get.getTimeIdentity,
        allVariants,
        history,
        activatedVariant._1,
        activatedVersion._1))
    }
  }

  def fragment(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    RegistryProvider.getRegistry flatMap (registry => {
      registry.getType(name, identity) flatMap (typeOption => {
        for {
          typeHandle <- typeOption.get.unfold()
          hasSingleton <- typeHandle.hasSingleton
          hasSingletonRoot <- typeHandle.hasSingletonRoot
          allTypes <- registry.getAllTypes
          allVariants <- registry.getVariantMap
        } yield {
          Ok(views.html.pages.model_element_details(typeHandle, allTypes.toSeq, allVariants.toSeq.sortBy(_._1._1), hasSingleton, hasSingletonRoot))
        }
      })
    })
  }

  def deleteFragment(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    RegistryProvider.getRegistry flatMap (registry => {
      if (name != ModelElement.ROOT_NAME && name != "Issue" && name != "Milestone") {
        registry.autoRemove(name, identity) map (_ => Redirect(routes.ModelController.index()))
      } else {
        Future.successful(Redirect(routes.ModelController.index()))
      }
    })
  }

  def addModelElement(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewFragmentForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.index()))
      },
      data => {
        RegistryProvider.getRegistry flatMap (registry => {
          val typeFactory = RegistryProvider.typeFactory
          val typeName = data.fragmentName
          val isTemplate = data.fragmentType == "TEMPLATE"
          typeFactory.newType(typeName, ModelElement.REFERENCE_IDENTITY, isTemplate) flatMap (newType =>
            registry.setType(newType) map (_ => Redirect(routes.ModelController.fragment(typeName, ModelElement.REFERENCE_IDENTITY))))
        })
      })
  }

  def addExtensionRule(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewExtensionRuleForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.fragment(name, identity)))
      },
      data => {
        if (name == "Milestone") {
          Future(Redirect(routes.ModelController.fragment(name, identity)))
        } else {

          RegistryProvider.getRegistry flatMap (registry => {
            registry.getType(name, identity) flatMap (typeOption => {
              typeOption.get.unfold() flatMap (typeHandle => {
                if(data.parent != "Milestone") {
                  val newRule = ParentRelationRule.create(data.parent, ModelElement.REFERENCE_IDENTITY)
                  if (newRule.verify()) {
                    typeHandle.applyRule(newRule)
                  }
                  typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
                } else {
                  Future(Redirect(routes.ModelController.fragment(name, identity)))
                }
              })
            })
          })

        }

      })
  }

  def addAttributeRule(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewAttributeRuleForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.fragment(name, identity)))
      },
      data => {
        RegistryProvider.getRegistry flatMap (registry => {
          registry.getType(name, identity) flatMap (typeOption => {
            typeOption.get.unfold() flatMap (typeHandle => {
              val nonEmpty = data.nonEmpty == "true"
              val newRule = AttributeRule.create(data.attributeName, data.datatype, nonEmpty)
              if (newRule.verify()) {
                typeHandle.applyRule(newRule)
              }
              typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
            })
          })
        })
      })
  }

  def addConcreteValueRule(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewConcreteValueRuleForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.fragment(name, identity)))
      },
      data => {
        RegistryProvider.getRegistry flatMap (registry => {
          registry.getType(name, identity) flatMap (typeOption => {
            typeOption.get.unfold() flatMap (typeHandle => {
              val isFinal = data.isFinal == "true"
              val newRule = ConcreteValue.create(data.valueType, data.valueName, (data.content + ":" + isFinal).split(":"))
              if (newRule.verify()) {
                typeHandle.applyRule(newRule)
              }
              typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
            })
          })
        })
      })
  }

  def addLinkRule(name: String, identity: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewLinkRuleForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.fragment(name, identity)))
      },
      data => {
        RegistryProvider.getRegistry flatMap (registry => {
          registry.getType(name, identity) flatMap (typeOption => {
            typeOption.getOrElse(throw new Exception()).unfold() flatMap (typeHandle => {
              val connectionInterface = new ConnectionInterface(mutable.Buffer[Slot]())
              val newRule = AssociationRule.create(data.linkName, data.targetName, data.multiplicity, connectionInterface)
              if (newRule.verify()) {
                typeHandle.applyRule(newRule)
              }
              typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
            })
          })
        })
      })
  }

  def addSlot(name: String, identity: String, ruleId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewSlotForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.fragment(name, identity)))
      },
      data => {
        RegistryProvider.getRegistry flatMap (registry => {
          registry.getType(name, identity) flatMap (typeOption => {
            typeOption.getOrElse(throw new Exception()).unfold() flatMap (typeHandle => {
              val prefix = data.prefix
              val variantTime = data.variantTime
              typeHandle.applySlot(ruleId, prefix + variantTime)
              typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
            })
          })
        })
      })
  }

  def removeSlot(name: String, identity: String, ruleId: String, variantTimeArg: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    RegistryProvider.getRegistry flatMap (registry => {
      registry.getType(name, identity) flatMap (typeOption => {
        typeOption.getOrElse(throw new Exception()).unfold() flatMap (typeHandle => {
          typeHandle.removeSlot(ruleId, variantTimeArg)
          typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
        })
      })
    })
  }

  def removeRule(name: String, identity: String, ruleId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    RegistryProvider.getRegistry flatMap (registry => {
      registry.getType(name, identity) flatMap (typeOption => {
        typeOption.get.unfold() flatMap (typeHandle => {
          typeHandle.removeRule(ruleId)
          typeHandle.commit() map (_ => Redirect(routes.ModelController.fragment(name, identity)))
        })
      })
    })
  }

  def incrementVariant(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    ForkVariantForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.index()))
      },
      data => {
        for {
          registry <- RegistryProvider.getRegistry
          _ <- registry.incrementVariant
          refTime <- registry.getReferenceTimeIdentity
        } yield {
          VirtualRegistryExtension.addVariantName(refTime.variantId, data.withName)
          modelService.setActivatedVariant(refTime.variantId)
          Redirect(routes.ModelController.index())
        }
      })
  }

  def commit(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    CommitVersionForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.index()))
      },
      data => {
        modelService.commit(data.withName) map (_ => Redirect(routes.ModelController.index()))
      })
  }

  def exchangeModel(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    ExchangeModelForm.form.bindFromRequest fold(
      _ => {
        Future.successful(Redirect(routes.ModelController.index()))
      },
      data => {

        val variantValue = data.variant
        val versionValue = data.version

        for {
          registry <- RegistryProvider.getRegistry
          model <- RegistryProvider.transformer.get.transform(
            NativeDSLParser.parse(
              VirtualFileSystem.read(variantValue + "/" + versionValue)))
          _ <- registry.exchangeModel(model.toSet)
        } yield {
          modelService.setActivatedVersion(data.version)
          modelService.setActivatedVariant(data.variant)
          Redirect(routes.ModelController.index())
        }
      })
  }

}