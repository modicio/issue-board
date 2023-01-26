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

import modules.instances.formdata.{NewAssociationForm, UpdateStringValueForm}
import modules.instances.service.InstanceService
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MilestoneController @Inject()(cc: ControllerComponents, instanceService: InstanceService) extends
  AbstractController(cc) with I18nSupport with Logging {

  def index(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      instances <- instanceService.getMostConcreteUnfoldedInstancesOfType("Milestone")
    } yield {
      Ok(views.html.pages.milestone_overview(instances.toSeq.sortBy(_.getInstanceId)))
    }
  }

  def addMilestone(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    instanceService.newInstance("Milestone") map (resId => {
      Redirect(routes.MilestoneController.getMilestone(resId))
    })
  }

  def getMilestone(instanceId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      instance <- instanceService.getUnfoldedInstance(instanceId)
      associationMap <- instanceService.getAssociationMap(instance)
    } yield {
      Ok(views.html.pages.milestone_details(instance, associationMap))
    }
  }

  def deleteMilestone(instanceId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    instanceService.deleteInstance(instanceId) map (_ => Redirect(routes.MilestoneController.index()))
  }

  def updateAttribute(instanceId: String, attributeName: String): Action[AnyContent] =
    Action.async { implicit request: Request[AnyContent] =>
      UpdateStringValueForm.form.bindFromRequest fold(
        errorForm => {
          Future.successful(Redirect(routes.MilestoneController.getMilestone(instanceId)))
        },
        data => {
          val attributeValue = data.newValue
          instanceService.updateAttribute(instanceId, attributeName, attributeValue) map (_ =>
            Redirect(routes.MilestoneController.getMilestone(instanceId)))
        })
    }

  def addAssociation(instanceId: String, relation: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    NewAssociationForm.form.bindFromRequest fold(
      errorForm => {
        Future.successful(Redirect(routes.MilestoneController.getMilestone(instanceId)))
      },
      data => {
        var associatedInstanceId = data.instanceId
        var associateAsType = data.asType
        if (data.instanceId.contains(":")) {
          val parts = data.instanceId.split(":")
          associateAsType = parts(0)
          associatedInstanceId = parts(1)
        }
        instanceService.addAssociation(instanceId, relation, associatedInstanceId, associateAsType) map (_ =>
          Redirect(routes.MilestoneController.getMilestone(instanceId)))
      })
  }

}