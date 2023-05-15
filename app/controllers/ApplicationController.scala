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

import modules.model.service.EvolutionService

import javax.inject._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._


@Singleton
class ApplicationController @Inject()(cc: ControllerComponents, evolutionService: EvolutionService) extends
  AbstractController(cc) with I18nSupport with Logging {


  def index: Action[AnyContent] = Action { implicit request: Request[AnyContent] => {
    evolutionService.compileFeatureRequest("the Car must have the attribute speed. it is a number.\nCar should not inherit from Bus.\nthe class Car has an existing manufacturer.\nit must be a phrase.\nthe class Car must have associations to Driver called drivenBy.\nit must exist the class Ship.")
    Redirect(routes.IssueController.index(""))
    }
  }

}
