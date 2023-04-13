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

import env.RegistryProvider
import modicio.core.monitoring.Monitoring
import modules.model.formdata.{SetSizeForm, SetTimeForm}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MonitoringController @Inject()(cc: ControllerComponents) extends
  AbstractController(cc) with I18nSupport with Logging {
  
  def index: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    for {
      registry <- RegistryProvider.getRegistry
    } yield {
    
      val monitoringData = registry match {
        case e: Monitoring => registry.asInstanceOf[Monitoring].produceJson().toString()
        case _ => ""
      
      }
    
      /*
      You can also pass the "references" and do something with them, but I don't see a reason why you should...
      Please look at the monitoring.scala.html, I added some code and hints for you.
       */
    
      Ok(views.html.pages.monitoring(monitoringData))
    }
  }
  
  
    def deleteKnowledgeTime(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
      RegistryProvider.getRegistry flatMap (registry => {
        registry match {
          case e: Monitoring => {
            SetTimeForm.form.bindFromRequest fold(
              errorForm => {
                Future.successful(Redirect(routes.MonitoringController.index()))
              },
              data => {
                registry.asInstanceOf[Monitoring].deleteObsoleteKnowledge(data.minutes.toInt)
              })
          }
          case _ => ""
        }
        Future.successful(Redirect(routes.MonitoringController.index()))
      })
    }
    
    def deleteKnowledgeSize(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
      RegistryProvider.getRegistry flatMap (registry => {
        registry match {
          case e: Monitoring => {
            SetSizeForm.form.bindFromRequest fold(
              errorForm => {
                Future.successful(Redirect(routes.MonitoringController.index()))
              },
              data => {
                registry.asInstanceOf[Monitoring].deleteObsoleteKnowledgeBySize(data.size.toInt)
              })
          }
          case _ => ""
        }
        Future.successful(Redirect(routes.MonitoringController.index()))
      })
    }

}