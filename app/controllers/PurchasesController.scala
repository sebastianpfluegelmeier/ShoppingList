package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}
import play.api.libs.json._ 
import play.api.libs.json.Reads._ 
import play.api.libs.functional.syntax._ 


@Singleton
class PurchasesController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao
  
  def purchases() = Action { implicit request: Request[AnyContent] => 
    var username = request.session.get("userName")
    var id = request.session.get("userId")
    id match {
        case Some(uid: String) => {
            var purchases = dao.getPurchases(uid.toLong)
            Ok(views.html.purchases(username, purchases))
        }
        case other => Redirect("login")
    }
  }

  def purchase(id: String) = Action { implicit request: Request[AnyContent] => 
    var username = request.session.get("userName")
    Ok(views.html.purchase(username, id.toLong))
  }

  def allUserShoppingListsIds()  = Action { implicit request: Request[AnyContent] => 
    var personId = request.session.get("userId").get.toLong
    var ids = dao.getAllShoppingLists(personId).map(s => s.id)
    Ok(Json.arr(ids)(0))
  }

  case class ShoppingListJson(name: String, id: Long, householdId: Long, list: List[ShoppingListItemJson], disabled: Boolean)
  case class ShoppingListItemJson(name: String, id: Long, purchaseId: Option[Long])
  case class PurchaseAndShoppingLists (shoppingLists: List[ShoppingListJson], purchase: Purchase)

  implicit val formatShoppingListItem: Format[ShoppingListItemJson] = (
      (JsPath \ "name").format[String] and
      (JsPath \ "id").format[Long] and
      (JsPath \ "purchaseId").formatNullable[Long]
    )(ShoppingListItemJson.apply(_, _,_), unapply(ShoppingListItemJson.unapply))

  implicit val formatPurchases: Format[Purchase] = (
      (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "personId").format[Long] and
      (JsPath \ "price").format[Int] and
      (JsPath \ "disabled").format[Boolean]
    )(Purchase.apply, unlift(Purchase.unapply))

  implicit val formatShoppingList: Format[ShoppingListJson] = (
      (JsPath \ "name").format[String] and
      (JsPath \ "id").format[Long] and
      (JsPath \ "householdId").format[Long] and
      (JsPath \ "list").format[List[ShoppingListItemJson]] and
      (JsPath \ "disabled").format[Boolean] 
    )(ShoppingListJson.apply(_,_,_,_,_), unlift(ShoppingListJson.unapply))

  implicit val formatPuraseShoppingLists: Format[PurchaseAndShoppingLists] = (
    (JsPath \ "shoppingLists").format[List[ShoppingListJson]] and
    (JsPath \ "purchase").format[Purchase]
  )(PurchaseAndShoppingLists.apply(_, _), unlift(PurchaseAndShoppingLists.unapply))

  def purchaseJson(id: String) = Action {implicit request: Request[AnyContent] =>
    var purchase = dao.getPurchase(id.toLong)
    var purchaseJson = Json.toJson(purchase)
    Ok(purchaseJson)
  }

  def purchaseAndShoppingListsJsonPost(id: String) = Action {implicit request: Request[AnyContent] =>
    var purchaseAndShoppingLists = request.body.asJson.get.as[PurchaseAndShoppingLists]
    dao.setPurchase(id.toLong, purchaseAndShoppingLists.purchase)

    purchaseAndShoppingLists.shoppingLists.map(sl => {
      val items = sl.list.map(item => ShoppingListItem(Some(item.id), sl.id, item.name, item.purchaseId))
      val shoppingList = ShoppingList(Some(sl.id), sl.name, sl.householdId, sl.disabled)
      dao.upsertShoppingList(shoppingList, items)
    })

    Ok("")
  }

  def newPurchase() = Action {implicit request: Request[AnyContent] => 
    val purchaseId = dao.newPurchase(request.session.data.get("userId").get.toLong)
    Redirect("/purchase/" + purchaseId)
  }

  def deletePurchase(id: String) = Action {implicit request: Request[AnyContent] =>
    dao.disablePurchase(id.toLong)
    Redirect("/purchases")
  } 
}