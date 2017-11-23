package ro.levi9.shoppingbasket.services

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.Configuration
import ro.levi9.shoppingbasket.{TestActorKit, TestValues}

class CatalogServiceSpec extends TestActorKit
  with MockitoSugar
  with MustMatchers
  with WordSpecLike
  with ScalaFutures
  with TestValues {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val config = Configuration(ConfigFactory.load("application.conf"))

  "A CatalogService" must {

    "return all the items in the catalog" in {
      val catalogActorRef = testActorRef(catalogList)
      val catalogService = new CatalogService(catalogActorRef, config)
      val result = catalogService.listCatalog

      result.futureValue mustEqual catalogList
    }

  }
}
