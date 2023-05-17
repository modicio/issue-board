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
import modules.model.formdata.FeatureRequestForm
import modules.model.service.EvolutionService
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FeatureRequestController @Inject()(cc: ControllerComponents, evolutionService: EvolutionService) extends
  AbstractController(cc) with I18nSupport with Logging {

  def getRequestPage: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("Hi")
  }

  def getSuccessPage: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("Work Done!")
  }

  def processRequest(): Action[AnyContent] =
    Action.async { implicit request: Request[AnyContent] =>
      FeatureRequestForm.form.bindFromRequest fold (
        errorForm => {
          Future.successful(Redirect(routes.FeatureRequestController.getRequestPage()))
        },
        data => {
          val content = data.content
          try {
            val deltaSequence = evolutionService.compileFeatureRequest(content)
          }
          Redirect(routes.MilestoneController.getMilestone(instanceId)))
        })
    }

}